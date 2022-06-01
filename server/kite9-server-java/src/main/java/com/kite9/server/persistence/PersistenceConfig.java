package com.kite9.server.persistence;

import java.util.Arrays;

import org.kite9.diagram.dom.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.server.persistence.github.GithubSourceAPIFactory;
import com.kite9.server.persistence.local.StaticSourceAPIFactory;
import com.kite9.server.sources.PlugableContentAPIFactory;
import com.kite9.server.sources.SourceAPIFactory;

/**
 * Handles the primary, plug-able {@link SourceAPIFactory}.
 * 
 * @author robmoffat
 *
 */
@Configuration
public class PersistenceConfig {
	
	@Autowired
	ResourcePatternResolver resolver;
	
	@Autowired
	OAuth2AuthorizedClientRepository clientRepository;
	
	@Autowired
	FormatSupplier fs;
	
	@Autowired
	Cache cache;
	
	@Autowired
	ApplicationContext ctx;
	
	@Autowired
	ADLFactory factory;
	
	@Bean
	StaticSourceAPIFactory staticSourceAPIFactory() {
		return new StaticSourceAPIFactory(cache, resolver, fs);
	}
	
	@Bean
	GithubSourceAPIFactory githubContentAPIFactory() {
		return new GithubSourceAPIFactory(ctx, factory, clientRepository, fs);
	}

	@Bean
	@Primary
	public SourceAPIFactory contentAPIFactory() {
		return new PlugableContentAPIFactory(
					Arrays.asList(githubContentAPIFactory(), staticSourceAPIFactory()));
	} 
}
