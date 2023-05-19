package com.kite9.server.persistence.github.conversion;

import java.util.List;

import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;

import com.kite9.server.persistence.github.urls.Kite9GithubPath;

public class HomeDetails {

	private final Kite9GithubPath path;
	private final GHPerson user;
	private final List<GHRepository> repoList;
	private final List<GHOrganization> organisations;
	
	public HomeDetails(Kite9GithubPath path, List<GHRepository> repoList, List<GHOrganization> organisations, GHPerson user) {
		super();
		this.path = path;
		this.repoList = repoList;
		this.organisations = organisations;
		this.user = user;
	}

	public Kite9GithubPath getPath() {
		return path;
	}

	public List<GHRepository> getRepoList() {
		return repoList;
	}

	public List<GHOrganization> getOrganisations() {
		return organisations;
	}

	public GHPerson getUser() {
		return user;
	}

}
