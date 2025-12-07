package ch.rasc.otodo.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Todo {

	private final long id;

	private final long ts;

	private final String subject;

	private final String description;

	@JsonCreator
	public Todo(@JsonProperty("id") long id, @JsonProperty("ts") long ts, @JsonProperty("subject") String subject,
			@JsonProperty("description") String description) {
		this.id = id;
		this.ts = ts;
		this.subject = subject;
		this.description = description;
	}

	public long getId() {
		return this.id;
	}

	public long getTs() {
		return this.ts;
	}

	public String getSubject() {
		return this.subject;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return "TodoDto [id=" + this.id + ", ts=" + this.ts + ", subject=" + this.subject + ", description="
				+ this.description + "]";
	}

}
