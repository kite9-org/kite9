package com.kite9.server.adl.format.media;

import com.kite9.pipeline.adl.format.media.EditableDiagramFormat;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.adl.holder.ADLOutputImpl;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders the GUI.
 * 
 * @author robmoffat
 *
 */
public class HTMLFormat extends AbstractSVGFormat implements EditableDiagramFormat {

	private final List<byte[]> format;
	private final List<K9MediaType> mediaTypes;

	public List<K9MediaType> getMediaTypes() {
		return mediaTypes;
	}
	public HTMLFormat(XMLHelper helper) {
		super(helper);
		String pageTemplate;
		try {
			pageTemplate = StreamUtils.copyToString(this.getClass().getResourceAsStream("/page-template.html"), Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		format = Arrays.stream(pageTemplate.split("\\{[a-z]+\\}"))
				.map(s -> s.getBytes())
				.collect(Collectors.toList());
		mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getHTML());
	}

	
	@Override
	public ADLOutput handleWrite(ADLDom adl, Kite9Transcoder t) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(format.get(0));
			baos.write(adl.getTitle().getBytes());
			baos.write(format.get(1));
			// additional headers
			baos.write(format.get(2));
			ADLOutput o1 = super.handleWrite(adl, t);
			baos.write(o1.getAsBytes());
			baos.write(format.get(3));
			return new ADLOutputImpl(this, adl, baos.toByteArray(), o1.getAsDocument());
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't create output stream", e);
		}
	}

	public String getExtension() {
		return "html";
	}

	@Override
	protected boolean isOmitDeclaration() {
		return true;
	}

	@Override
	protected void setupTranscoder(Kite9Transcoder t, ADLDom toWrite) {
		t.addTranscodingHint(Kite9SVGTranscoder.KEY_MEDIA, "editor");
		super.setupTranscoder(t, toWrite);
	}

	
}
