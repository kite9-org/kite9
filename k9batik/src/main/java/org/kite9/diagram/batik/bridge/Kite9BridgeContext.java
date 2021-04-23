package org.kite9.diagram.batik.bridge;

import kotlin.reflect.KClass;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.*;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12BridgeExtension;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.util.SAXIOException;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.common.range.IntegerRange;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.managers.IntegerRangeValue;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Rectangle2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.Placement;
import org.kite9.diagram.model.style.Measurement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.xpath.XPathResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Kite9 bridge context has to manage the conversion of XML elements into {@link GraphicsNode} 
 * contents.   Since we also have `template` functionality now, it also has to manage loading 
 * templates correctly, and we'll use the {@link DocumentLoader} to handle this.
 * 
 * @author robmoffat
 *
 */
public class Kite9BridgeContext extends SVG12BridgeContext implements ElementContext {

	private Mark end;

	public Kite9BridgeContext(UserAgent userAgent, DocumentLoader loader, boolean textAsGlyphs) {
		super(userAgent, loader);
		setTextAsGlyphs(textAsGlyphs);
	}
	
	public void setTextAsGlyphs(boolean textAsGlyphs) {
//		if (!textAsGlyphs) {
//			setTextPainter(new LocalRenderingFlowTextPainter());
//		} else {
//			setTextPainter(null);
//		}
	}
	
	/**
	 * Setting this true allows us to keep track of XML-GraphicsNode mapping.
	 */
	public boolean isInteractive() {
		return true;	
	}

	/**
	 * Allows us to change the document and update the GraphicsNode tree.
	 */
	public boolean isDynamic() {
		return true;
	}

	public void registerDiagramRenderedSize(Diagram d) {
		RectangleRenderingInformation rri = d.getRenderingInformation();
		double width = rri.getPosition().x()+rri.getSize().getW();
		double height = rri.getPosition().y()+rri.getSize().getH();
		double oldWidth = getDocumentSize().getWidth();
		double oldHeight = getDocumentSize().getHeight();
		setDocumentSize(new Dimension2DDouble(Math.max(width,  oldWidth), Math.max(height, oldHeight)));
	}
	
	private ParsedURL resourceURL;
	
	public void setNextOperationResourceURL(ParsedURL url) {
		this.resourceURL = url;
	}

	public ParsedURL getAndClearResourceURL() {
		ParsedURL out = resourceURL;
		this.resourceURL = null;
		return out;
	}

	@Override
	public void setDocument(Document document) {
		super.setDocument(document);
	}

	@Override
	public void setGVTBuilder(GVTBuilder gvtBuilder) {
		super.setGVTBuilder(gvtBuilder);
	}

	@Override
	public void initializeDocument(Document document) {
		super.initializeDocument(document);
	}

	/**
	 * Adding support for SVG1.2, whether version is specified or not.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List getBridgeExtensions(Document doc) {
		List<BridgeExtension> out = (List<BridgeExtension>) super.getBridgeExtensions(doc);
		for (int i = 0; i < out.size(); i++) {
			BridgeExtension be = out.get(i);
			if (be instanceof SVGBridgeExtension) {
				// upgrade it
				out.set(i, new SVG12BridgeExtension());
			}
		}
		return out;
	}

	/**
     * This is a duplicate of the original method, but 
     * contains better error-handling, so we don't swallow the exception.
     */
    public Node getReferencedNode(Element e, String uri) {
        SVGDocument document = (SVGDocument)e.getOwnerDocument();
        URIResolver ur = createURIResolver(document, documentLoader);
        Node ref;
        
        try {
            ref = ur.getNode(uri, e);
            if (ref == null) {
                throw new BridgeException(this, e, ERR_URI_BAD_TARGET,
                                          new Object[] {uri});
            } else {
                SVGOMDocument refDoc =
                    (SVGOMDocument) (ref.getNodeType() == Node.DOCUMENT_NODE
                                       ? ref
                                       : ref.getOwnerDocument());
                // This is new rather than attaching this BridgeContext
                // with the new document we now create a whole new
                // BridgeContext to go with the new document.
                // This means that the new document has it's own
                // world of stuff and it should avoid memory leaks
                // since the new document isn't 'tied into' this
                // bridge context.
                if (refDoc != document) {
                    createSubBridgeContext(refDoc);
                }
                return ref;
            }
        } catch (SAXIOException ex) {
        	// to be consistent with safari and chome, if an element reference is on a stylesheet, then 
        	// we should take the reference from the current document.
        	try {
        		ParsedURL pUrl = new ParsedURL(uri);
        		String fragment = pUrl.getRef();
        		ref = ur.getNode("#"+ fragment, e);
        		return ref;
        	} catch (Exception ex2) {
        		// throw the original exception
        		throw new Kite9XMLProcessingException("Problem with getting URL:"+uri, ex, e);
        	}
        } catch (Exception ex) {
            throw new Kite9XMLProcessingException("Problem with getting URL:"+uri, ex, e);
        }
    }

	@Override
	public double getCssStyleDoubleProperty(String prop, Element e) {
		Value v = getCSSValue(prop, e);
		return (v instanceof FloatValue) ? v.getFloatValue() : 0.0;
	}


