package ch.rasc.otodo.controller;

import static ch.rasc.otodo.db.tables.AppUser.APP_USER;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import org.hashids.Hashids;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.rasc.otodo.config.AppProperties;
import ch.rasc.otodo.dto.User;

@RestController
@Validated
@RequestMapping("/be/admin")
class AdminController {

  private final DSLContext dsl;

  private final Hashids hashids;

  private final Duration loginLockDuration;

  public AdminController(DSLContext dsl, AppProperties appProperties) {
    this.dsl = dsl;
    this.hashids = new Hashids();
    this.loginLockDuration = appProperties.getLoginLockDuration();
  }

  @GetMapping("/users")
  public List<User> fetchUsers() {
    var result = this.dsl.selectFrom(APP_USER).fetch();
    return result.stream().map(
        user -> new User(this.hashids.encode(user.getId()), user, this.loginLockDuration))
        .collect(Collectors.toList());
  }

  @PostMapping("/unlock")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlock(@RequestBody @NotEmpty String hashedId) {
    long userId = this.hashids.decode(hashedId)[0];
    this.dsl.update(APP_USER).set(APP_USER.FAILED_LOGINS, (Integer) null)
        .set(APP_USER.LOCKED_OUT, (LocalDateTime) null).where(APP_USER.ID.eq(userId))
        .execute();
  }

  @PostMapping("/activate")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void activate(@RequestBody @NotEmpty String hashedId) {
    long userId = this.hashids.decode(hashedId)[0];
    this.dsl.update(APP_USER).set(APP_USER.EXPIRED, (LocalDateTime) null)
        .where(APP_USER.ID.eq(userId)).execute();
  }

  @PostMapping("/delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@RequestBody @NotEmpty String hashedId) {
    long userId = this.hashids.decode(hashedId)[0];
    this.dsl.delete(APP_USER).where(APP_USER.ID.eq(userId)).execute();
  }

  @PostMapping("/enable")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void enable(@RequestBody @NotEmpty String hashedId) {
    long userId = this.hashids.decode(hashedId)[0];
    this.dsl.update(APP_USER).set(APP_USER.ENABLED, true).where(APP_USER.ID.eq(userId))
        .execute();
  }

  @PostMapping("/disable")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void disable(@RequestBody @NotEmpty String hashedId) {
    long userId = this.hashids.decode(hashedId)[0];
    this.dsl.update(APP_USER).set(APP_USER.ENABLED, false).where(APP_USER.ID.eq(userId))
        .execute();
  }

}
