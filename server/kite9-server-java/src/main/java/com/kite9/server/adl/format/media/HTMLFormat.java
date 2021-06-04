package com.kite9.server.adl.format.media;

import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;
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
public class HTMLFormat extends AbstractSVGFormat implements EditableDiagramFormat<Document> {

	private final List<byte[]> format;
	private final List<MediaType> mediaTypes;

	public List<MediaType> getMediaTypes() {
		return mediaTypes;
	}
	public HTMLFormat() {
		super();
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
	public void handleWrite(ADLDom adl, OutputStream baos, Kite9Transcoder<Document> t) {
		try {
			baos.write(format.get(0));
			baos.write(adl.getTitle().getBytes());
			baos.write(format.get(1));
			// additional headers
			baos.write(format.get(2));
			super.handleWrite(adl, baos, t);
			baos.write(format.get(3));
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
	protected void setupTranscoder(Kite9Transcoder<Document> t, ADLDom toWrite) {
		t.addTranscodingHint(Kite9SVGTranscoder.KEY_MEDIA, "editor");
		super.setupTranscoder(t, toWrite);
	}

	
}
