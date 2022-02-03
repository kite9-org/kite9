package com.kite9.server.adl;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.server.adl.cache.PublicCache;
import com.kite9.server.adl.format.BasicFormatSupplier;
import com.kite9.server.adl.holder.ADLFactoryImpl;
import org.kite9.diagram.dom.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ADLConfig {
	
	@Value("${kite9.caching:true}")
	private boolean caching;
	
	@Value("${kite9.transformer.factory:}")
	private String defaultXSLFactory;

    @Bean
    public Cache cache() {
        return caching ? new PublicCache() : Cache.NO_CACHE;
    }

    @Bean
    public ADLFactory adlFactory(Cache c) {
        return new ADLFactoryImpl(c, defaultXSLFactory);
    }

    @Bean
    public FormatSupplier formatSupplier(ADLFactory adlFactory) {
        return new BasicFormatSupplier(adlFactory);
    }

}
