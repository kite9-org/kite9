package com.kite9.server.web;

import java.io.IOException;
import java.util.stream.Collectors;

import org.kite9.diagram.dom.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.adl.format.MediaTypeHelper;

public abstract class AbstractADLDomMessageWriter<X> extends AbstractGenericHttpMessageConverter<X> {

	public static final Logger LOG = LoggerFactory.getLogger(ADLDomMessageWriter.class);

	protected FormatSupplier formatSupplier;
	protected Cache cache;

	public AbstractADLDomMessageWriter(FormatSupplier formatSupplier, Cache c) {
		super();
		this.formatSupplier = formatSupplier;
		this.cache = c;
		setSupportedMediaTypes(formatSupplier.getMediaTypes().stream()
				.map(mt -> MediaType.parseMediaType(mt.toString())).collect(Collectors.toList()));
	}

	protected void writeADLDom(ADLDom t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		MediaType contentType = outputMessage.getHeaders().getContentType();
		try {
			LOG.info("Performing Conversion on {} ", t.getUri());
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

				outputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE, contentType.toString());
				StreamUtils.copy(out.getAsBytes(), outputMessage.getBody());
			} else {
				throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
						"Can't write diagram as " + f.getExtension());
			}

		} catch (Exception e) {
			LOG.error("Couldn't process: "+t.getAsString());
			throw new HttpMessageNotWritableException("Caused by: " + e.getMessage(), e);
		}
	}
}