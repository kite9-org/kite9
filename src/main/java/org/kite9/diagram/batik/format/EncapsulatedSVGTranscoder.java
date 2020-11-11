package org.kite9.diagram.batik.format;

import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.ERROR_INCOMPATIBLE_OUTPUT_TYPE;
import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.KEY_ESCAPED;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.ToSVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.CachingSVGDOMImplementation;
import org.kite9.diagram.dom.Kite9DocumentFactory;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.defs.DefList;
import org.kite9.diagram.dom.defs.HasDefs;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.post.DocumentValueReplacer;
import org.kite9.diagram.dom.processors.post.Kite9ExpandingCopier;
import org.kite9.diagram.dom.processors.pre.BasicTemplater;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.common.Kite9XMLProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.XMLFilter;

/**
 * Produces SVG where all the resources are held in the one file.  That is, 
 * fonts are turned into strokes, images are encoded as data: urls, css styles are expanded.
 */
public class EncapsulatedSVGTranscoder extends SVGAbstractTranscoder implements Logable {
	
	private final CachingSVGDOMImplementation domImpl;
	private final Kite9DocumentFactory docFactory;
	private final Kite9DocumentLoader docLoader;
	private final BridgeContext bridgeContext;
	private final Cache cache;
	
	public EncapsulatedSVGTranscoder() {
		this(Cache.NO_CACHE);
	}
	
	public EncapsulatedSVGTranscoder(Cache c) {
		super();
		cache = c;
		domImpl = new CachingSVGDOMImplementation(c);
		docFactory = new Kite9DocumentFactory(domImpl, XMLResourceDescriptor.getXMLParserClassName());
	    docLoader = new Kite9DocumentLoader(userAgent, docFactory, cache);
		bridgeContext = new Kite9BridgeContext(userAgent, docLoader, true);
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, CachingSVGDOMImplementation.SVG_NAMESPACE_URI);
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, domImpl);
		setTranscodingHints(hints);
	}

	public Kite9DocumentFactory getDocFactory() {
		return docFactory;
	}
	
	public Kite9DocumentLoader getDocLoader() {
		return docLoader;
	}
	
	public CachingSVGDOMImplementation getDomImplementation() {
		return domImpl;
	}

	@Override
	protected UserAgent createUserAgent() {
		return new SVGAbstractTranscoderUserAgent() {

			@Override
			public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) throws SecurityException {
				// TODO Auto-generated method stub
				super.checkLoadScript(scriptType, scriptURL, docURL);
			}

			@Override
			public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) throws SecurityException {
				// TODO Auto-generated method stub
				super.checkLoadExternalResource(resourceURL, docURL);
			}

			@Override
			public SVGDocument getBrokenLinkDocument(Element e, String url, String message) {
				try {
					InputStream broken = EncapsulatedSVGTranscoder.class.getResourceAsStream("/broken.svg");
					return (SVGDocument) docLoader.loadDocument(url, broken);
				} catch (IOException e1) {
					throw new Kite9ProcessingException("Couldn't load broken.svg", e1);
				}
			}
			
			
			
		};
	}
	
	public UserAgent getUserAgent() {
		return userAgent;
	}

	@Override
	public BridgeContext createBridgeContext(String version) {
		return bridgeContext;
	}

	@Override
	public BridgeContext createBridgeContext() {
		return super.createBridgeContext();
	}
	
	@Override
	protected Kite9DocumentFactory createDocumentFactory(DOMImplementation domImpl, String parserClassname) {
		return docFactory;
	}

	protected Document createDocument(TranscoderOutput output) {
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

	@Override
	protected void transcode(Document document, String uri, TranscoderOutput output) throws TranscoderException {
		// TODO Auto-generated method stub
		document.setDocumentURI(uri);
		super.transcode(document, uri, output);
	}
	
	@Override
	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		super.transcode(input, output);
		Document outputDocument = createDocument(output);
        SVGGraphics2D g2 = new SVGGraphics2D(outputDocument);
        ((RootGraphicsNode) this.root).paint(g2);
        Element groupElem = g2.getTopLevelGroup(true);
        outputDocument.getDocumentElement().appendChild(groupElem);
        writeSVGToOutput(outputDocument, output);
	} 

	/** 
	 * Writes the SVG content held by the svgGenerator to the
     * <code>TranscoderOutput</code>. This method does nothing if the output already
     * contains a Document.
     * 
     * (From {@link ToSVGAbstractTranscoder})
     */
    public void writeSVGToOutput(Document outputDocument, TranscoderOutput output) throws TranscoderException {

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
            
            // nothing set
            output.setDocument(outputDocument);
        } catch(IOException e){
            throw new TranscoderException(e);
        }
    }

	@Override
	public String getPrefix() {
		return "KSVG";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}


}