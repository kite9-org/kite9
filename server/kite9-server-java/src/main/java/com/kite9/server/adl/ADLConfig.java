package com.kite9.server.adl;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.server.adl.cache.PublicCache;
import com.kite9.server.adl.format.BasicFormatSupplier;
import com.kite9.server.adl.holder.ADLFactoryImpl;
import org.kite9.diagram.dom.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ADLConfig {

    @Bean
    public Cache cache() {
        return new PublicCache();
    }

    @Bean
    public ADLFactory adlFactory(Cache c) {
        return new ADLFactoryImpl(c);
    }

    @Bean
    public FormatSupplier formatSupplier(ADLFactory adlFactory) {
        return new BasicFormatSupplier(adlFactory);
    }

}
