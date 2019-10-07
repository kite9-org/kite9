package org.kite9.diagram.batik.format;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.model.DiagramElementFactoryImpl;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.Kite9DocumentFactory;
import org.kite9.diagram.dom.model.DiagramElementFactory;
import org.kite9.framework.logging.Kite9Log;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * This is a massive, horrible hack - we end up creating the doc twice.  Isn't there a better way?
 * This will turn a rendered SVG diagram into PNG.
 */
public class Kite9PNGTranscoder extends PNGTranscoder {

	private final ADLExtensibleDOMImplementation domImpl;
	private final Kite9Log log = new Kite9Log(this);
	private final Kite9DocumentFactory docFactory;
	private final Kite9DocumentLoader docLoader;
	private final Kite9BridgeContext bridgeContext;
	
	public Kite9SVGTranscoder() {
		super();
		domImpl = new ADLExtensibleDOMImplementation();
		docFactory = new Kite9DocumentFactory(domImpl, XMLResourceDescriptor.getXMLParserClassName());
	    docLoader = new Kite9DocumentLoader(userAgent, docFactory, true);
		bridgeContext = new Kite9BridgeContext(userAgent, docLoader);
		DiagramElementFactory def = new DiagramElementFactoryImpl(bridgeContext);
		domImpl.setDiagramElementFactory(def);
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, ADLExtensibleDOMImplementation.SVG_NAMESPACE_URI);
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, domImpl);
		setTranscodingHints(hints);
	}


	@Override
	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		TranscoderOutput inter1 = new TranscoderOutput();
		super.transcode(input, inter1);
		Document svg = inter1.getDocument();
		svg.setDocumentURI(input.getURI());
		PNGTranscoder png = new PNGTranscoder();
		png.setTranscodingHints(this.getTranscodingHints());
		TranscoderInput inter2 = new TranscoderInput(svg);
		png.transcode(inter2, output);
	}
	
	/**
	 * Since we're converting to PNG, we can use Batik's support of SVG1.2
	 */
	protected Document createDocument(TranscoderOutput output) {
		// Use SVGGraphics2D to generate SVG content
		Document doc;
		if (output.getDocument() == null) {
			DOMImplementation domImpl = SVG12DOMImplementation.getDOMImplementation();
			doc = domImpl.createDocument(SVG12DOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_SVG_TAG, null);
		} else {
			doc = output.getDocument();
		}

		return doc;
	}
	
}

