package com.kite9.server.persistence.github.config;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.kohsuke.github.GHTreeEntry;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class Source implements Predicate<GHTreeEntry> {

	private static final Predicate<String> HIDDEN_MATCHER = Pattern.compile("(\\/\\.|^\\.)").asPredicate();
	
	private String pattern= "*.adl";
	private boolean showDirectories = false;
	private boolean excludeHidden = true;

	public Source() {
		super();
	}

	public Source(String path, String pattern) {
		super();
		this.pattern = pattern;
	}
	
	static boolean isDir(GHTreeEntry te) {
		return "tree".equals(te.getType());
	}
	
	private transient PathMatcher matcher;

	@Override
	public boolean test(GHTreeEntry te) {
		if (matcher == null) {
			matcher = new AntPathMatcher();
		}
		String path = te.getPath();
		return checkHidden(path) && (isDir(te) ? 
				isShowDirectories() : 
				matcher.match(pattern, path));
	}

	private boolean checkHidden(String p) {
		return (isExcludeHidden()) ? !HIDDEN_MATCHER.test(p) : true;
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
	
	public boolean isExcludeHidden() {
		return excludeHidden;
	}

	public void setExcludeHidden(boolean excludeHidden) {
		this.excludeHidden = excludeHidden;
	}

	@Override
	public int hashCode() {
		return Objects.hash(excludeHidden, pattern, showDirectories);
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
		return excludeHidden == other.excludeHidden 
				&& Objects.equals(pattern, other.pattern) 
				&& showDirectories == other.showDirectories;
	}

	@Override
	public String toString() {
		return "Source [pattern=" + pattern + ", showDirectories=" + showDirectories
				+ ", excludeHidden=" + excludeHidden + "]";
	}
	
}
