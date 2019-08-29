package ch.rasc.otodo.config.security;

import static ch.rasc.otodo.db.tables.AppSession.APP_SESSION;
import static ch.rasc.otodo.db.tables.AppUser.APP_USER;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import ch.rasc.otodo.db.tables.records.AppSessionRecord;
import ch.rasc.otodo.service.TokenService;

@Component
class AppLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final TokenService tokenService;

  private final DSLContext dsl;

  public AppLoginSuccessHandler(DSLContext dsl, TokenService tokenService) {
    this.dsl = dsl;
    this.tokenService = tokenService;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication) throws IOException {

    JooqUserDetails userDetail = (JooqUserDetails) authentication.getPrincipal();
    String sessionId = this.tokenService.createToken();

    this.dsl.transaction(txConf -> {
      try (var txdsl = DSL.using(txConf)) {
        LocalDateTime now = LocalDateTime.now();

        String ua = request.getHeader("user-agent");
        if (ua != null) {
          ua = ua.substring(0, Math.min(255, ua.length()));
        }

        AppSessionRecord record = this.dsl.newRecord(APP_SESSION);
        record.setId(sessionId);
        record.setAppUserId(userDetail.getUserDbId());
        record.setLastAccess(now);
        record.setIp(request.getRemoteAddr());
        record.setUserAgent(ua);
        record.store();

        this.dsl.update(APP_USER).set(APP_USER.LAST_ACCESS, now)
            .where(APP_USER.ID.eq(userDetail.getUserDbId())).execute();
      }
    });

    response.setStatus(HttpServletResponse.SC_OK);

    response.addHeader(AuthHeaderFilter.HEADER_NAME, sessionId);

    response.getWriter().print(userDetail.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
  }

}