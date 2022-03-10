package org.kite9.diagram.batik.format;

import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.ERROR_INCOMPATIBLE_OUTPUT_TYPE;
import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.KEY_ESCAPED;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.bridge.GVTBuilder;
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
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jetbrains.annotations.NotNull;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.model.BatikDiagramElementFactory;
import org.kite9.diagram.batik.text.Kite9FontFamilyResolver;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.CachingSVGDOMImplementation;
import org.kite9.diagram.dom.Kite9DocumentFactory;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
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
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLFilter;

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

	/**
	 * This allows us to specify the name of the template used to transform the input document.
	 */
	public static final TranscodingHints.Key KEY_TEMPLATE = new StringKey();

	/**
	 * Allows us to set the viewBox of the SVG (if used in the template
	 */
	public static final TranscodingHints.Key KEY_VIEW_BOX = new StringKey();

	public static final String TRANSFORMER = "transformer";

	private final ADLExtensibleDOMImplementation domImpl;
	private final Kite9Log log = Kite9Log.Companion.instance(this);
	private final Kite9DocumentFactory docFactory;
	private final Kite9DocumentLoader docLoader;
	private final Cache cache;
	private final BatikDiagramElementFactory def;
	private final FontFamilyResolver ffr;
	protected final Map<String, String> variables = new HashMap<>();
	protected final XMLHelper xmlHelper;

	/**
	 * Just used in testing
	 */
	public Kite9SVGTranscoder() {
		this(Cache.NO_CACHE, new XMLHelper());
	}
	
	public Kite9SVGTranscoder(Cache c, XMLHelper xmlHelper) {
		super();
		this.handler = xmlHelper.getErrorHandler();
		this.cache = c;
		this.ffr = new Kite9FontFamilyResolver();
		this.domImpl = new ADLExtensibleDOMImplementation(c);
		this.docFactory = new Kite9DocumentFactory(domImpl, XMLResourceDescriptor.getXMLParserClassName(), (ErrorHandler) this.handler);
	    this.docLoader = new Kite9DocumentLoader(userAgent, docFactory, cache, (ErrorHandler) this.handler);
		setTranscodingHints(initTranscodingHints());
		this.ctx = new Kite9BridgeContext(userAgent, docLoader, ffr, variables);
		this.def = new BatikDiagramElementFactory((Kite9BridgeContext) ctx);
		this.xmlHelper = xmlHelper;
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
			public SVGDocument getBrokenLinkDocument(Element e, String url, String message) {
				try {
					InputStream broken = Kite9SVGTranscoder.class.getResourceAsStream("/broken.svg");
					return (SVGDocument) docLoader.loadDocument(url, broken);
				} catch (IOException e1) {
					throw new Kite9XMLProcessingException("Couldn't load broken.svg", e1, e);
				}
			}

			@Override
			public FontFamilyResolver getFontFamilyResolver() {
				return ffr;
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
			input.setDocumentURI(uri);
			outputDocument = handleTransformToSvg(input);

			// prepare context + css
			outputDocument.setDocumentURI(uri);
			createBridgeContext();
			ensureCSSEngine((SVGOMDocument) outputDocument);

			// create GVT tree
			this.builder = new GVTBuilder();
			this.builder.build(this.ctx, outputDocument);

			// handle text-wrapping
			TextWrapProcessor wrapProcessor = new TextWrapProcessor((Kite9BridgeContext) this.ctx);
			wrapProcessor.processContents(outputDocument.getDocumentElement());

			// create diagram element structure
			DiagramStructureProcessor p = new DiagramStructureProcessor(def, (Kite9BridgeContext) ctx);
			p.processContents(outputDocument.getDocumentElement());
			for (Diagram d: p.getDiagrams()) {

				// arrange diagram
				BasicArrangementPipeline pipeline = new BasicArrangementPipeline(def, new BasicCompleteDisplayer(false));
				try {
					d = pipeline.arrange(d);
				} finally {
					lastDiagram = d;
					lastPipeline = pipeline;
					lastContext = (Kite9BridgeContext) ctx;
				}
			}


			// position diagram OR produce new output
			XMLProcessor postProcessor = buildOutputProcessor(outputDocument);
			postProcessor.processContents(outputDocument.getDocumentElement());
			lastOutputDocument = outputDocument;
		} catch (Exception e) {
			log.error("Problem with XML: ",e);
			throw new Kite9XMLProcessingException("Transcoder problem: "+e.getMessage(), e, outputDocument == null ? input : outputDocument);
		}
	}

	protected String getTemplateUri(Document input) {
		String template = (String) hints.get(KEY_TEMPLATE);

		if ((template == null) || (template.length() == 0)) {
			template = input.getDocumentElement().getAttributeNS(Kite9Namespaces.XSL_TEMPLATE_NAMESPACE, "template");
		}

		if ((template == null) || (template.length() == 0)) {
			if (Kite9Namespaces.ADL_NAMESPACE.equals(input.getDocumentElement().getNamespaceURI())) {
				// default to the basic template
				return "/public/templates/basic/basic-template.xsl";
			}
		}
		return template;
	}

	private Document handleTransformToSvg(Document input)  {
		Document out;

		String template = getTemplateUri(input);
		Transformer trans;


		try {
			if ((template == null) || (template.length() == 0)) {
				// returns the 'null' transformer
				trans = xmlHelper.newTransformer(true);
			} else {
				trans = getTransformer(input, template);
			}

			SVG12OMDocument r = new SVG12OMDocument(null, domImpl);
			Source inSource = new DOMSource(input);
			DOMResult result = new DOMResult(r);
			trans.transform(inSource, result);
			out = (Document) result.getNode();
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't transform document with "+template, e, input);
		}

		if (out.getDocumentElement() == null) {
			throw new Kite9XMLProcessingException("Empty transformed document: "+input.getDocumentURI(), null, null, null);
		}

		return out;
	}

	@NotNull
	private Transformer getTransformer(Document input, String template) throws Exception {
		URI uri = new URI(input.getDocumentURI());
		URI templateURI = uri.resolve(template);
		URI baseUri = uri.resolve("/");
		URI templatePath = templateURI.resolve("");

		// load the transform document
		Transformer trans = (Transformer) cache.get(template, TRANSFORMER);
		if (trans == null) {
			ParsedURL parsedTemplateUri = new ParsedURL(templateURI.toString());
			Source source = new StreamSource(parsedTemplateUri.openStream());
			source.setSystemId(templateURI.toString());
			trans = xmlHelper.newTransformer(source, false);
			trans.setParameter("base-uri", baseUri.toString());
			trans.setParameter("template-uri", uri.toString());
			trans.setParameter("template-path", templatePath.toString());
			
			for (String key : variables.keySet()) {
				trans.setParameter(key, variables.get(key));
			}

			cache.set(template, TRANSFORMER, trans);
		}
		return trans;
	}

	protected XMLProcessor buildOutputProcessor(Document input) {
		Kite9BridgeContext ctx = createBridgeContext();
		if (Boolean.TRUE == hints.get(KEY_ENCAPSULATING)) {
			// in this mode, we are converting the whole diagram into a single SVG file without
			// external references.
			return new Kite9InliningProcessor(ctx, new XPathValueReplacer(ctx), getUserAgent(), xmlHelper);
		} else {
			// this version is an "editable" svg diagram, which still uses stylesheets etc.
			return new DiagramPositionProcessor(ctx, new XPathValueReplacer(ctx));
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
			String uri = input.getURI();
			if (input.getDocument() != null) {
				document = input.getDocument();
			} else if (input.getInputStream() != null) {
				document = docFactory.createDocument(uri, input.getInputStream());
			} else if (input.getReader() != null) {
				document = docFactory.createDocument(uri, input.getReader());
			} else if (uri != null) {
				document = docFactory.createDocument(uri);
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (Exception e) {
			throw new TranscoderException("Couldn't create Dom document", e);
		}

		try {
			transcode(document, input.getURI(), output);
		} finally {
			if (outputDocument instanceof SVGOMDocument) {
				writeSVGToOutput(outputDocument, output);
			}
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
    
   	public Map<String, String> getVariables() {
		return variables;
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