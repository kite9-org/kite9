package com.kite9.server.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.MediaTypeHelper;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;

public class ADLInputMessageReader extends AbstractGenericHttpMessageConverter<ADLBase> {
	
	public static final Logger LOG = LoggerFactory.getLogger(ADLInputMessageReader.class);

	public static final Charset DEFAULT = Charset.forName("UTF-8");

	protected FormatSupplier formatSupplier;
		
	public ADLInputMessageReader(FormatSupplier formatSupplier) {
		super();
		this.formatSupplier = formatSupplier;
		setSupportedMediaTypes(
				formatSupplier.getMediaTypes()
						.stream()
						.map(mt -> org.springframework.http.MediaType.parseMediaType(mt.toString()))
						.collect(Collectors.toList()));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return ADLBase.class.isAssignableFrom(clazz);
	}
	
	@Override
	protected boolean canWrite(org.springframework.http.MediaType mediaType) {
		return false;
	}

	@Override
	protected boolean canRead(org.springframework.http.MediaType mediaType) {
		K9MediaType mt = MediaTypeHelper.getKite9MediaType(mediaType);
		return MediaTypeHelper.includes(Kite9MediaTypes.INSTANCE.getSVG(), mediaType) ||
				MediaTypeHelper.includes(Kite9MediaTypes.INSTANCE.getADL_SVG(), mediaType);
	}

	@Override
	public List<org.springframework.http.MediaType> getSupportedMediaTypes() {
		return super.getSupportedMediaTypes();
	}

	@Override
	protected ADLBase readInternal(Class<? extends ADLBase> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		K9MediaType mt = MediaTypeHelper.getKite9MediaType(inputMessage.getHeaders().getContentType());
		Format f = formatSupplier.getFormatFor(mt);
		if (f instanceof DiagramFileFormat) {
			try {
				K9URI uri = URIRewriter.getCompleteCurrentRequestURI();
				ADLBase out = ((DiagramFileFormat) f).handleRead(inputMessage.getBody(), uri, inputMessage.getHeaders());
				return out;
			} catch (Exception e) {
				throw new Kite9XMLProcessingException("Couldn't extract diagram from "+f, e);
			}
		} else {
			throw new HttpMessageNotReadableException("Couldn't extract diagram from "+f, inputMessage);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ADLBase read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return readInternal((Class<? extends ADLBase>) contextClass, inputMessage);
	}

	@Override
	protected void writeInternal(ADLBase t, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
	}


	
}
