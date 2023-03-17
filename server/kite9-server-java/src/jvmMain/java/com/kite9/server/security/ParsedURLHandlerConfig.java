package com.kite9.server.security;

import org.apache.batik.util.ParsedURL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParsedURLHandlerConfig implements InitializingBean {
	
	@Override
	public void afterPropertiesSet() throws Exception {
		ParsedURL.registerHandler(new AuthenticatingParsedURLHandler("https"));
		ParsedURL.registerHandler(new AuthenticatingParsedURLHandler("http"));
	}

}
