package ch.rasc.otodo.controller;

import static ch.rasc.otodo.db.tables.AppSession.APP_SESSION;
import static ch.rasc.otodo.db.tables.AppUser.APP_USER;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.passpol.PasswordPolicy;
import com.codahale.passpol.Status;

import ch.rasc.otodo.config.AppProperties;
import ch.rasc.otodo.config.security.AuthHeaderFilter;
import ch.rasc.otodo.config.security.JooqUserDetails;
import ch.rasc.otodo.dto.SessionInfo;
import ch.rasc.otodo.service.EmailService;
import ch.rasc.otodo.service.TokenService;

@RestController
@Validated
@RequestMapping("/be")
public class ProfileController {

  private final DSLContext dsl;

  private final PasswordEncoder passwordEncoder;

  private final TokenService tokenService;

  private final EmailService emailService;

  private final AppProperties appProperties;

  private final PasswordPolicy passwordPolicy;

  private final BuildProperties buildProperties;

  public ProfileController(DSLContext dsl, PasswordEncoder passwordEncoder,
      TokenService tokenService, EmailService emailService, AppProperties appProperties,
      PasswordPolicy passwordPolicy, BuildProperties buildProperties) {
    this.dsl = dsl;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.emailService = emailService;
    this.appProperties = appProperties;
    this.passwordPolicy = passwordPolicy;
    this.buildProperties = buildProperties;
  }

  enum ChangePasswordResponse {
    INVALID, WEAK_PASSWORD
  }

  @PostMapping("/change-password")
  public ChangePasswordResponse changePassword(
      @AuthenticationPrincipal JooqUserDetails user,
      @RequestParam("oldPassword") @NotEmpty String oldPassword,
      @RequestParam("newPassword") @NotEmpty String newPassword) {

    Status status = this.passwordPolicy.check(newPassword);
    if (status != Status.OK) {
      return ChangePasswordResponse.WEAK_PASSWORD;
    }

    return this.dsl.transactionResult(txConf -> {
      try (var txdsl = DSL.using(txConf)) {
        if (passwordMatches(txdsl, user.getUserDbId(), oldPassword)) {
          String encodedNewPassword = this.passwordEncoder.encode(newPassword);

          txdsl.update(APP_USER).set(APP_USER.PASSWORD_HASH, encodedNewPassword)
              .where(APP_USER.ID.eq(user.getUserDbId())).execute();

          this.emailService.sendPasswordChangedEmail(user.getUsername());

          this.dsl.delete(APP_SESSION)
              .where(APP_SESSION.APP_USER_ID.eq(user.getUserDbId())).execute();

          return null;
        }

        return ChangePasswordResponse.INVALID;
      }
    });
  }

  @PostMapping("/delete-account")
  public boolean deleteAccount(@AuthenticationPrincipal JooqUserDetails user,
      @RequestBody @NotEmpty String password) {

    return this.dsl.transactionResult(txConf -> {
      try (var txdsl = DSL.using(txConf)) {
        if (passwordMatches(txdsl, user.getUserDbId(), password)) {
          txdsl.delete(APP_USER).where(APP_USER.ID.eq(user.getUserDbId())).execute();
          return true;
        }

        return false;
      }
    });
  }

  enum ChangeEmailResponse {
    SAME, // No change
    USE, // Email in use by another user
    PASSWORD // Password wrong
  }

