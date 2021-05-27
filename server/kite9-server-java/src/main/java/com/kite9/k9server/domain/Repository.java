package com.kite9.k9server.domain;

public abstract class Repository extends Directory {
	
	@Override
	public String getIcon() {
		return "/public/templates/admin/icons/project.svg";
	}

	@Override
	public String getType() {
		return "repository";
	}

	@Override
	public String getCommands() {
		return "focus NewDocument";
	}

	
}
