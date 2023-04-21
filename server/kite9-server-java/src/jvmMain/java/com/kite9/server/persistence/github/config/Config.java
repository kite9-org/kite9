package com.kite9.server.persistence.github.config;

import java.util.List;
import java.util.function.Predicate;

import org.kohsuke.github.GHTreeEntry;

public class Config implements Predicate<GHTreeEntry> {

	private String uploads;
	private List<Source> sources;
	
	public Config() {
		super();
	}

	public Config(String uploads, List<Source> sources) {
		super();
		this.uploads = uploads;
		this.sources = sources;
	}

	@Override
	public boolean test(GHTreeEntry te) {
		return sources.stream()
			.filter(s -> s.test(te))
			.count() > 0;
	}

	public String getUploads() {
		return uploads;
	}

	public void setUploads(String uploads) {
		this.uploads = uploads;
	}

	public List<Source> getSources() {
		return sources;
	}

	public void setSources(List<Source> sources) {
		this.sources = sources;
	}
}

