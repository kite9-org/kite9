package com.kite9.k9server.command;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is thrown when something goes wrong in command processing.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CommandException extends ResponseStatusException {

	private List<Command> c;
	
	public CommandException(HttpStatus status, String message, Throwable cause, Command c) {
		super(status, message, cause);
		this.c = Collections.singletonList(c);
	}
	
	public CommandException(HttpStatus status, String message, Throwable cause, List<Command> c) {
		super(status, message, cause);
		this.c = c;
	}

	public CommandException(HttpStatus status, String message) {
		super(status, message);
	}
	
	public CommandException(HttpStatus status, String message, Command c) {
		super(status, message);
		this.c = Collections.singletonList(c);
	}

	public CommandException(HttpStatus status, String message, List<Command> c) {
		super(status, message);
		this.c = c;
	}

	
	@Override
	public String getMessage() {
		try {
			return super.getMessage()+" - Command: "+new ObjectMapper().writeValueAsString(c);
		} catch (JsonProcessingException e) {
			return super.getMessage()+" - Command couldn't be serialized";
		}
	}

	
	
}
