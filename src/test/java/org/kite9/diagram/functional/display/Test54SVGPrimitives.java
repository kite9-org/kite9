package org.kite9.diagram.functional.display;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.junit.Test;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.serialization.ADLExtensibleDOMImplementation;
import org.kite9.framework.serialization.Kite9DocumentFactory;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class Test54SVGPrimitives extends AbstractDisplayFunctionalTest {

	
	public final class Kite9PNGTranscoder extends PNGTranscoder {
		
		@Override
		protected DocumentFactory createDocumentFactory(DOMImplementation domImpl, String parserClassname) {
			return new Kite9DocumentFactory(parserClassname);
		}
		
		  /**
	     * Transcodes the specified Document as an image in the specified output.
	     *
	     * @param document the document to transcode
	     * @param uri the uri of the document or null if any
	     * @param output the ouput where to transcode
	     * @exception TranscoderException if an error occured while transcoding
	     */
	    protected void transcode(Document document,
	                             String uri,
	                             TranscoderOutput output)
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


	        ADLDocument svgDoc = (ADLDocument)document;
	        DiagramXMLElement root = svgDoc.getDocumentElement();
	        ctx = createBridgeContext(svgDoc);

	        // build the GVT tree
	        builder = new GVTBuilder();
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


	}

	private void transcode(String s) throws Exception {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, m.getName(), m.getName()+"-graph.png");
		TranscoderInput in = new TranscoderInput(new StringReader(s));
		TranscoderOutput out = new TranscoderOutput(new FileOutputStream(f));
		PNGTranscoder transcoder = new Kite9PNGTranscoder();
		
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "diagram");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, XMLHelper.KITE9_NAMESPACE);
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, new ADLExtensibleDOMImplementation());
		
		
		transcoder.setTranscodingHints(hints);
		transcoder.transcode(in, out);
	}
	
	@Test
	public void test_54_1_EmptyDiagram() throws IOException {
		String someXML = diagramOpen()+ diagramClose();
		renderDiagram(someXML);
	}

	@Test
	public void test_54_2_TextPrimitive() throws IOException {
		String someXML = diagramOpen() + textOpen()+"The Text"+textClose()+diagramClose();
		renderDiagram(someXML);
	}
	
	@Test
	public void test_54_3_TestTranscoder() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("simple.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		transcode(out.toString());
	}
	
	@Test
	public void test_54_4_TestTranscoderOnKite9() throws Exception {
		String someXML = diagramOpen()+ diagramClose();
		transcode(someXML);
	}

	@Test
	public void test_54_3_ResizeablePrimitive() throws IOException {
		String someXML = diagramOpen() + 
					containerOpen()+
				textOpen()+
				"Internal Text" +
				textClose() +
				containerClose() +
				diagramClose();
		renderDiagram(someXML);
	}
	
	private String diagramClose() {
		return "</diagram>";
	}

	private String diagramOpen() {
		return "<diagram xmlns='"+XMLHelper.KITE9_NAMESPACE+"' id='one' style='type: diagram; padding: 30px; fill: white; stroke: grey; stroke-width: 3px; '>";
	}
	

	private String textClose() {
		return "</someelement>";
	}

	private String textOpen() {
		return "<someelement style='type: connected; sizing: text; '>";
	}

	private String containerOpen() {
		return "<container style='type: container'>";
	}

	private String containerClose() {
		return "</container>";
	}
}
