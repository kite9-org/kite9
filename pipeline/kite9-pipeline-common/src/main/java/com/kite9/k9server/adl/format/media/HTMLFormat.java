package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders the GUI.
 * 
 * @author robmoffat
 *
 */
public class HTMLFormat extends AbstractSVGFormat implements EditableDiagramFormat {

	private List<byte[]> format;
	
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
	}

	@Override
	public MediaType[] getMediaTypes() {
		return new MediaType[] { MediaType.TEXT_HTML };
	}
	
	@Override
	public void handleWrite(ADLDom adl, OutputStream baos, Kite9SVGTranscoder t) throws Exception {
		baos.write(format.get(0));	
		baos.write(adl.getTitle().getBytes());
		baos.write(format.get(1));
		// additional headers
		baos.write(format.get(2));
		super.handleWrite(adl, baos, t);
		baos.write(format.get(3));
	}

	public String getExtension() {
		return "html";
	}

	@Override
	protected boolean isOmitDeclaration() {
		return true;
	}

	@Override
	protected void setupTranscoder(Kite9SVGTranscoder t, ADLDom toWrite) {
		t.addTranscodingHint(Kite9SVGTranscoder.KEY_MEDIA, "editor");
		super.setupTranscoder(t, toWrite);
	}

	
}
