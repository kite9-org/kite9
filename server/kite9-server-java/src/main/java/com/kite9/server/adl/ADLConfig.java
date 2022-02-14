package com.kite9.server.adl;

import org.kite9.diagram.batik.format.ConsolidatedErrorHandler;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.kite9.diagram.logging.Logable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.server.adl.cache.PublicCache;
import com.kite9.server.adl.format.BasicFormatSupplier;
import com.kite9.server.adl.holder.ADLFactoryImpl;
import com.kite9.server.web.WebConfig;

@Configuration
@AutoConfigureAfter({WebConfig.class})
public class ADLConfig implements Logable {
	
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
        return new ADLFactoryImpl(c, xmlHelper());
    }

    @Bean
    public ConsolidatedErrorHandler consolidatedErrorHandler() {
		Kite9Log.Companion.setFactory(logable -> new Kite9LogImpl(logable));
    	return new ConsolidatedErrorHandler(Kite9Log.Companion.instance(this));
    }

    @Bean
    public XMLHelper xmlHelper() {
        return new XMLHelper(defaultXSLFactory, consolidatedErrorHandler());
    }

    @Bean
    public FormatSupplier formatSupplier(ADLFactory adlFactory, XMLHelper xmlHelper) {
        return new BasicFormatSupplier(adlFactory, xmlHelper);
    }


    @Override
    public String getPrefix() {
        return "XML ";
    }

    @Override
    public boolean isLoggingEnabled() {
        return true;
    }
}
