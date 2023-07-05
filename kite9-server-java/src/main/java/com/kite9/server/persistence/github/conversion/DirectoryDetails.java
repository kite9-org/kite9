package com.kite9.server.persistence.github.conversion;

import java.util.List;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeEntry;

import com.kite9.server.persistence.github.config.Config;
import com.kite9.server.persistence.github.urls.Kite9GithubPath;

public class DirectoryDetails {

	private final Kite9GithubPath path;
	private final GHRepository repo;
	private final List<GHTreeEntry> entries;
	private final Config config;
	
	public DirectoryDetails(GHRepository repo, List<GHTreeEntry> entries, Kite9GithubPath path, Config config) {
		super();
		this.repo = repo;
		this.entries = entries;
		this.path = path;
		this.config = config;
	}

	public GHRepository getRepo() {
		return repo;
	}

	public List<GHTreeEntry> getEntries() {
		return entries;
	}

	public Kite9GithubPath getPath() {
		return path;
	}

	public Config getConfig() {
		return config;
	}


}
