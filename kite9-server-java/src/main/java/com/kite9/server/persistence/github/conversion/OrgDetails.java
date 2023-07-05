package com.kite9.server.persistence.github.conversion;

import java.util.List;

import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;

import com.kite9.server.persistence.github.urls.Kite9GithubPath;

public class OrgDetails {

	private final Kite9GithubPath path;
	private final List<GHRepository> repoList;
	private final GHPerson userOrOrg;
	
	public OrgDetails(Kite9GithubPath path, List<GHRepository> repoList, GHPerson userOrOrg) {
		super();
		this.path = path;
		this.repoList = repoList;
		this.userOrOrg = userOrOrg;
	}

	public Kite9GithubPath getPath() {
		return path;
	}

	public List<GHRepository> getRepoList() {
		return repoList;
	}

	public GHPerson getUserOrOrg() {
		return userOrOrg;
	}

}
