package org.kite9.diagram.batik.format;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.xml.utils.DefaultErrorHandler;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.model.BatikDiagramElementFactory;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.CachingSVGDOMImplementation;
import org.kite9.diagram.dom.Kite9DocumentFactory;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.processors.DiagramPositionProcessor;
import org.kite9.diagram.dom.processors.DiagramStructureProcessor;
import org.kite9.diagram.dom.processors.TextWrapProcessor;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.post.Kite9InliningProcessor;
import org.kite9.diagram.dom.processors.xpath.XPathValueReplacer;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.visualization.display.BasicCompleteDisplayer;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.diagram.visualization.pipeline.BasicArrangementPipeline;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.ERROR_INCOMPATIBLE_OUTPUT_TYPE;
import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.KEY_ESCAPED;

/**
 * Please note - this transcoder is single-use.
 */
public class Kite9SVGTranscoder extends SVGAbstractTranscoder implements Logable {
	
	/**
	 * If the Encapsulating hint is set, then the SVG will not reference external files for images, fonts,
	 * style sheets, etc.  Everything will be in-lined.  This is how SVG is normally formatted, but is
	 * not the usual way for editable diagrams, so is false by default.
	 */
	public static final TranscodingHints.Key KEY_ENCAPSULATING = new BooleanKey();
	public static final String TRANSFORMER = "transformer";

	private final ADLExtensibleDOMImplementation domImpl;
	private final Kite9Log log = Kite9Log.Companion.instance(this);
	private final Kite9DocumentFactory docFactory;
	private final Kite9DocumentLoader docLoader;
	private final Cache cache;
	private final BatikDiagramElementFactory def;
	private final TransformerFactory transFact;

	public Kite9SVGTranscoder() {
		this(Cache.NO_CACHE);
	}
	
	public Kite9SVGTranscoder(Cache c) {
		super();
		this.hints.put(KEY_ENCAPSULATING, false);
		this.cache = c;
		this.domImpl = new ADLExtensibleDOMImplementation(c);
		this.docFactory = new Kite9DocumentFactory(domImpl, XMLResourceDescriptor.getXMLParserClassName());
	    this.docLoader = new Kite9DocumentLoader(userAgent, docFactory, cache);
		this.ctx = new Kite9BridgeContext(userAgent, docLoader, false);
		this.def = new BatikDiagramElementFactory((Kite9BridgeContext) ctx);
		this.transFact = TransformerFactory.newInstance();
		this.transFact.setErrorListener(new DefaultErrorHandler(true));
		setTranscodingHints(initTranscodingHints());
	}