	@NotNull
	@Override
	public Placement getCSSStylePlacementProperty(@NotNull String prop, @NotNull Element e) {
		Value v = getCSSValue(prop, e);
		if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
			return new Placement(Measurement.PERCENTAGE, v.getFloatValue());
		} else if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
			return new Placement(Measurement.PIXELS, v.getFloatValue());
		} else {
			return new Placement(Measurement.PERCENTAGE, 50.0F);
		}
	}

	private Value getCSSValue(String prop, Element e) {
		if (e instanceof CSSStylableElement) {
			CSSEngine cssEngine = ((SVGOMDocument) e.getOwnerDocument()).getCSSEngine();
			int idx = cssEngine.getPropertyIndex(prop);
			Value v = cssEngine.getComputedStyle((CSSStylableElement) e, null, idx);
			return v;
		} else {
			return null;
		}
	}

	@Override
	public String getCssStyleStringProperty(String prop, Element e) {
		Value v = getCSSValue(prop, e);
		return ((v == null) || (v instanceof InheritValue)) ? null : v.getStringValue();
	}


	@Override
	public <X> X getCSSStyleEnumProperty(String prop, Element e, KClass<X> c) {
		Value v = getCSSValue(prop, e);
		return  v == null ? null : (X) ((EnumValue)v).getTheValue();
	}

	@Override
	public IntegerRange getCSSStyleRangeProperty(String prop, Element e) {
		IntegerRangeValue v = (IntegerRangeValue) getCSSValue(prop, e);
		return v;
	}


	public Placement getConnectionAlignment(String prop, Element e) {
		Value v = getCSSValue(prop, e);

		if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
			return new Placement(Measurement.PERCENTAGE, v.getFloatValue());
		} else if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
			return new Placement(Measurement.PIXELS, v.getFloatValue());
		}

		return Placement.Companion.getNONE();
	}

	@Override
	public DiagramElement getReferencedElement(String id, Element e) {
		Document ownerDocument = e.getOwnerDocument();
		Element r = ownerDocument.getElementById(id);
		DiagramElement de = getRegisteredDiagramElement(r);
		return de;
	}

	@Override
	public String getReference(String prop, Element e) {
		String xpath = getCssStyleStringProperty(prop, e);
		return evaluateXPath(xpath, e);
	}

	@Override
	public Kite9ProcessingException contextualException(String reason, Throwable t, Element e) {
		return new Kite9XMLProcessingException(reason, t, e);
	}

	@Override
	public Kite9ProcessingException contextualException(String reason, Element e) {
		return contextualException(reason, null, e);
	}


	private Map<Element, DiagramElement> xmlToDiagram = new HashMap<>();
	private Map<DiagramElement, List<DiagramElement>> children = new HashMap<>();

	@Override
	public void addChild(DiagramElement parent, DiagramElement c) {
		List<DiagramElement> contents = children.getOrDefault(parent, new ArrayList<>());
		contents.add(c);
		children.putIfAbsent(parent, contents);
	}


	@Override
	public List<DiagramElement> getChildDiagramElements(DiagramElement theElement) {
		return children.getOrDefault(theElement, new ArrayList<>());
	}


	@Override
	public String evaluateXPath(String x, Element e) {
		SVGOMDocument ownerDocument = (SVGOMDocument) e.getOwnerDocument();
		XPathResult result = (XPathResult) ownerDocument.evaluate(x, e, ownerDocument.createNSResolver(e), XPathResult.STRING_TYPE, null);
		return result.getStringValue();
	}

	@Override
	public void register(Element x, DiagramElement out) {
		xmlToDiagram.put(x, out);
	}

	@Override
	public DiagramElement getRegisteredDiagramElement(Element x) {
		return xmlToDiagram.get(x);
	}

	@Nullable
	@Override
	public Rectangle2D bounds(@NotNull Element x) {
		GraphicsNode gn = getGraphicsNode(x);
		java.awt.geom.Rectangle2D g = gn.getBounds();
		if (g==null) {
			return null;
		}
		return convertRect(g);
	}

	@NotNull
	private Rectangle2D convertRect(java.awt.geom.Rectangle2D g) {
		return new Rectangle2D(g.getX(), g.getY(), g.getWidth(), g.getHeight());
	}

	@NotNull
	@Override
	public double textWidth(@NotNull String s, @NotNull Element inside) {
		GraphicsNode gn = getGraphicsNode(inside);
		if (gn instanceof TextNode) {
			TextNode tn = (TextNode) gn;
			int idx = tn.getText().indexOf(s);
			TextPainter tp = tn.getTextPainter();
			Mark start = tp.getMark(tn, idx, true);
			Mark end = tp.getMark(tn, idx + s.length() - 1, false);
			Shape shape = tp.getHighlightShape(start, end);
			java.awt.geom.Rectangle2D sb = shape.getBounds();
			return sb.getWidth();
		} else {
			return 0;
		}
	}

	@Override
	public double getCssUnitSizeInPixels(@NotNull String prop, @NotNull Element e) {
		CSSContext ctx = getCSSEngineForElement(e).getCSSContext();
		UnitProcessor.Context me = new UnitProcessor.Context() {

			public Element getElement() { return null; }
			public float getFontSize() { return 0; }
			public float getXHeight() { return 0; }
			public float getViewportHeight() {return 0;}
			public float getViewportWidth() {return 0;}

			public float getPixelUnitToMillimeter() {
				return ctx.getPixelUnitToMillimeter();
			}

			public float getPixelToMM() {
				return ctx.getPixelToMillimeter();
			}

		};
		float f = UnitProcessor.svgToUserSpace("1"+prop, "", UnitProcessor.HORIZONTAL_LENGTH, me);
		return f;
	}

}