package org.kite9.diagram.batik.format;

import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.ERROR_INCOMPATIBLE_OUTPUT_TYPE;
import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.KEY_ESCAPED;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.element.DiagramElementFactoryImpl;
import org.kite9.diagram.batik.templater.BasicCopier;
import org.kite9.diagram.batik.templater.Kite9ExpandingCopier;
import org.kite9.diagram.batik.templater.XMLProcessor;
import org.kite9.diagram.model.style.DiagramElementFactory;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.ADLExtensibleDOMImplementation;
import org.kite9.framework.dom.Kite9DocumentFactory;
import org.kite9.framework.dom.XMLHelper;
import org.kite9.framework.xml.ADLDocument;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.XMLFilter;

public final class Kite9SVGTranscoder extends SVGAbstractTranscoder {
	
	private ADLExtensibleDOMImplementation domImpl;
	private ResourceReferencer rr;	
	
	public Kite9SVGTranscoder(ResourceReferencer rr) {
		super();
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, ADLExtensibleDOMImplementation.SVG_NAMESPACE_URI);
		domImpl = new ADLExtensibleDOMImplementation();
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, domImpl);
		setTranscodingHints(hints);
		this.rr = rr;
	}

	@Override
	protected BridgeContext createBridgeContext(SVGOMDocument doc) {
		Kite9BridgeContext out = new Kite9BridgeContext(userAgent, createDocumentFactory());
		DiagramElementFactory def = new DiagramElementFactoryImpl(out);
		domImpl.setDiagramElementFactory(def);
		return out;
	}

	protected Kite9DocumentFactory createDocumentFactory() {
		return createDocumentFactory(domImpl, XMLResourceDescriptor.getXMLParserClassName());
	}
	
	@Override
	protected Kite9DocumentFactory createDocumentFactory(DOMImplementation domImpl, String parserClassname) {
		return new Kite9DocumentFactory((ADLExtensibleDOMImplementation) domImpl, parserClassname);
	}

	private Document createDocument(TranscoderOutput output) {
		// Use SVGGraphics2D to generate SVG content
		Document doc;
		if (output.getDocument() == null) {
			DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
			doc = domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_SVG_TAG, null);
		} else {
			doc = output.getDocument();
		}

		return doc;
	}

	private Document outputDocument;
	
	protected void transcode(Document input, String uri, TranscoderOutput output) throws TranscoderException {
		try {
			input.setDocumentURI(uri);
			super.transcode(input, uri, output);
			
			this.outputDocument = createDocument(output);
			XMLProcessor copier = new Kite9ExpandingCopier("");
			copier.process(input.getDocumentElement(), outputDocument.getDocumentElement());
		} catch (Exception e) {
			ADLDocument d = (ADLDocument)input;
			try {
				File f = new File("expanded.svg");
				String input2 = new XMLHelper().toXML(d);
				FileWriter fw = new FileWriter(f);
				fw.write(input2);
				fw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			throw new Kite9ProcessingException(e);
		}
	}
	
	
	@Override
	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		super.transcode(input, output);
		
        Document doc = createDocument(output);
		writeSVGToOutput(outputDocument, output);
	}

	/** Writes the SVG content held by the svgGenerator to the
     * <code>TranscoderOutput</code>. This method does nothing if the output already
     * contains a Document.
     */
    protected void writeSVGToOutput(Document outputDocument,
        TranscoderOutput output) throws TranscoderException {

        Document doc = output.getDocument();

        if (doc != null) return;

        // XMLFilter
        XMLFilter xmlFilter = output.getXMLFilter();
        if (xmlFilter != null) {
            handler.fatalError(new TranscoderException("" + ERROR_INCOMPATIBLE_OUTPUT_TYPE));
        }

        Element svgRoot = outputDocument.getDocumentElement();
        SVGGraphics2D svgGenerator = new SVGGraphics2D(outputDocument);
        
        
        try {
            boolean escaped = false;
            if (hints.containsKey(KEY_ESCAPED)) {
                escaped = ((Boolean)hints.get(KEY_ESCAPED)).booleanValue();
            }
            // Output stream
            OutputStream os = output.getOutputStream();
            if (os != null) {
                svgGenerator.stream(svgRoot, new OutputStreamWriter(os), false, escaped);
                return;
            }

            // Writer
            Writer wr = output.getWriter();
            if (wr != null) {
                svgGenerator.stream(svgRoot, wr, false, escaped);
                return;
            }

            // URI
            String uri = output.getURI();
            if ( uri != null ){
                try{
                    URL url = new URL(uri);
                    URLConnection urlCnx = url.openConnection();
                    os = urlCnx.getOutputStream();
                    svgGenerator.stream(svgRoot, new OutputStreamWriter(os), false, escaped);
                    return;
                } catch (MalformedURLException e){
                    handler.fatalError(new TranscoderException(e));
                } catch (IOException e){
                    handler.fatalError(new TranscoderException(e));
                }
            }
        } catch(IOException e){
            throw new TranscoderException(e);
        }

        throw new TranscoderException("" + ERROR_INCOMPATIBLE_OUTPUT_TYPE);

    }
    

}