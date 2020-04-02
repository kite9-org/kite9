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
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
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
import org.kite9.diagram.batik.model.DiagramElementFactoryImpl;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.Kite9DocumentFactory;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.model.DiagramElementFactory;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.post.DocumentValueReplacer;
import org.kite9.diagram.dom.processors.post.Kite9ExpandingCopier;
import org.kite9.diagram.dom.processors.pre.BasicTemplater;
import org.kite9.diagram.dom.scripts.HasScripts;
import org.kite9.diagram.dom.scripts.ScriptList;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.common.Kite9XMLProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.XMLFilter;

/**
 * Please note - this transcoder is single-use.
 */
public class Kite9SVGTranscoder extends SVGAbstractTranscoder implements Logable {
	
	private final ADLExtensibleDOMImplementation domImpl;
	private final Kite9Log log = new Kite9Log(this);
	private final Kite9DocumentFactory docFactory;
	private final Kite9DocumentLoader docLoader;
	private final Kite9BridgeContext bridgeContext;
	private final Cache cache;
	
	public Kite9SVGTranscoder() {
		this(Cache.NO_CACHE);
	}
	
	public Kite9SVGTranscoder(Cache c) {
		super();
		cache = c;
		domImpl = new ADLExtensibleDOMImplementation(c);
		docFactory = new Kite9DocumentFactory(domImpl, XMLResourceDescriptor.getXMLParserClassName());
	    docLoader = new Kite9DocumentLoader(userAgent, docFactory, true, cache);
		bridgeContext = new Kite9BridgeContext(userAgent, docLoader);
		DiagramElementFactory def = new DiagramElementFactoryImpl(bridgeContext);
		domImpl.setDiagramElementFactory(def);
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, ADLExtensibleDOMImplementation.SVG_NAMESPACE_URI);
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, domImpl);
		setTranscodingHints(hints);
	}

	public Kite9DocumentFactory getDocFactory() {
		return docFactory;
	}
	
	public Kite9DocumentLoader getDocLoader() {
		return docLoader;
	}
	
	public ADLExtensibleDOMImplementation getDomImplementation() {
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
					InputStream broken = Kite9SVGTranscoder.class.getResourceAsStream("/broken.svg");
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
	protected Kite9BridgeContext createBridgeContext(String version) {
		return bridgeContext;
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
			doc = domImpl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, null, null);
		} else {
			doc = output.getDocument();
		}

		return doc;
	}

	private Document outputDocument;
	
	protected void transcode(Document input, String uri, TranscoderOutput output) throws TranscoderException {
		try {
			
			// this bit positions all the diagram elements
			input.setDocumentURI(uri);
			ensureCSSEngine((ADLDocument) input);
			new BasicTemplater(this.docLoader).processContents(input);

			if (log.go()) {
				log.send(new XMLHelper().toXML(input));
			}

			
			super.transcode(input, uri, output);
			
			
			this.outputDocument = createDocument(output);
			ensureCSSEngine((SVGOMDocument) this.outputDocument);
			XMLProcessor copier = new Kite9ExpandingCopier("", outputDocument, new DocumentValueReplacer(input));
			//System.out.println("OUTPUTTING");
			Node outputNode = copier.processContents(input.getDocumentElement());
			this.outputDocument.appendChild(outputNode);
			transcodeScripts(input, this.outputDocument);
		} catch (Exception e) {
			String s = new XMLHelper().toXML(input);
			log.error("Problem with XML: "+e);
			throw new Kite9XMLProcessingException("Transcoder problem: "+e.getMessage(), e, s, null);
		}
	}

	public void ensureCSSEngine(SVGOMDocument input) {
		if (input.getCSSEngine() == null) {
			CSSEngine engine = domImpl.createCSSEngine(input, createBridgeContext());
			input.setCSSEngine(engine);
		}
	}
		
	protected void transcodeScripts(Document input, Document output) {
		if (input instanceof HasScripts) {
			ScriptList scripts = ((HasScripts)input).getScripts();
			scripts.appendScriptTags(output);
		}
	}

	private void copySVGAttributes(Element in, Element out) {
		NamedNodeMap nnm = in.getAttributes();
		
		for (int i = 0; i < nnm.getLength(); i++) {
			Attr a = (Attr) nnm.item(i);
			
			if ((a.getNamespaceURI() == null) || (a.getNamespaceURI().equals(SVGConstants.SVG_NAMESPACE_URI))) {
				if(!a.getName().equals("id")) {
					out.setAttributeNS(a.getNamespaceURI(), a.getName(), a.getValue());
				}
			}
		}
	}

	@Override
	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		super.transcode(input, output);
		writeSVGToOutput(outputDocument, output);
	}

	/** Writes the SVG content held by the svgGenerator to the
     * <code>TranscoderOutput</code>. This method does nothing if the output already
     * contains a Document.
     * 
     * (From {@link ToSVGAbstractTranscoder})
     */
    public void writeSVGToOutput(Document outputDocument,
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