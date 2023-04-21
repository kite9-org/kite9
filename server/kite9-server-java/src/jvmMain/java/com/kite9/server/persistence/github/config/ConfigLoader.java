package com.kite9.server.persistence.github.config;

import java.io.IOException;
import java.util.Arrays;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;

public interface ConfigLoader {
	
	public static final String DEFAULT_CONFIG_FILE = ".kite9/config.yml";
	
	public static final Config DEFAULT = new Config("./kite9/templates", 
			Arrays.asList(new Source("", 
					"https://kite9.com/public/examples/basic.adl?format=adl",
					"*.adl")));

	public Config loadConfig(String yamlString);

	public Config getConfig(GHTree tree, GHRepository repo, String token, String ref) throws IOException;
}
