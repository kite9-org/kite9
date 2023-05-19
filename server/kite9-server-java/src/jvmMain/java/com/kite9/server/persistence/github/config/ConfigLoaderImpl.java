package com.kite9.server.persistence.github.config;

import java.io.IOException;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StreamUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.kite9.server.persistence.github.urls.RawGithub;

public class ConfigLoaderImpl implements ConfigLoader {
	
	public static final Logger LOG = LoggerFactory.getLogger(ConfigLoaderImpl.class);
	
	private static final String KITE_CONFIG_FILE = ".kite9/config.yml";
	
	@Override
	public Config loadConfig(String token, String owner, String reponame, String ref) throws IOException {
		try {
			String url = RawGithub.assembleGithubURL(owner, reponame, ref, KITE_CONFIG_FILE);
			ByteArrayResource r = RawGithub.loadBytesFromGithub(token, url);
			String contents = StreamUtils.copyToString(r.getInputStream(), Charset.forName("UTF-8"));
			return loadConfig(contents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Config();
		}
	}
	
	public Config loadConfig(String yamlString) {
		Representer representer = new Representer();
		representer.getPropertyUtils().setSkipMissingProperties(true);
		Yaml yaml = new Yaml(new Constructor(Config.class), representer);
		Config out = yaml.load(yamlString); 
		return out;
	}

}