  @PostMapping("/change-email")
  public ChangeEmailResponse changeEmail(@AuthenticationPrincipal JooqUserDetails user,
      @RequestParam("password") @NotEmpty String password,
      @RequestParam("newEmail") @NotEmpty @Email String newEmail) {

    return this.dsl.transactionResult(txConf -> {
      try (var txdsl = DSL.using(txConf)) {

        // is new email same as old email
        int count = this.dsl.selectCount().from(APP_USER).where(APP_USER.EMAIL
            .equalIgnoreCase(newEmail).and(APP_USER.ID.eq(user.getUserDbId())))
            .fetchOne(0, int.class);
        if (count > 0) {
          return ChangeEmailResponse.SAME;
        }

        // is new email already used by another user
        count = this.dsl.selectCount().from(APP_USER)
            .where(APP_USER.EMAIL.equalIgnoreCase(newEmail)).fetchOne(0, int.class);
        if (count > 0) {
          return ChangeEmailResponse.USE;
        }

        if (passwordMatches(txdsl, user.getUserDbId(), password)) {

          String confirmationToken = this.tokenService.createToken();
          txdsl.update(APP_USER)
              .set(APP_USER.CONFIRMATION_TOKEN_CREATED, LocalDateTime.now())
              .set(APP_USER.CONFIRMATION_TOKEN, confirmationToken)
              .set(APP_USER.EMAIL_NEW, newEmail).where(APP_USER.ID.eq(user.getUserDbId()))
              .execute();

          this.emailService.sendEmailChangeConfirmationEmail(newEmail, confirmationToken);

          return null;
        }

        return ChangeEmailResponse.PASSWORD;
      }
    });
  }

  @PostMapping("/confirm-email-change")
  public boolean confirmEmailChange(@RequestBody @NotEmpty String token) {

    var record = this.dsl.select(APP_USER.ID, APP_USER.CONFIRMATION_TOKEN_CREATED)
        .from(APP_USER).where(APP_USER.CONFIRMATION_TOKEN.equal(token)).fetchOne();

    if (record != null) {
      long userId = record.get(APP_USER.ID);
      LocalDateTime tokenCreated = record.get(APP_USER.CONFIRMATION_TOKEN_CREATED);

      if (tokenCreated != null && tokenCreated.isAfter(LocalDateTime.now()
          .minus(this.appProperties.getSignupNotConfirmedUserMaxAge()))) {

        this.dsl.update(APP_USER).set(APP_USER.CONFIRMATION_TOKEN, (String) null)
            .set(APP_USER.CONFIRMATION_TOKEN_CREATED, (LocalDateTime) null)
            .set(APP_USER.EMAIL, APP_USER.EMAIL_NEW)
            .set(APP_USER.EMAIL_NEW, (String) null).where(APP_USER.ID.equal(userId))
            .execute();

        return true;
      }

      this.dsl.update(APP_USER).set(APP_USER.CONFIRMATION_TOKEN, (String) null)
          .set(APP_USER.CONFIRMATION_TOKEN_CREATED, (LocalDateTime) null)
          .set(APP_USER.EMAIL_NEW, (String) null).where(APP_USER.ID.equal(userId))
          .execute();
    }

    return false;

  }

  @GetMapping("/sessions")
  public List<SessionInfo> sessions(@AuthenticationPrincipal JooqUserDetails user,
      HttpServletRequest request) {

    String sessionId = request.getHeader(AuthHeaderFilter.HEADER_NAME);

    return this.dsl.selectFrom(APP_SESSION)
        .where(APP_SESSION.APP_USER_ID.eq(user.getUserDbId())).fetch().stream()
        .map(record -> new SessionInfo(record.getId(), record.getId().equals(sessionId),
            record.getLastAccess(), record.getIp(), record.getUserAgent()))
        .collect(Collectors.toList());
  }

  @PostMapping("/delete-session")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSession(@RequestBody @NotEmpty String sessionId,
      @AuthenticationPrincipal JooqUserDetails user) {
    this.dsl.delete(APP_SESSION).where(
        APP_SESSION.ID.eq(sessionId).and(APP_SESSION.APP_USER_ID.eq(user.getUserDbId())))
        .execute();
  }

  private boolean passwordMatches(DSLContext d, long appUserId, String password) {
    String passwordFromDb = d.select(APP_USER.PASSWORD_HASH).from(APP_USER)
        .where(APP_USER.ID.eq(appUserId)).fetchOne().get(APP_USER.PASSWORD_HASH);

    return this.passwordEncoder.matches(password, passwordFromDb);
  }

  @GetMapping("/build-info")
  public BuildProperties buildInfo() {
    return this.buildProperties;
  }

}
