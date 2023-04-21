package com.kite9.server.persistence.github.config;

import java.util.Objects;
import java.util.function.Predicate;

import org.kohsuke.github.GHTreeEntry;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class Source implements Predicate<GHTreeEntry> {

	private String path = "/";
	private String pattern= "*.adl";
	private boolean showDirectories = false;
	
	public Source() {
		super();
	}

	public Source(String path, String pattern) {
		super();
		this.path = path;
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

	@Override
	public int hashCode() {
		return Objects.hash(path, pattern, showDirectories);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Source other = (Source) obj;
		return Objects.equals(path, other.path) && Objects.equals(pattern, other.pattern)
				&& showDirectories == other.showDirectories;
	}

	@Override
	public String toString() {
		return "Source [path=" + path + ", pattern=" + pattern + ", showDirectories=" + showDirectories + "]";
	}
	
}
