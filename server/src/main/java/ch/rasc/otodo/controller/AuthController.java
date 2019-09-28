package ch.rasc.otodo.controller;

import static ch.rasc.otodo.db.tables.AppSession.APP_SESSION;
import static ch.rasc.otodo.db.tables.AppUser.APP_USER;

import java.time.LocalDateTime;
import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import ch.rasc.otodo.db.tables.records.AppUserRecord;
import ch.rasc.otodo.service.EmailService;
import ch.rasc.otodo.service.TokenService;

@RestController
@Validated
@RequestMapping("/be")
class AuthController {

  private final DSLContext dsl;

  private final PasswordEncoder passwordEncoder;

  private final TokenService tokenService;

  private final EmailService emailService;

  private final AppProperties appProperties;

  private final PasswordPolicy passwordPolicy;

  public AuthController(DSLContext dsl, PasswordEncoder passwordEncoder,
      TokenService tokenService, EmailService emailService, AppProperties appProperties,
      PasswordPolicy passwordPolicy) {
    this.dsl = dsl;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.emailService = emailService;
    this.appProperties = appProperties;
    this.passwordPolicy = passwordPolicy;
  }

  @GetMapping("/authenticate")
  public String authenticate(@AuthenticationPrincipal UserDetails user) {
    return user.getAuthorities().iterator().next().getAuthority();
  }

  enum SignupResponse {
    EMAIL_REGISTERED, WEAK_PASSWORD
  }

  @PostMapping("/signup")
  public SignupResponse signup(@RequestParam("email") @Email @NotEmpty String email,
      @RequestParam("password") @NotEmpty String password) {

    // delete an old unconfirmed registration
    this.dsl.delete(APP_USER).where(APP_USER.EMAIL.equalIgnoreCase(email)
        .and(APP_USER.CONFIRMATION_TOKEN.isNotNull())).execute();

    // cancel if the user is already registered
    int count = this.dsl.selectCount().from(APP_USER)
        .where(APP_USER.EMAIL.equalIgnoreCase(email)).fetchOne(0, int.class);
    if (count > 0) {
      return SignupResponse.EMAIL_REGISTERED;
    }

    Status status = this.passwordPolicy.check(password);
    if (status != Status.OK) {
      return SignupResponse.WEAK_PASSWORD;
    }

    String confirmationToken = this.tokenService.createToken();

    AppUserRecord record = this.dsl.newRecord(APP_USER);
    record.setEmail(email);
    record.setPasswordHash(this.passwordEncoder.encode(password));
    record.setAuthority("USER");
    record.setEnabled(false);
    record.setConfirmationToken(confirmationToken);
    record.setConfirmationTokenCreated(LocalDateTime.now());
    record.setExpired(null);
    record.setFailedLogins(null);
    record.setLockedOut(null);
    record.setLastAccess(null);
    record.setPasswordResetToken(null);
    record.setPasswordResetTokenCreated(null);
    record.store();

    this.emailService.sendEmailConfirmationEmail(email, confirmationToken);

    return null;
  }

  @PostMapping("/confirm-signup")
  public boolean confirmSignup(@RequestBody @NotEmpty String token) {

    var record = this.dsl.select(APP_USER.ID, APP_USER.CONFIRMATION_TOKEN_CREATED)
        .from(APP_USER).where(APP_USER.CONFIRMATION_TOKEN.equal(token)).fetchOne();

    if (record != null) {
      long userId = record.get(APP_USER.ID);
      LocalDateTime tokenCreated = record.get(APP_USER.CONFIRMATION_TOKEN_CREATED);

      if (tokenCreated != null && tokenCreated.isAfter(LocalDateTime.now()
          .minus(this.appProperties.getSignupNotConfirmedUserMaxAge()))) {

        this.dsl.update(APP_USER).set(APP_USER.ENABLED, true)
            .set(APP_USER.CONFIRMATION_TOKEN, (String) null)
            .set(APP_USER.CONFIRMATION_TOKEN_CREATED, (LocalDateTime) null)
            .where(APP_USER.ID.equal(userId)).execute();

        return true;
      }

      this.dsl.delete(APP_USER).where(APP_USER.ID.equal(userId)).execute();
    }

    return false;
  }

  @PostMapping("/reset-password-request")
  public boolean resetPasswordRequest(@RequestBody @NotEmpty @Email String email) {

    var record = this.dsl.select(APP_USER.ID, APP_USER.EMAIL).from(APP_USER)
        .where(APP_USER.EMAIL.equalIgnoreCase(email)).limit(1).fetchOne();

    if (record != null) {
      long userId = record.get(APP_USER.ID);
      String resetToken = this.tokenService.createToken();

      this.dsl.update(APP_USER).set(APP_USER.PASSWORD_RESET_TOKEN, resetToken)
          .set(APP_USER.PASSWORD_RESET_TOKEN_CREATED, LocalDateTime.now())
          .where(APP_USER.ID.equal(userId)).execute();

      this.emailService.sendPasswordResetEmail(record.get(APP_USER.EMAIL), resetToken);
    }

    return true;
  }

  enum ResetPasswordResponse {
    INVALID, WEAK_PASSWORD
  }

  @PostMapping("/reset-password")
  public ResetPasswordResponse resetPassword(@RequestParam("resetToken") @NotEmpty String resetToken,
      @RequestParam("password") @NotEmpty String password) {

    var record = this.dsl.select(APP_USER.ID, APP_USER.PASSWORD_RESET_TOKEN_CREATED)
        .from(APP_USER).where(APP_USER.PASSWORD_RESET_TOKEN.equal(resetToken)).fetchOne();

    if (record != null) {

      Status status = this.passwordPolicy.check(password);
      if (status != Status.OK) {
        return ResetPasswordResponse.WEAK_PASSWORD;
      }

      long userId = record.get(APP_USER.ID);
      LocalDateTime tokenCreated = record.get(APP_USER.PASSWORD_RESET_TOKEN_CREATED);

      if (tokenCreated != null && tokenCreated.isAfter(
          LocalDateTime.now().minus(this.appProperties.getPasswordResetTokenMaxAge()))) {
        this.dsl.update(APP_USER).set(APP_USER.PASSWORD_RESET_TOKEN, (String) null)
            .set(APP_USER.PASSWORD_RESET_TOKEN_CREATED, (LocalDateTime) null)
            .set(APP_USER.PASSWORD_HASH, this.passwordEncoder.encode(password))
            .where(APP_USER.ID.equal(userId)).execute();
        return null;
      }

      this.dsl.update(APP_USER).set(APP_USER.PASSWORD_RESET_TOKEN, (String) null)
          .set(APP_USER.PASSWORD_RESET_TOKEN_CREATED, (LocalDateTime) null)
          .where(APP_USER.ID.equal(userId)).execute();
    }

    return ResetPasswordResponse.INVALID;
  }

  @PostMapping("/invalidate-sessions")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void invalidateSessions(@RequestBody @NotEmpty Set<String> sessions) {
    this.dsl.delete(APP_SESSION).where(APP_SESSION.ID.in(sessions)).execute();
  }

}