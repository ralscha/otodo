package ch.rasc.otodo.config.security;

import static ch.rasc.otodo.db.tables.AppUser.APP_USER;

import java.time.LocalDateTime;

import org.jooq.DSLContext;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
class UserAuthenticationSuccessHandler
    implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

  private final DSLContext dsl;

  UserAuthenticationSuccessHandler(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
    Object principal = event.getAuthentication().getPrincipal();
    if (principal instanceof JooqUserDetails) {

      Long id = ((JooqUserDetails) principal).getUserDbId();

      this.dsl.update(APP_USER).set(APP_USER.LOCKED_OUT, (LocalDateTime) null)
          .set(APP_USER.FAILED_LOGINS, (Integer) null).where(APP_USER.ID.eq(id))
          .execute();
    }
  }
}