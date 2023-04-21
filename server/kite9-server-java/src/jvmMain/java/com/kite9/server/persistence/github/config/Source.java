package com.kite9.server.persistence.github.config;

import java.util.function.Predicate;

import org.kohsuke.github.GHTreeEntry;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class Source implements Predicate<GHTreeEntry> {

	private String path = "/";
	private String template;
	private String pattern= "*.adl";
	private boolean showDirectories = false;
	
	public Source() {
		super();
	}

	public Source(String path, String template, String pattern) {
		super();
		this.path = path;
		this.template = template;
		this.pattern = pattern;
	}
	
	static boolean isDir(GHTreeEntry te) {
		return "tree".equals(te.getType());
	}
	
	private PathMatcher matcher;

	@Override
	public boolean test(GHTreeEntry te) {
		if (matcher == null) {
			matcher = new AntPathMatcher();
		}
		String path = AntPathMatcher.DEFAULT_PATH_SEPARATOR+te.getPath();
		return path.startsWith(this.path) && (isDir(te) ? 
				isShowDirectories() : 
				matcher.match(pattern, path.substring(this.path.length())));
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public boolean isShowDirectories() {
		return showDirectories;
	}

	public void setShowDirectories(boolean showDirectories) {
		this.showDirectories = showDirectories;
	}
	
}
