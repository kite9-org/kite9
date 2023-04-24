package com.kite9.server.persistence.github.config;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.kohsuke.github.GHTreeEntry;

public class Config implements Predicate<GHTreeEntry> {

	static final String KITE9_UPLOADS = ".kite9/uploads";
	
	public static final String DEFAULT_TEMPLATE_DIR = "/public/examples";
	
	private String uploads = KITE9_UPLOADS;
	private List<Source> sources = Collections.singletonList(new Source());
	private String templates = DEFAULT_TEMPLATE_DIR;
	
	public Config() {
		super();
	}

	public Config(String uploads, List<Source> sources, String templates) {
		super();
		this.uploads = uploads;
		this.sources = sources;
		this.templates = templates;
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
	
	public String getTemplates() {
		return templates;
	}

	public void setTemplates(String templates) {
		this.templates = templates;
	}

	
}

