package ch.rasc.otodo.config.security;

import static ch.rasc.otodo.db.tables.AppSession.APP_SESSION;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
class AppLogoutSuccessHandler implements LogoutSuccessHandler {

  private final DSLContext dsl;

  public AppLogoutSuccessHandler(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    String sessionId = request.getHeader(AuthHeaderFilter.HEADER_NAME);
    if (sessionId != null) {
      this.dsl.delete(APP_SESSION).where(APP_SESSION.ID.eq(sessionId)).execute();
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }

}
