package ch.rasc.otodo;

import static ch.rasc.otodo.db.tables.AppUser.APP_USER;
import static ch.rasc.otodo.db.tables.Todo.TODO;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import ch.rasc.otodo.config.security.AuthHeaderFilter;
import ch.rasc.otodo.db.tables.records.AppUserRecord;
import ch.rasc.otodo.db.tables.records.TodoRecord;
import ch.rasc.otodo.dto.Todo;
import ch.rasc.otodo.dto.TodoSyncRequest;
import ch.rasc.otodo.dto.TodoSyncResponse;

@SuppressWarnings({ "static-method" })
class TodoServiceTest extends AbstractBaseTest {

	@Test
	void testUserIsolation() {
		insertAnotherUser();

		AppUserRecord user1 = getUtilService().getUser("user@test.com");
		AppUserRecord user2 = getUtilService().getUser("another@test.com");

		Todo newTodo1 = new Todo(-1, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), "subject1", "descr1");
		TodoSyncRequest sync1 = new TodoSyncRequest(List.of(newTodo1), null, null, null);

		Todo newTodo2 = new Todo(-1, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), "subject2", "descr2");
		TodoSyncRequest sync2 = new TodoSyncRequest(List.of(newTodo2), null, null, null);

		String token1 = getUtilService().sendLogin(user1.getEmail(), "password", 200, "USER");
		String token2 = getUtilService().sendLogin(user2.getEmail(), "another", 200, "USER");

		insertTodo(user1, sync1, token1);
		insertTodo(user2, sync2, token2);

		fetchSyncViewAndCompare(user1, token1);
		fetchSyncViewAndCompare(user2, token2);

		var records = getDsl().selectFrom(TODO).fetch();
		TodoSyncRequest getAllSync = new TodoSyncRequest(null, null, null,
				records.stream().map(TodoRecord::getId).collect(Collectors.toSet()));
		var response1 = sendSyncRequest(getAllSync, token1);
		var response2 = sendSyncRequest(getAllSync, token2);

		List<Todo> user1Todos = response1.getBody().getGets();
		List<Todo> user2Todos = response2.getBody().getGets();

		var records1 = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user1.getId())).fetchMap(TODO.ID);
		var records2 = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user2.getId())).fetchMap(TODO.ID);

		compareTodos(user1Todos, records1);
		compareTodos(user2Todos, records2);

		// try to remove todo from other user
		TodoSyncRequest removeSync = new TodoSyncRequest(null, null, records2.keySet(), null);
		ResponseEntity<TodoSyncResponse> response = sendSyncRequest(removeSync, token1);
		TodoSyncResponse syncResponse = response.getBody();
		assertThat(syncResponse.getGets()).isNull();
		assertThat(syncResponse.getUpdated()).isNull();
		assertThat(syncResponse.getRemoved()).isNull();
		assertThat(syncResponse.getInserted()).isNull();
		int count = getDsl().selectCount().from(TODO).where(TODO.APP_USER_ID.eq(user2.getId())).fetchOne(0, int.class);
		assertThat(count).isEqualTo(1);

		// try to update todo from other user
		Long todo2Id = records2.keySet().iterator().next();
		Todo updatedTodo = new Todo(todo2Id, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), "newSubject",
				"newDescription");
		TodoSyncRequest updateSync = new TodoSyncRequest(null, List.of(updatedTodo), null, null);
		response = sendSyncRequest(updateSync, token1);
		syncResponse = response.getBody();
		assertThat(syncResponse.getGets()).isNull();
		assertThat(syncResponse.getUpdated()).isNull();
		assertThat(syncResponse.getRemoved()).isNull();
		assertThat(syncResponse.getInserted()).isNull();
		TodoRecord todoFromDb = getDsl().selectFrom(TODO)
			.where(TODO.APP_USER_ID.eq(user2.getId()).and(TODO.ID.eq(todo2Id)))
			.fetchOne();
		TodoRecord prevTodo = records2.get(todo2Id);
		assertThat(todoFromDb).usingRecursiveComparison().isEqualTo(prevTodo);
	}

	void testUpdate() {
		getDsl().delete(TODO).execute();
		AppUserRecord user = getUtilService().getUser("user@test.com");
		String token = getUtilService().sendLogin(user.getEmail(), "password", 200, "USER");

		// insert

		long currentTs = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

		Todo newTodo = new Todo(-1, currentTs, "newSubject", "newDescription");
		TodoSyncRequest insertSync = new TodoSyncRequest(List.of(newTodo), null, null, null);
		insertTodo(user, insertSync, token);

		TodoRecord todoRecord = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user.getId())).fetchOne();
		assertThat(todoRecord).isNotNull();
		assertThat(todoRecord.getSubject()).isEqualTo("newSubject");
		assertThat(todoRecord.getDescription()).isEqualTo("newDescription");

		// update

		Todo updatedTodo = new Todo(todoRecord.getId(), currentTs, "anotherNewSubject", "anotherNewDescription");
		TodoSyncRequest updateSync = new TodoSyncRequest(null, List.of(updatedTodo), null, null);

		ResponseEntity<TodoSyncResponse> response = sendSyncRequest(updateSync, token);

		TodoRecord updatedTodoRecord = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user.getId())).fetchOne();
		assertThat(updatedTodoRecord).isNotNull();
		assertThat(updatedTodoRecord.getId()).isEqualTo(todoRecord.getId());
		assertThat(updatedTodoRecord.getSubject()).isEqualTo("anotherNewSubject");
		assertThat(updatedTodoRecord.getDescription()).isEqualTo("anotherNewDescription");

		TodoSyncResponse syncResponse = response.getBody();
		assertThat(syncResponse.getGets()).isNull();
		assertThat(syncResponse.getUpdated()).hasSize(1);
		assertThat(syncResponse.getRemoved()).isNull();
		assertThat(syncResponse.getInserted()).isNull();
		assertThat(syncResponse.getUpdated().get(todoRecord.getId()))
			.isEqualTo(updatedTodoRecord.getUpdated().toEpochSecond(ZoneOffset.UTC));
	}

	void testRemove() {
		getDsl().delete(TODO).execute();
		AppUserRecord user = getUtilService().getUser("user@test.com");
		String token = getUtilService().sendLogin(user.getEmail(), "password", 200, "USER");

		// insert
		long currentTs = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

		Todo newTodo = new Todo(-1, currentTs, "newSubject", "newDescription");
		TodoSyncRequest insertSync = new TodoSyncRequest(List.of(newTodo), null, null, null);
		insertTodo(user, insertSync, token);

		TodoRecord todoRecord = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user.getId())).fetchOne();
		assertThat(todoRecord).isNotNull();
		assertThat(todoRecord.getSubject()).isEqualTo("newSubject");
		assertThat(todoRecord.getDescription()).isEqualTo("newDescription");

		// remove
		TodoSyncRequest removeSync = new TodoSyncRequest(null, null, Set.of(todoRecord.getId()), null);
		ResponseEntity<TodoSyncResponse> response = sendSyncRequest(removeSync, token);

		TodoRecord record = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user.getId())).fetchOne();
		assertThat(record).isNull();

		TodoSyncResponse syncResponse = response.getBody();
		assertThat(syncResponse.getGets()).isNull();
		assertThat(syncResponse.getUpdated()).isNull();
		assertThat(syncResponse.getRemoved()).hasSize(1);
		assertThat(syncResponse.getInserted()).isNull();
		assertThat(syncResponse.getRemoved()).containsExactly(todoRecord.getId());
	}

	private void compareTodos(List<Todo> userTodos, Map<Long, TodoRecord> dbRecords) {
		assertThat(userTodos).hasSize(dbRecords.size());
		for (Todo todo : userTodos) {
			TodoRecord dbRecord = dbRecords.get(todo.getId());
			assertThat(dbRecord).isNotNull();
			assertThat(dbRecord.getId()).isEqualTo(todo.getId());
			assertThat(dbRecord.getSubject()).isEqualTo(todo.getSubject());
			assertThat(dbRecord.getDescription()).isEqualTo(todo.getDescription());
			assertThat(dbRecord.getUpdated().toEpochSecond(ZoneOffset.UTC)).isEqualTo(todo.getTs());
		}
	}

	private void fetchSyncViewAndCompare(AppUserRecord user, String authToken) {
		var headers = new HttpHeaders();
		headers.set(AuthHeaderFilter.HEADER_NAME, authToken);
		var request = new HttpEntity<>(headers);
		var typeRef = new ParameterizedTypeReference<Map<Long, Long>>() {
			// nothing_here
		};

		var response = getRestTemplate().exchange("/be/syncview", HttpMethod.GET, request, typeRef);
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		var sync = response.getBody();

		var records = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user.getId())).fetch();

		assertThat(sync).hasSize(records.size());

		for (TodoRecord record : records) {
			assertThat(sync.get(record.getId())).isEqualTo(record.getUpdated().toEpochSecond(ZoneOffset.UTC));
		}
	}

	private void insertTodo(AppUserRecord user, TodoSyncRequest sync, String token) {
		ResponseEntity<TodoSyncResponse> response = sendSyncRequest(sync, token);

		TodoRecord todoRecord = getDsl().selectFrom(TODO).where(TODO.APP_USER_ID.eq(user.getId())).fetchOne();
		assertThat(todoRecord).isNotNull();

		TodoSyncResponse syncResponse = response.getBody();
		assertThat(syncResponse.getGets()).isNull();
		assertThat(syncResponse.getUpdated()).isNull();
		assertThat(syncResponse.getRemoved()).isNull();
		assertThat(syncResponse.getInserted()).hasSize(1);
		assertThat(syncResponse.getInserted().get(-1L).getId()).isEqualTo(todoRecord.getId());
		assertThat(syncResponse.getInserted().get(-1L).getTs())
			.isEqualTo(todoRecord.getUpdated().toEpochSecond(ZoneOffset.UTC));
	}

	private ResponseEntity<TodoSyncResponse> sendSyncRequest(TodoSyncRequest sync, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(AuthHeaderFilter.HEADER_NAME, token);
		var request = new HttpEntity<>(sync, headers);
		ResponseEntity<TodoSyncResponse> response = getRestTemplate().postForEntity("/be/sync", request,
				TodoSyncResponse.class);
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		return response;
	}

	private void insertAnotherUser() {
		getDsl().insertInto(APP_USER, APP_USER.EMAIL, APP_USER.PASSWORD_HASH, APP_USER.AUTHORITY, APP_USER.ENABLED,
				APP_USER.EXPIRED, APP_USER.LAST_ACCESS, APP_USER.PASSWORD_RESET_TOKEN,
				APP_USER.PASSWORD_RESET_TOKEN_CREATED, APP_USER.CONFIRMATION_TOKEN, APP_USER.CONFIRMATION_TOKEN_CREATED)
			.values("another@test.com", getPasswordEncoder().encode("another"), "USER", true, null, null, null, null,
					null, null)
			.execute();
	}

}
