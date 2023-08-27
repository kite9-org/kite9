package com.kite9.server.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

import org.kite9.diagram.dom.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;

public class ADLDomMessageWriter extends AbstractADLDomMessageWriter<ADLDom> {
	
	public static final Logger LOG = LoggerFactory.getLogger(ADLDomMessageWriter.class);

	public static final Charset DEFAULT = Charset.forName("UTF-8");
			
	public ADLDomMessageWriter(FormatSupplier formatSupplier, Cache c) {
		super(formatSupplier, c);
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
	public ADLDom read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ADLDom readInternal(Class<? extends ADLDom> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void writeInternal(ADLDom t, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		writeADLDom(t, outputMessage);
	}
	
}