	protected TranscodingHints initTranscodingHints() {
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, CachingSVGDOMImplementation.SVG_NAMESPACE_URI);
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, domImpl);
		return hints;
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
					throw new Kite9XMLProcessingException("Couldn't load broken.svg", e1, e);
				}
			}
			
			
			
		};
	}
	
	public UserAgent getUserAgent() {
		return userAgent;
	}

	@Override
	public Kite9BridgeContext createBridgeContext(String version) {
		return (Kite9BridgeContext) ctx;
	}

	@Override
	public Kite9BridgeContext createBridgeContext() {
		return (Kite9BridgeContext) super.createBridgeContext();
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

	public static Diagram lastDiagram; // for testing only
	public static AbstractArrangementPipeline lastPipeline; // testing only
	public static Kite9BridgeContext lastContext; //testing only
	public static Document lastOutputDocument; // testing only

	private Document outputDocument;

	protected void transcode(Document input, String uri, TranscoderOutput output) throws TranscoderException {
		try {
			// turn into SVG
			outputDocument = handleTransformToAdl(input);

			// prepare context + css
			input.setDocumentURI(uri);
			setupBridgeContext();
			ensureCSSEngine((SVGOMDocument) outputDocument);

			// create GVT tree
			this.builder = new GVTBuilder();
			GraphicsNode gvtRoot = this.builder.build(this.ctx, outputDocument);

			// handle text-wrapping
			TextWrapProcessor wrapProcessor = new TextWrapProcessor((Kite9BridgeContext) this.ctx);
			wrapProcessor.processContents(outputDocument.getDocumentElement());

			// create diagram element structure
			DiagramStructureProcessor p = new DiagramStructureProcessor(def, (Kite9BridgeContext) ctx);
			p.processContents(outputDocument.getDocumentElement());
			for (Diagram d: p.getDiagrams()) {

				// arrange diagram
				BasicArrangementPipeline pipeline = new BasicArrangementPipeline(def, new BasicCompleteDisplayer(false));
				d = pipeline.arrange(d);
				lastDiagram = d;
				lastPipeline = pipeline;
				lastContext = (Kite9BridgeContext) ctx;

			}


			// position diagram OR produce new output
			XMLProcessor postProcessor = buildOutputProcessor(outputDocument);
			postProcessor.processContents(outputDocument.getDocumentElement());
			lastOutputDocument = outputDocument;
		} catch (Exception e) {
			String s = new XMLHelper().toXML(input);
			log.error("Problem with XML: ",e);
			throw new Kite9XMLProcessingException("Transcoder problem: "+e.getMessage(), e, s, null);
		}
	}

	private Document handleTransformToAdl(Document input) throws Exception {
		String template = input.getDocumentElement().getAttribute("template");

		if ((template == null) || (template.length() == 0)) {
			return input;
		}

		URI uri = new URI(template);
		URI baseUri = uri.resolve("/");
		URI templatePath = uri.resolve("");

		// load the transform document
		Transformer trans = (Transformer) cache.get(template, TRANSFORMER);
		if (trans == null) {
			Source source = new StreamSource(new FileInputStream(template));
			source.setSystemId(template);
			trans = transFact.newTransformer(source);
			trans.setParameter("base-uri", baseUri.toString());
			trans.setParameter("template-uri", uri.toString());
			trans.setParameter("template-path", templatePath.toString());
			cache.set(template, TRANSFORMER, trans);
		}

		SVG12OMDocument r = new SVG12OMDocument(null, domImpl);
		Source inSource = new DOMSource(input);
		DOMResult result = new DOMResult(r);
		trans.transform(inSource, result);
		Document out = (Document) result.getNode();
		return out;

	}


	protected XMLProcessor buildOutputProcessor(Document input) {
		Kite9BridgeContext ctx = createBridgeContext();
		if (Boolean.TRUE == hints.get(KEY_ENCAPSULATING)) {
			// in this mode, we are converting the whole diagram into a single SVG file without
			// external references.
			return new Kite9InliningProcessor(ctx, new XPathValueReplacer(ctx), getUserAgent());
		} else {
			// this version is an "editable" svg diagram, which still uses stylesheets etc.
			return new DiagramPositionProcessor(ctx, new XPathValueReplacer(ctx));
		}
	}



	
	protected void setupBridgeContext() {
		Kite9BridgeContext ctx = createBridgeContext();
		if (Boolean.TRUE == hints.get(KEY_ENCAPSULATING)) {
			ctx.setTextAsGlyphs(true);
		} else {
			ctx.setTextAsGlyphs(false);
		}
	}

	public void ensureCSSEngine(SVGOMDocument input) {
		if (input.getCSSEngine() == null) {
			CSSEngine engine = domImpl.createCSSEngine(input, createBridgeContext());
			if (getTranscodingHints().get(KEY_MEDIA) != null) {
				engine.setMedia(getTranscodingHints().get(KEY_MEDIA).toString());
			} else {
				engine.setMedia("screen");
			}
			input.setCSSEngine(engine);
		}
	}

	@Override
	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		Document document;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			String uri = input.getURI();
			InputSource is;

			if (input.getInputStream() != null) {
				is = new InputSource(input.getInputStream());
			} else if (input.getReader() != null) {
				is = new InputSource(input.getReader());
			} else if (uri != null) {
				is = new InputSource(uri);
			} else {
				throw new UnsupportedOperationException();
			}

			is.setSystemId(uri);

			document = builder.parse(is);
		} catch (Exception e) {
			throw new TranscoderException("Couldn't create Dom document", e);
		}

		try {
			transcode(document, input.getURI(), output);
		} finally {
			writeSVGToOutput(outputDocument, output);
		}
	}

	/** 
	 * Writes the SVG content held by the svgGenerator to the
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