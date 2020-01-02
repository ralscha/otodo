package ch.rasc.otodo.config.security;

import static ch.rasc.otodo.db.tables.AppSession.APP_SESSION;
import static ch.rasc.otodo.db.tables.AppUser.APP_USER;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jooq.DSLContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import ch.rasc.otodo.config.AppProperties;

public class AuthHeaderFilter extends GenericFilterBean {

  public final static String HEADER_NAME = "x-authentication";

  private final DSLContext dsl;

  private final AppProperties appProperties;

  private final ConcurrentLinkedQueue<Ids> updateLastAccessQueue;

  public AuthHeaderFilter(DSLContext dsl, AppProperties appProperties) {
    this.dsl = dsl;
    this.appProperties = appProperties;
    this.updateLastAccessQueue = new ConcurrentLinkedQueue<>();
  }

  void updateLastAccess() {
    if (!this.updateLastAccessQueue.isEmpty()) {
      Set<String> appSessionIds = new HashSet<>();
      Set<Long> appUserIds = new HashSet<>();

      Ids id;
      while ((id = this.updateLastAccessQueue.poll()) != null) {
        appSessionIds.add(id.getAppSessionId());
        appUserIds.add(id.getAppUserId());
      }

      LocalDateTime now = LocalDateTime.now();

      this.dsl.update(APP_SESSION).set(APP_SESSION.LAST_ACCESS, now)
          .where(APP_SESSION.ID.in(appSessionIds)).execute();

      this.dsl.update(APP_USER).set(APP_USER.LAST_ACCESS, now)
          .where(APP_USER.ID.in(appUserIds)).execute();
    }
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    String sessionId = httpServletRequest.getHeader(AuthHeaderFilter.HEADER_NAME);

    if (sessionId != null) {
      var sessionRecord = this.dsl
          .select(APP_SESSION.ID, APP_SESSION.APP_USER_ID, APP_SESSION.LAST_ACCESS)
          .from(APP_SESSION).where(APP_SESSION.ID.eq(sessionId)).fetchOne();

      if (sessionRecord != null) {
        LocalDateTime lastAccess = sessionRecord.get(APP_SESSION.LAST_ACCESS);

        if (lastAccess.plus(this.appProperties.getInactiveSessionMaxAge())
            .isAfter(LocalDateTime.now())) {

          Long appUserId = sessionRecord.get(APP_SESSION.APP_USER_ID);
          var appUserRecord = this.dsl
              .select(APP_USER.ID, APP_USER.EMAIL, APP_USER.ENABLED, APP_USER.EXPIRED,
                  APP_USER.LOCKED_OUT, APP_USER.AUTHORITY)
              .from(APP_USER).where(APP_USER.ID.eq(appUserId)).fetchOne();

          if (appUserRecord != null) {
            Boolean enabled = appUserRecord.get(APP_USER.ENABLED);
            LocalDateTime expired = appUserRecord.get(APP_USER.EXPIRED);
            LocalDateTime lockedOut = appUserRecord.get(APP_USER.LOCKED_OUT);

            if (enabled.booleanValue() && expired == null && lockedOut == null) {
              AppUserDetail userDetail = new AppUserDetail(appUserRecord.get(APP_USER.ID),
                  appUserRecord.get(APP_USER.EMAIL),
                  appUserRecord.get(APP_USER.AUTHORITY));

              Ids updateIds = new Ids(appUserId, sessionRecord.get(APP_SESSION.ID));
              this.updateLastAccessQueue.add(updateIds);
              SecurityContextHolder.getContext()
                  .setAuthentication(new AppUserAuthentication(userDetail));
            }
          }
        }
        else {
          // session expired
          this.dsl.delete(APP_SESSION).where(APP_SESSION.ID.eq(sessionId)).execute();
        }
      }

    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  private static class Ids {
    final long appUserId;
    final String appSessionId;

    Ids(long appUserId, String appSessionId) {
      this.appUserId = appUserId;
      this.appSessionId = appSessionId;
    }

    long getAppUserId() {
      return this.appUserId;
    }

    String getAppSessionId() {
      return this.appSessionId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + (this.appSessionId == null ? 0 : this.appSessionId.hashCode());
      result = prime * result + (int) (this.appUserId ^ this.appUserId >>> 32);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Ids other = (Ids) obj;
      if (this.appSessionId == null) {
        if (other.appSessionId != null) {
          return false;
        }
      }
      else if (!this.appSessionId.equals(other.appSessionId)) {
        return false;
      }
      return this.appUserId == other.appUserId;
    }

  }
}