package com.kite9.server.domain;

public abstract class Ref extends RestEntity implements HasContents {
	
	@Override
	public String getIcon() {
		return "/public/templates/admin/icons/project.svg";
	}

	@Override
	public String getType() {
		return "branch";
	}

	@Override
	public String getCommands() {
		return "focus NewDocument";
	}

	
}
