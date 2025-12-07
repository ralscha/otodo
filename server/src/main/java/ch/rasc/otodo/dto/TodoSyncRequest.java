package ch.rasc.otodo.dto;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoSyncRequest {

	private final List<Todo> inserted;

	private final List<Todo> updated;

	private final Set<Long> removed;

	private final Set<Long> gets;

	@JsonCreator
	public TodoSyncRequest(@JsonProperty("inserted") List<Todo> inserted, @JsonProperty("updated") List<Todo> updated,
			@JsonProperty("removed") Set<Long> removed, @JsonProperty("gets") Set<Long> gets) {
		this.inserted = inserted != null ? List.copyOf(inserted) : null;
		this.updated = updated != null ? List.copyOf(updated) : null;
		this.removed = removed != null ? Set.copyOf(removed) : null;
		this.gets = gets != null ? Set.copyOf(gets) : null;
	}

	public List<Todo> getInserted() {
		return this.inserted;
	}

	public List<Todo> getUpdated() {
		return this.updated;
	}

	public Set<Long> getRemoved() {
		return this.removed;
	}

	public Set<Long> getGets() {
		return this.gets;
	}

}
