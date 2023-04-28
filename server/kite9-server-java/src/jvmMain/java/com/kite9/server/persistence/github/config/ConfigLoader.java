package com.kite9.server.persistence.github.config;

import java.io.IOException;

public interface ConfigLoader {
			
	public Config loadConfig(String yamlString);

	public Config loadConfig(String token, String owner, String reponame, String ref) throws IOException;
}
