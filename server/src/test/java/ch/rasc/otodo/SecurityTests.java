package ch.rasc.otodo;

import static ch.rasc.otodo.db.tables.AppSession.APP_SESSION;
import static ch.rasc.otodo.db.tables.AppUser.APP_USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import ch.rasc.otodo.config.security.AuthHeaderFilter;
import ch.rasc.otodo.db.tables.records.AppUserRecord;

@SuppressWarnings("null")
class SecurityTests extends AbstractEmailTest {

  @ParameterizedTest
  @ValueSource(strings = { "/syncview", "/sync", "/change-password", "/delete-account",
      "/change-email", "/sessions", "/delete-session", "/invalidate-sessions", "/users",
      "/unlock", "/activate", "/delete", "/enable", "/disable" })
  void testReturns401WhenNotLoggedIn(String url) {
    var response = getRestTemplate().getForEntity(url, Object.class);
    assertThat(response.getStatusCode().value()).isEqualTo(401);
  }

  @Test
  void testAuthenticate() {
    ResponseEntity<String> response = getRestTemplate().getForEntity("/be/authenticate",
        String.class);
    assertThat(response.getBody()).isNull();
    assertThat(response.getStatusCode().value()).isEqualTo(401);

    String token = getUtilService().sendLogin("admin@test.com", "password", 200, "ADMIN");

    HttpHeaders headers = new HttpHeaders();
    headers.set(AuthHeaderFilter.HEADER_NAME, token);
    var request = new HttpEntity<>(headers);
    response = getRestTemplate().exchange("/be/authenticate", HttpMethod.GET, request,
        String.class);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isEqualTo("ADMIN");

    getUtilService().sendLogout(token, 200);

    response = getRestTemplate().getForEntity("/be/authenticate", String.class);
    assertThat(response.getBody()).isNull();
    assertThat(response.getStatusCode().value()).isEqualTo(401);
  }

  @Test
  void testLogin() {
    getDsl().delete(APP_SESSION).execute();

    String token = getUtilService().sendLogin("admin@test.com", "password", 200, "ADMIN");

    long userId = getUtilService().getUserId("admin@test.com");
    assertThat(getDsl().selectCount().from(APP_SESSION).fetchOne().get(0)).isEqualTo(1);
    assertThat(getDsl().selectCount().from(APP_SESSION)
        .where(APP_SESSION.APP_USER_ID.eq(userId).and(APP_SESSION.ID.eq(token)))
        .fetchOne().get(0)).isEqualTo(1);

    getUtilService().sendLogout(token, 200);
    assertThat(getDsl().selectCount().from(APP_SESSION).fetchOne().get(0)).isEqualTo(0);
  }

  @Test
  void testLoginLockWorkflow() {
    getDsl().update(APP_USER).set(APP_USER.FAILED_LOGINS, (Integer) null)
        .set(APP_USER.LOCKED_OUT, (LocalDateTime) null).execute();

    // failed logins
    for (int i = 1; i < 10; i++) {
      getUtilService().sendLogin("admin@test.com", "passwordWRONG", 401, null);
      AppUserRecord record = getUtilService().getUser("admin@test.com");
      assertThat(record.getFailedLogins()).isEqualTo(i);
      assertThat(record.getLockedOut()).isNull();
      assertThat(getDsl().selectCount().from(APP_SESSION).fetchOne().get(0)).isEqualTo(0);
    }

    getUtilService().sendLogin("admin@test.com", "passwordWRONG", 401, null);
    AppUserRecord record = getUtilService().getUser("admin@test.com");
    assertThat(record.getFailedLogins()).isEqualTo(10);
    assertThat(record.getLockedOut()).isNotNull();
    assertThat(getDsl().selectCount().from(APP_SESSION).fetchOne().get(0)).isEqualTo(0);

    // test with correct password
    getUtilService().sendLogin("admin@test.com", "password", 401, null);
    record = getUtilService().getUser("admin@test.com");
    assertThat(record.getFailedLogins()).isEqualTo(10);
    assertThat(record.getLockedOut()).isNotNull();
    assertThat(getDsl().selectCount().from(APP_SESSION).fetchOne().get(0)).isEqualTo(0);

    String userToken = getUtilService().sendLogin("user@test.com", "password", 200,
        "USER");
    getUtilService().sendLogout(userToken, 200);

    getDsl().update(APP_USER)
        .set(APP_USER.LOCKED_OUT,
            record.getLockedOut().minus(getAppProperties().getLoginLockDuration()))
        .where(APP_USER.ID.eq(record.getId())).execute();

    getUtilService().sendLogin("admin@test.com", "password", 200, "ADMIN");
    record = getUtilService().getUser("admin@test.com");
    assertThat(record.getFailedLogins()).isNull();
    assertThat(record.getLockedOut()).isNull();
    assertThat(getDsl().selectCount().from(APP_SESSION).fetchOne().get(0)).isEqualTo(1);
  }

}
