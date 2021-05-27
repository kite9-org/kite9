package com.kite9.k9server.update;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kite9.k9server.command.Command;

/**
 * Contains all the state required to perform a set of commands on some ADL xml.
 * 
 * @author robmoffat
 *
 */
public class Update {
	
	public enum Type {UNDO, REDO, NEW};

	public Update() {
		super();
	}
	
	public Update(List<Command> commands, URI uri, String base64adl, Type type) {
		super();
		this.uri = uri;
		this.commands = commands;
		this.base64adl = base64adl;
		this.type = type;
	}
	
	public Update(List<Command> commands, String base64adl, Type type) {
		super();
		this.commands = commands;
		this.base64adl = base64adl;
		this.type = type;
	}
	
	public Update(List<Command> commands, URI uri, Type type) {
		super();
		this.commands = commands;
		this.uri = uri;
		this.type = type;
	}
	
	private List<Command> commands;
	private String base64adl;
	private URI uri;
	private Type type = Type.NEW;

	@JsonIgnore
	private HttpHeaders headers = new HttpHeaders();

	public List<Command> getCommands() {
		return commands;
	}

	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}

	public String getBase64adl() {
		return base64adl;
	}

	public void setBase64adl(String base64adl) {
		this.base64adl = base64adl;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}
	
	public void addHeaders(HttpHeaders headers) {
		if (this.headers == null) {
			this.headers = headers;
		} else if (headers == null) {
			return;
		} else {
			this.headers.addAll(headers);
		}
	}

	public Type getType() {
		return type;
	}

	public void setType(Type undo) {
		this.type = undo;
	}

}
