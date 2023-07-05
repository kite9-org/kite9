package com.kite9.server.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.K9MediaType;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebConfig implements WebMvcConfigurer, InitializingBean {

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(adlMessageWriter());
	}
	
	@Autowired
	Cache cache;

	@Autowired
	FormatSupplier fs;
	
	@Bean
	public HttpMessageConverter<?> adlMessageWriter() {
		return new ADLDomMessageWriter(fs, cache);
	}
	
	@Bean 
	public ADLInputMessageReader adlMessageReader() {
		return new ADLInputMessageReader(fs);
	}

	@Value("${kite9.diagram-log:FILE}")
	Kite9Log.Destination destination;

	@Override
	public void afterPropertiesSet() throws Exception {
		Kite9Log.Companion.setFactory(logable -> new Kite9LogImpl(logable));
		Kite9LogImpl.setLogging(destination);
	}

	public class LoggingFilter extends CommonsRequestLoggingFilter {

		public LoggingFilter() {
			super();
			 this.setIncludeClientInfo(true);
			 this.setIncludeQueryString(true);
			 this.setIncludePayload(true);
			 this.setMaxPayloadLength(1000);		
		}
		
		@Override
		protected String createMessage(HttpServletRequest request, String prefix, String suffix) {
			return request.getMethod() + " " + super.createMessage(request, prefix, suffix);
		}
		
	}
	
	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
	   return new LoggingFilter();
	}

	/**
	 * This allows the user to add the ?format=html parameter to the query 
	 * to modify the type of content required.
	 */
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorParameter(true);
		for (Format f : fs.getPriorityOrderedFormats()) {
			for (K9MediaType mt : f.getMediaTypes()) {
				configurer.mediaType(f.getExtension(), MediaType.parseMediaType(mt.toString()));
			}
		}		
	}
	
	

}
