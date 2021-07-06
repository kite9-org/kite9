package com.kite9.server.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import org.kite9.diagram.dom.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.adl.format.MediaTypeHelper;
import com.kite9.server.adl.format.media.EditableSVGFormat;
import com.kite9.server.topic.ChangeBroadcaster;

public class ADLDomMessageWriter extends AbstractGenericHttpMessageConverter<ADLDom> {
	
	public static final Logger LOG = LoggerFactory.getLogger(ADLDomMessageWriter.class);

	public static final Charset DEFAULT = Charset.forName("UTF-8");

	protected FormatSupplier formatSupplier;
	protected Cache cache;
	protected ChangeBroadcaster changeBroadcaster;
		
	public ADLDomMessageWriter(FormatSupplier formatSupplier, Cache c, ChangeBroadcaster changeBroadcaster) {
		super();
		this.formatSupplier = formatSupplier;
		this.cache = c;
		this.changeBroadcaster = changeBroadcaster;
		setSupportedMediaTypes(
				formatSupplier.getMediaTypes().stream()
					.map(mt -> MediaType.parseMediaType(mt.toString()))
						.collect(Collectors.toList()));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return ADLDom.class.isAssignableFrom(clazz);
	}
	
	@Override
	protected boolean canWrite(MediaType mediaType) {
		return super.canWrite(mediaType);
	}

	@Override
	protected boolean canRead(MediaType mediaType) {
		return false;
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return super.getSupportedMediaTypes();
	}

	@Override
	protected void writeInternal(ADLDom t, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		MediaType contentType = outputMessage.getHeaders().getContentType();	
		try {
			LOG.info("Performing Conversion on {} ",t.getUri());
			if (LOG.isDebugEnabled()) {
				LOG.debug(t.getAsString());
			}
			
			Format f = formatSupplier.getFormatFor(MediaTypeHelper.getKite9MediaType(contentType));
			if (f instanceof DiagramWriteFormat) {
				DiagramWriteFormat df = (DiagramWriteFormat) f;
				String uriStr = t.getUri().toString();
				ADLOutput out = t.process(t.getUri(), df);
	
				if (cache.isValid(uriStr)) {
					cache.set(uriStr, df.getFormatIdentifier(), out.getAsBytes());
				} 
				
				if (df instanceof EditableSVGFormat) {
					changeBroadcaster.broadcast(t.getTopicUri(), out);
				}
	
				StreamUtils.copy(out.getAsBytes(), outputMessage.getBody());
			} else {
				throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Can't write diagram as "+f.getExtension());
			}
			
		} catch (Exception e) {
			throw new HttpMessageNotWritableException("Caused by: "+e.getMessage(), e);
		}
	}

	@Override
	public ADLDom read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ADLDom readInternal(Class<? extends ADLDom> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	
	
}
