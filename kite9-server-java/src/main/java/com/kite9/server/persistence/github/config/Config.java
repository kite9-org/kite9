package com.kite9.server.persistence.github.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHTreeEntry;

import com.kite9.pipeline.adl.holder.meta.CreateConfig;

public class Config implements Predicate<GHTreeEntry>, CreateConfig {

	static final String KITE9_UPLOADS = ".kite9/uploads";
	
	public static final String DEFAULT_TEMPLATE_DIR = "/public/examples";
	
	private String uploads = KITE9_UPLOADS;
	private List<Source> sources = Collections.singletonList(new Source());
	private String templatePath = DEFAULT_TEMPLATE_DIR;

	private String defaultFormat = "adl";

	private List<String> allowedFormats = Arrays.asList("adl", "png", "svg");

	private List<String> templates = null;
	
	public Config() {
		super();
	}

	@Override
	public boolean test(GHTreeEntry te) {
		return sources.stream()
			.filter(s -> s.test(te))
			.count() > 0;
	}

	public String getUploadsPath() {
		return uploads;
	}

	public void setUploadsPath(String uploads) {
		this.uploads = uploads;
	}

	public List<Source> getSources() {
		return sources;
	}

	public void setSources(List<Source> sources) {
		this.sources = sources;
	}
	
	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public List<String> getTemplates() {
		return templates;
	}

	public void setTemplates(List<String> templates) {
		this.templates = templates;
	}

	public String getDefaultFormat() {
		return defaultFormat;
	}

	public void setDefaultFormat(@NotNull String defaultFormat) {
		this.defaultFormat = defaultFormat;
	}

	public List<String> getAllowedFormats() {
		return allowedFormats;
	}

	public void setAllowedFormats(@NotNull List<String> allowedFormats) {
		this.allowedFormats = allowedFormats;
	}
}

