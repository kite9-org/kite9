package com.kite9.server.adl.format.media;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.holder.ADLFactoryImpl;
import com.kite9.server.adl.holder.ADLOutputImpl;
import org.apache.commons.io.Charsets;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Outputs the untransformed input xml.
 * 
 * @author robmoffat
 *
 */
public class ADLFormat implements DiagramFileFormat {
	
	private final ADLFactory factory;
	
	public ADLFormat(ADLFactory factory) {
		this.factory = factory;
	}

	private final List<K9MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getADL_SVG());

	public List<K9MediaType> getMediaTypes() {
		return mediaTypes;
	}

	public String getFormatIdentifier() {
		return getMediaTypes().get(0).toString();
	}

	@Override
	public ADLOutput handleWrite(ADLDom toWrite, Kite9Transcoder t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ADLFactoryImpl.duplicate(toWrite.getDocument(), isOmitDeclaration(), new StreamResult(baos));
		return new ADLOutputImpl(this, toWrite, baos.toByteArray(), null, toWrite.getDocument());
	}

	private boolean isOmitDeclaration() {
		return false;
	}

	@Override
	public String getExtension() {
		return "adl";
	}

	@Override
	public boolean isBinaryFormat() {
		return false;
	}

	@Override
	public ADLBase handleRead(InputStream someFormat, K9URI uri, HttpHeaders headers) throws Exception {
		String s = StreamUtils.copyToString(someFormat, Charsets.UTF_8);
		return factory.adl(uri, s, headers);
	}

	@Override
	public ADLBase handleRead(K9URI in, HttpHeaders headers) {
		headers.set(HttpHeaders.ACCEPT, getMediaTypes().stream().map(mt -> mt.toString()).collect(Collectors.joining(",")));
		ADLBase a = factory.uri(in, headers);
		return a;
	}

}