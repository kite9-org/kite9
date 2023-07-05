package com.kite9.server.persistence.github.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;

import org.kite9.diagram.logging.Kite9ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.util.StreamUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;
import com.kite9.server.update.Update;
import com.kite9.server.web.URIRewriter;

public class ConfigLoaderImpl implements ConfigLoader {
	
	public static final Logger LOG = LoggerFactory.getLogger(ConfigLoaderImpl.class);
		
	private SourceAPIFactory factory;
	
	public ConfigLoaderImpl(SourceAPIFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public Config loadConfig(Authentication authentication, String owner, String reponame, String ref) throws IOException {
		try {
			K9URI sourceUri = URIRewriter.resolve("/github/"+owner+"/"+reponame+"/"+KITE9_CONFIG_FILE+"?v="+ref);
			Update u = new Update(Collections.emptyList(), sourceUri, Update.Type.NEW);
			SourceAPI s = factory.createAPI(u, authentication);
			InputStream is = s.getCurrentRevisionContentStream(authentication);
			String contents = StreamUtils.copyToString(is, Charset.forName("UTF-8"));
			return loadConfig(contents);
		} catch (IOException e) {
			LOG.debug("No Config found for {} {} {}", owner, reponame, ref);
			return new Config();
		} catch (Exception e) {
			throw new Kite9ProcessingException("Couldn't load config: "+e.getMessage());
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
