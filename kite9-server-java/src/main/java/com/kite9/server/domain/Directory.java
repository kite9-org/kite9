package com.kite9.server.domain;

public abstract class Directory extends Content implements HasContents {

	@Override
	public String getIcon() {
		return "/public/templates/admin/icons/folder.svg";
	}

	@Override
	public String getType() {
		return "directory";
	}

	@Override
	public String getCommands() {
		return "focus NewDocument";
	}

}
