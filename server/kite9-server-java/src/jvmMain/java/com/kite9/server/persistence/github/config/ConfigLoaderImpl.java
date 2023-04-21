package com.kite9.server.persistence.github.config;

import java.io.IOException;
import java.nio.charset.Charset;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StreamUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.kite9.server.persistence.github.urls.RawGithub;

public class ConfigLoaderImpl implements ConfigLoader {
	
	private static final String KITE_CONFIG_FILE = ".kite9/config.yml";
	

	@Override
	public Config getConfig(GHTree tree, GHRepository repo, String token, String ref) throws IOException {
		GHTreeEntry configEntry = tree.getEntry(KITE_CONFIG_FILE);
		
		if (configEntry == null) {
			return DEFAULT;
		} else {
			return loadConfig(token, repo.getOwnerName(), repo.getName(), KITE_CONFIG_FILE, ref);
		}		
	}
	
	public Config loadConfig(String token, String owner, String reponame, String ref, String path) throws IOException {
		String url = RawGithub.assembleGithubURL(owner, reponame, path, ref);
		ByteArrayResource r = RawGithub.loadBytesFromGithub(token, url);
		String contents = StreamUtils.copyToString(r.getInputStream(), Charset.forName("UTF-8"));
		return loadConfig(contents);
	}
	
	public Config loadConfig(String yamlString) {
		Representer representer = new Representer();
		representer.getPropertyUtils().setSkipMissingProperties(true);
		Yaml yaml = new Yaml(new Constructor(Config.class), representer);
		Config out = yaml.load(yamlString); 
		return out;
	}

}
