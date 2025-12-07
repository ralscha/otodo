package ch.rasc.otodo.controller;

import static ch.rasc.otodo.db.tables.Todo.TODO;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.DSLContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rasc.otodo.config.security.AppUserDetail;
import ch.rasc.otodo.dto.Todo;
import ch.rasc.otodo.dto.TodoSyncRequest;
import ch.rasc.otodo.dto.TodoSyncResponse;
import ch.rasc.otodo.dto.TodoSyncResponse.NewId;

@RestController
@RequestMapping("/be")
class TodoController {

	private final DSLContext dsl;

	public TodoController(DSLContext dsl) {
		this.dsl = dsl;
	}

	@GetMapping("syncview")
	public Map<Long, Long> getSyncView(@AuthenticationPrincipal AppUserDetail userDetails) {

		Map<Long, Long> result = new HashMap<>();

		this.dsl.select(TODO.ID, TODO.UPDATED)
			.from(TODO)
			.where(TODO.APP_USER_ID.eq(userDetails.getAppUserId()))
			.fetch()
			.forEach(record -> result.put(record.get(TODO.ID), record.get(TODO.UPDATED).toEpochSecond(ZoneOffset.UTC)));

		return result;
	}

	@PostMapping("sync")
	public TodoSyncResponse sync(@RequestBody TodoSyncRequest sync,
			@AuthenticationPrincipal AppUserDetail userDetails) {
		Set<Long> removed = null;
		Map<Long, Long> updated = null;
		Map<Long, NewId> inserted = null;
		List<Todo> get = new ArrayList<>();

		long loggedInUserId = userDetails.getAppUserId();

		// delete
		if (sync.getRemoved() != null && !sync.getRemoved().isEmpty()) {
			removed = new HashSet<>();
			for (Long id : sync.getRemoved()) {
				int noOfDeleted = this.dsl.delete(TODO)
					.where(TODO.ID.eq(id).and(TODO.APP_USER_ID.eq(loggedInUserId)))
					.execute();
				if (noOfDeleted == 1) {
					removed.add(id);
				}
			}
		}

		// update
		if (sync.getUpdated() != null && !sync.getUpdated().isEmpty()) {
			updated = new HashMap<>();
			for (Todo clientTodo : sync.getUpdated()) {
				var record = this.dsl.select(TODO.ID, TODO.UPDATED, TODO.SUBJECT, TODO.DESCRIPTION)
					.from(TODO)
					.where(TODO.ID.eq(clientTodo.getId()).and(TODO.APP_USER_ID.eq(loggedInUserId)))
					.fetchOne();

				if (record != null) {
					if (record.get(TODO.UPDATED).toEpochSecond(ZoneOffset.UTC) > clientTodo.getTs()) {
						// db todo is newer than the version sent from the client. ignore
						// client
						// update
						get.add(new Todo(record.get(TODO.ID), record.get(TODO.UPDATED).toEpochSecond(ZoneOffset.UTC),
								record.get(TODO.SUBJECT), record.get(TODO.DESCRIPTION)));
					}
					else {
						LocalDateTime now = LocalDateTime.now();
						int noOfUpdated = this.dsl.update(TODO)
							.set(TODO.SUBJECT, clientTodo.getSubject())
							.set(TODO.DESCRIPTION, clientTodo.getDescription())
							.set(TODO.UPDATED, now)
							.where(TODO.ID.eq(clientTodo.getId()).and(TODO.APP_USER_ID.eq(loggedInUserId)))
							.execute();
						if (noOfUpdated == 1) {
							updated.put(clientTodo.getId(), now.toEpochSecond(ZoneOffset.UTC));
						}
					}
				}
			}
		}

		// insert
		if (sync.getInserted() != null && !sync.getInserted().isEmpty()) {
			inserted = new HashMap<>();
			for (Todo clientTodo : sync.getInserted()) {
				LocalDateTime now = LocalDateTime.now();

				long id = this.dsl.insertInto(TODO, TODO.SUBJECT, TODO.DESCRIPTION, TODO.UPDATED, TODO.APP_USER_ID)
					.values(clientTodo.getSubject(), clientTodo.getDescription(), now, loggedInUserId)
					.returning(TODO.ID)
					.fetchOne()
					.getId();

				inserted.put(clientTodo.getId(), new NewId(id, now.toEpochSecond(ZoneOffset.UTC)));
			}
		}

		// gets
		if (sync.getGets() != null) {
			for (Long id : sync.getGets()) {
				var record = this.dsl.select(TODO.ID, TODO.UPDATED, TODO.SUBJECT, TODO.DESCRIPTION)
					.from(TODO)
					.where(TODO.ID.eq(id).and(TODO.APP_USER_ID.eq(loggedInUserId)))
					.fetchOne();

				if (record != null) {
					get.add(new Todo(record.get(TODO.ID), record.get(TODO.UPDATED).toEpochSecond(ZoneOffset.UTC),
							record.get(TODO.SUBJECT), record.get(TODO.DESCRIPTION)));
				}
			}
		}

		if (get.isEmpty()) {
			get = null;
		}
		if (removed != null && removed.isEmpty()) {
			removed = null;
		}
		if (updated != null && updated.isEmpty()) {
			updated = null;
		}

		return new TodoSyncResponse(get, inserted, updated, removed);
	}

}
