package com.kite9.k9server.domain;

public abstract class Document extends RestEntity {

	@Override
	public String getCommands() {
		return "open";
	}

	@Override
	public String getType() {
		return "document";
	}

}
