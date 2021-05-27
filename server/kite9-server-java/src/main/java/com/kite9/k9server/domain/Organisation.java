package com.kite9.k9server.domain;

import java.util.Collections;
import java.util.List;

public abstract class Organisation extends RestEntity {

	@Override
	public List<RestEntity> getParents() {
		return Collections.emptyList();
	}

	@Override
	public String getType() {
		return "organisation";
	}

	@Override
	public String getCommands() {
		return "focus";
	}
	
	public abstract List<Repository> getRepositories();
	
}
