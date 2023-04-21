package com.kite9.server.persistence.github.urls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the parsing and storage of the path parts related to a github repo given 
 * Kite9's mapping of URLs to Github content, which looks like this:
 * 
 * <pre>/github/&lt;owner&gt;/&lt;repo&gt;/&lt;branchref&gt;/&lt;path&gt;</pre>
 */
public class Kite9GithubPath {

	private static String pathPart(String contents) {
		return "(\\/([a-zA-Z0-9_-]+)"+contents+")?";
	}
	
	public static final Pattern p = Pattern.compile("^\\/([a-z]+)"+pathPart(pathPart("(\\/.*)?")));
	
	public static final int TYPE = 1;
	public static final int OWNER = 3;
	public static final int REPONAME = 5;
	public static final int FILEPATH = 6;
	
	public static Kite9GithubPath create(String from, String ref) {
		Matcher m = p.matcher(from);
		if (m.find() && ("github".equals(m.group(TYPE)))) {
			String path = m.group(FILEPATH);
			path = path == null ? "" : path;
			path = path.startsWith("/") ? path.substring(1) : path;
			String reponame = m.group(REPONAME);
			String owner = m.group(OWNER);
			return new Kite9GithubPath(owner, reponame, ref, path);
		} else {
			return null;
		}
	}
	
	public static boolean isGithubPath(String path) {
		return path.startsWith("/github");
	}
	
	private final String owner;
	private final String reponame;
	private final String filepath;
	private final String ref;

	public Kite9GithubPath(String owner, String reponame, String ref, String filepath) {
		super();
		this.owner = owner;
		this.reponame = reponame;
		this.filepath = filepath;
		this.ref = ref;
	}

	public String getOwner() {
		return owner;
	}

	public String getReponame() {
		return reponame;
	}
	
	public String getRef() {
		return ref;
	}

	public String getFilepath() {
		return filepath;
	}

	@Override
	public String toString() {
		return "Kite9GithubPath [owner=" + owner + ", reponame=" + reponame + ", ref=" + ref + ", filepath=" + filepath	+ "]";
	}
	
	
}
