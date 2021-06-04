package com.kite9.server.adl.format.media;

import com.kite9.server.adl.holder.ADLFactoryImpl;
import com.kite9.server.pipeline.adl.holder.ADLFactory;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.pipeline.uri.URI;
import org.apache.commons.io.Charsets;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
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

	private final List<MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getADL_SVG());

	public List<MediaType> getMediaTypes() {
		return mediaTypes;
	}

	public String getFormatIdentifier() {
		return getMediaTypes().get(0).toString();
	}

	@Override
	public Document handleWrite(ADLDom toWrite, Kite9Transcoder<Document> t) {
		return toWrite.getDocument();
	}

	public void handleWrite(ADLDom toWrite, OutputStream baos, Kite9Transcoder<Document> t) {
		Document d = handleWrite(toWrite, t);
		ADLFactoryImpl.duplicate(d, isOmitDeclaration(), new StreamResult(baos));
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
	public ADLBase handleRead(InputStream someFormat, URI uri, HttpHeaders headers) throws Exception {
		String s = StreamUtils.copyToString(someFormat, Charsets.UTF_8);
		return factory.adl(uri, s, headers);
	}

	@Override
	public ADLBase handleRead(URI in, HttpHeaders headers) {
		headers.set(HttpHeaders.ACCEPT, getMediaTypes().stream().map(mt -> mt.toString()).collect(Collectors.joining(",")));
		ADLBase a = factory.uri(in, headers);
		return a;
	}

}