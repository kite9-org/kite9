package com.kite9.server.domain;

import java.util.Collections;
import java.util.List;

public abstract class User extends RestEntity {
	
	@Override
	public List<RestEntity> getParents() {
		return Collections.emptyList();
	}

	@Override
	public String getType() {
		return "user";
	}

	public abstract List<Organisation> getOrganisations();
	
	public abstract List<Repository> getRepositories();

	@Override
	public String getCommands() {
		return "focus";
	}
	
}
