package com.kite9.server.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tools for examining paths pointing to github content.
 * 
 * @author robmoffat
 *
 */
public interface PathUtils {

	public static final Pattern p = Pattern.compile(
			"^\\/([a-z]+)(\\/([a-zA-Z0-9_-]+)(\\/([a-zA-Z0-9_-]+)(\\/.*)?)?)?");
	
	public static final int TYPE = 1;
	public static final int OWNER = 3;
	public static final int REPONAME = 5;
	public static final int FILEPATH = 6;
	
	public static String getPathSegment(int part, String ps) {
		Matcher m = p.matcher(ps);
		if (m.find()) {
			if (part == FILEPATH) {
				String path = m.group(FILEPATH);
				path = path == null ? "" : path;
				path = path.startsWith("/") ? path.substring(1) : path;
				return path;
			} else {
				return m.group(part);
			}
		} else {
			return null;
		}
	}
}
