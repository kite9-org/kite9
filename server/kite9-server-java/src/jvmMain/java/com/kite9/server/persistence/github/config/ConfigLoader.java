package com.kite9.server.persistence.github.config;

import java.io.IOException;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;

public interface ConfigLoader {
	
	public static final String DEFAULT_CONFIG_FILE = ".kite9/config.yml";
		
	public Config loadConfig(String yamlString);

	public Config getConfig(GHTree tree, GHRepository repo, String token, String ref) throws IOException;
}
