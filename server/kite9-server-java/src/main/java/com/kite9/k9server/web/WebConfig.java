package com.kite9.k9server.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.kite9.diagram.dom.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kite9.k9server.adl.format.FormatSupplier;
import com.kite9.k9server.adl.format.media.Format;
import com.kite9.k9server.topic.ChangeBroadcaster;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(adlMessageWriter());
	}

	@Autowired
	FormatSupplier fs;
	
	@Autowired
	Cache cache;
	
	@Autowired
	ChangeBroadcaster changeBroadcaster;
	
	@Bean
	public HttpMessageConverter<?> adlMessageWriter() {
		return new ADLDomMessageWriter(fs, cache, changeBroadcaster);
	}
	
	@Bean 
	public ADLInputMessageReader adlMessageReader() {
		return new ADLInputMessageReader(fs);
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
			for (MediaType	mt : f.getMediaTypes()) {
				configurer.mediaType(f.getExtension(), mt);
			}
		}		
	}
	
	

}
