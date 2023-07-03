package com.kite9.server.persistence.github.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;

public interface ConfigLoader {
	
	public static final String KITE9_CONFIG_FILE = ".kite9/config.yml";
			
	public Config loadConfig(String yamlString);

	public Config loadConfig(Authentication a, String owner, String reponame, String ref) throws IOException;
	
}
