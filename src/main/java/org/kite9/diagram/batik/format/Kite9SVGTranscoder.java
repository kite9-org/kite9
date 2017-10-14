package org.kite9.diagram.batik.format;

import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.ERROR_INCOMPATIBLE_OUTPUT_TYPE;
import static org.apache.batik.transcoder.ToSVGAbstractTranscoder.KEY_ESCAPED;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.PreservingGVTBuilder;
import org.kite9.diagram.batik.element.DiagramElementFactoryImpl;
import org.kite9.diagram.batik.element.Templater;
import org.kite9.diagram.model.style.DiagramElementFactory;
import org.kite9.framework.dom.ADLExtensibleDOMImplementation;
import org.kite9.framework.dom.Kite9DocumentFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGSVGElement;
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
	
	protected void transcode(Document document, String uri, TranscoderOutput output) throws TranscoderException {
		document.setDocumentURI(uri);
		transcodeInner(document, uri, output, new PreservingGVTBuilder());
	}
	
	/**
     * Transcodes the specified Document as an image in the specified output.
     * Added this from superclass so we can override the builder.
     * Not even sure how much of this is still required - we should heavily edit this,
     * maybe not even extend the SVGAbstractTranscoder
     *
     * @param document the document to transcode
     * @param uri the uri of the document or null if any
     * @param output the ouput where to transcode
     * @param builder our GVT builder.
     * @exception TranscoderException if an error occured while transcoding
     */
    protected void transcodeInner(Document document,
                             String uri,
                             TranscoderOutput output, GVTBuilder builder)
            throws TranscoderException {

        if ((document != null) &&
            !(document.getImplementation() instanceof SVGDOMImplementation)) {
            DOMImplementation impl;
            impl = (DOMImplementation)hints.get(KEY_DOM_IMPLEMENTATION);
            // impl = SVGDOMImplementation.getDOMImplementation();
            document = DOMUtilities.deepCloneDocument(document, impl);
            if (uri != null) {
                ParsedURL url = new ParsedURL(uri);
                ((SVGOMDocument)document).setParsedURL(url);
            }
        }

        if (hints.containsKey(KEY_WIDTH))
            width = ((Float)hints.get(KEY_WIDTH)).floatValue();
        if (hints.containsKey(KEY_HEIGHT))
            height = ((Float)hints.get(KEY_HEIGHT)).floatValue();


        SVGOMDocument svgDoc = (SVGOMDocument)document;
        SVGSVGElement root = svgDoc.getRootElement();
        ctx = createBridgeContext(svgDoc);

        // flag that indicates if the document is dynamic
        boolean isDynamic =
            hints.containsKey(KEY_EXECUTE_ONLOAD) &&
             ((Boolean)hints.get(KEY_EXECUTE_ONLOAD)).booleanValue();

        GraphicsNode gvtRoot;
        try {
            if (isDynamic)
                ctx.setDynamicState(BridgeContext.DYNAMIC);

            gvtRoot = builder.build(ctx, svgDoc);

            // dispatch an 'onload' event if needed
            if (ctx.isDynamic()) {
                BaseScriptingEnvironment se;
                se = new BaseScriptingEnvironment(ctx);
                se.loadScripts();
                se.dispatchSVGLoadEvent();
                if (hints.containsKey(KEY_SNAPSHOT_TIME)) {
                    float t =
                        ((Float) hints.get(KEY_SNAPSHOT_TIME)).floatValue();
                    ctx.getAnimationEngine().setCurrentTime(t);
                } else if (ctx.isSVG12()) {
                    float t = SVGUtilities.convertSnapshotTime(root, null);
                    ctx.getAnimationEngine().setCurrentTime(t);
                }
            }
        } catch (BridgeException ex) {
            ex.printStackTrace();
            throw new TranscoderException(ex);
        }

        // get the 'width' and 'height' attributes of the SVG document
        float docWidth = (float)ctx.getDocumentSize().getWidth();
        float docHeight = (float)ctx.getDocumentSize().getHeight();

        setImageSize(docWidth, docHeight);

        // compute the preserveAspectRatio matrix
        AffineTransform Px;

        // take the AOI into account if any
        if (hints.containsKey(KEY_AOI)) {
            Rectangle2D aoi = (Rectangle2D)hints.get(KEY_AOI);
            // transform the AOI into the image's coordinate system
            Px = new AffineTransform();
            double sx = width / aoi.getWidth();
            double sy = height / aoi.getHeight();
            double scale = Math.min(sx,sy);
            Px.scale(scale, scale);
            double tx = -aoi.getX() + (width/scale - aoi.getWidth())/2;
            double ty = -aoi.getY() + (height/scale -aoi.getHeight())/2;
            Px.translate(tx, ty);
            // take the AOI transformation matrix into account
            // we apply first the preserveAspectRatio matrix
            curAOI = aoi;
        } else {
            String ref = new ParsedURL(uri).getRef();

            // XXX Update this to use the animated value of 'viewBox' and
            //     'preserveAspectRatio'.
            String viewBox = root.getAttributeNS
                (null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);

            if ((ref != null) && (ref.length() != 0)) {
                Px = ViewBox.getViewTransform(ref, root, width, height, ctx);
            } else if ((viewBox != null) && (viewBox.length() != 0)) {
                String aspectRatio = root.getAttributeNS
                    (null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
                Px = ViewBox.getPreserveAspectRatioTransform
                    (root, viewBox, aspectRatio, width, height, ctx);
            } else {
                // no viewBox has been specified, create a scale transform
                float xscale, yscale;
                xscale = width/docWidth;
                yscale = height/docHeight;
                float scale = Math.min(xscale,yscale);
                Px = AffineTransform.getScaleInstance(scale, scale);
            }

            curAOI = new Rectangle2D.Float(0, 0, width, height);
        }

        CanvasGraphicsNode cgn = getCanvasGraphicsNode(gvtRoot);
        if (cgn != null) {
            cgn.setViewingTransform(Px);
            curTxf = new AffineTransform();
        } else {
            curTxf = Px;
        }

        this.root = gvtRoot;
    }
	
	@Override
	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		super.transcode(input, output);
        Document doc = this.createDocument(output);
        ExtendedSVGGraphics2D svgGenerator = new ExtendedSVGGraphics2D(doc, rr, createElementNodeMapper());
        svgGenerator.setUnsupportedAttributes(null);// writes as text
        
        root.paint(svgGenerator);
        
        /** set precision
         ** otherwise Ellipses aren't working (for example) (because of Decimal format
         * modifications ins SVGGenerator Context
         */
        svgGenerator.getGeneratorContext().setPrecision(4);


        //svgGenerator.setSVGCanvasSize(new Dimension(vpW, vpH));

        Element svgRoot = svgGenerator.getRoot();

//        svgRoot.setAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE,
//                                String.valueOf( vpX ) + ' ' + vpY + ' ' +
//                               vpW + ' ' + vpH );

        // Now, write the SVG content to the output
        writeSVGToOutput(svgGenerator, svgRoot, output);
	}

	 private ElementNodeMapper createElementNodeMapper() {
		return new ElementNodeMapper() {
			
			@Override
			public GraphicsNode getGraphicsNode(Node node) {
				return ctx.getGraphicsNode(node);
			}
			
			@Override
			public Element getElement(GraphicsNode gn) {
				return ctx.getElement(gn);
			}
			
			public Templater getTemplater() {
				return ((Kite9BridgeContext) ctx).getTemplater();
			}
			
		};
	}

	/** Writes the SVG content held by the svgGenerator to the
     * <code>TranscoderOutput</code>. This method does nothing if the output already
     * contains a Document.
     */
    protected void writeSVGToOutput(SVGGraphics2D svgGenerator, Element svgRoot,
        TranscoderOutput output) throws TranscoderException {

        Document doc = output.getDocument();

        if (doc != null) return;

        // XMLFilter
        XMLFilter xmlFilter = output.getXMLFilter();
        if (xmlFilter != null) {
            handler.fatalError(new TranscoderException("" + ERROR_INCOMPATIBLE_OUTPUT_TYPE));
        }

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