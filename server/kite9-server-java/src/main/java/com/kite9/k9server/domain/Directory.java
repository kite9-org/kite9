package com.kite9.k9server.domain;

import java.util.List;

public abstract class Directory extends RestEntity {

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

	public abstract List<Document> getDocuments();
	
	public abstract List<Directory> getSubDirectories();

}
