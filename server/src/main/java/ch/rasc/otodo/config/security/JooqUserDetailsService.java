package ch.rasc.otodo.config.security;

import static ch.rasc.otodo.db.tables.AppUser.APP_USER;

import java.time.Duration;

import org.jooq.DSLContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ch.rasc.otodo.config.AppProperties;

@Service
class JooqUserDetailsService implements UserDetailsService {

  private final DSLContext dsl;

  private final Duration loginLockDuration;

  public JooqUserDetailsService(DSLContext dsl, AppProperties appProperties) {
    this.dsl = dsl;
    this.loginLockDuration = appProperties.getLoginLockDuration();
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var appUserRecord = this.dsl
        .select(APP_USER.ID, APP_USER.EMAIL, APP_USER.PASSWORD_HASH, APP_USER.ENABLED,
            APP_USER.EXPIRED, APP_USER.LOCKED_OUT, APP_USER.AUTHORITY)
        .from(APP_USER).where(APP_USER.EMAIL.eq(email)).limit(1).fetchOne();

    if (appUserRecord != null) {
      return new JooqUserDetails(appUserRecord.get(APP_USER.ID),
          appUserRecord.get(APP_USER.EMAIL), appUserRecord.get(APP_USER.PASSWORD_HASH),
          appUserRecord.get(APP_USER.ENABLED), appUserRecord.get(APP_USER.EXPIRED),
          appUserRecord.get(APP_USER.LOCKED_OUT), appUserRecord.get(APP_USER.AUTHORITY),
          this.loginLockDuration);
    }
    throw new UsernameNotFoundException(email);
  }

}
