package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.ADLFactory;
import com.kite9.k9server.adl.holder.pipeline.ADLBase;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.apache.commons.io.Charsets;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.StreamHelp;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;

/**
 * Outputs the untransformed input xml.
 * 
 * @author robmoffat
 *
 */
public class ADLFormat implements DiagramFileFormat {
	
	private ADLFactory factory;
	
	public ADLFormat(ADLFactory factory) {
		this.factory = factory;
	}
	
	public MediaType[] getMediaTypes() {
		return new MediaType[] { Kite9MediaTypes.ADL_SVG };
	}

	public void handleWrite(ADLDom data, OutputStream baos, Kite9SVGTranscoder transcoder) throws Exception {
		StreamHelp.streamCopy(new StringReader(data.getXMLString()), new OutputStreamWriter(baos), false);
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
		headers.setAccept(Arrays.asList(getMediaTypes()));
		ADLBase a = factory.uri(in, headers);
		return a;
	}

}