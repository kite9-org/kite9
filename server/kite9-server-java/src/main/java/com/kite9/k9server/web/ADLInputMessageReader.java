package com.kite9.k9server.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.kite9.k9server.adl.format.FormatSupplier;
import com.kite9.k9server.adl.format.media.DiagramReadFormat;
import com.kite9.k9server.adl.format.media.Format;
import com.kite9.k9server.adl.format.media.Kite9MediaTypes;
import com.kite9.k9server.adl.holder.pipeline.ADLBase; 

public class ADLInputMessageReader extends AbstractGenericHttpMessageConverter<ADLBase> {
	
	public static final Logger LOG = LoggerFactory.getLogger(ADLInputMessageReader.class);

	public static final Charset DEFAULT = Charset.forName("UTF-8");

	protected FormatSupplier formatSupplier;
		
	public ADLInputMessageReader(FormatSupplier formatSupplier) {
		super();
		this.formatSupplier = formatSupplier;
		setSupportedMediaTypes(formatSupplier.getMediaTypes());
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return ADLBase.class.isAssignableFrom(clazz);
	}
	
	@Override
	protected boolean canWrite(MediaType mediaType) {
		return false;
	}

	@Override
	protected boolean canRead(MediaType mediaType) {
		return Kite9MediaTypes.SVG.includes(mediaType) || Kite9MediaTypes.ADL_SVG.includes(mediaType);
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return super.getSupportedMediaTypes();
	}

	@Override
	protected ADLBase readInternal(Class<? extends ADLBase> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		MediaType mt = inputMessage.getHeaders().getContentType();
		Format f = formatSupplier.getFormatFor(mt);
		if (f instanceof DiagramReadFormat) {
			try {
				URI uri = URIRewriter.getCompleteCurrentRequestURI();
				ADLBase out = ((DiagramReadFormat) f).handleRead(inputMessage.getBody(), uri, inputMessage.getHeaders());
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
