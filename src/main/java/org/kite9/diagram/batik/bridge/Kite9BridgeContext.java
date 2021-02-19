package org.kite9.diagram.batik.bridge;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.*;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12BridgeExtension;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.util.SAXIOException;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.kite9.diagram.batik.text.LocalRenderingFlowTextPainter;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.common.range.IntegerRange;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.elements.*;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.managers.IntegerRangeValue;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.ConnectionAlignment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.svg.SVGDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * The Kite9 bridge context has to manage the conversion of XML elements into {@link GraphicsNode} 
 * contents.   Since we also have `template` functionality now, it also has to manage loading 
 * templates correctly, and we'll use the {@link DocumentLoader} to handle this.
 * 
 * @author robmoffat
 *
 */
public class Kite9BridgeContext extends SVG12BridgeContext implements ElementContext {
	
	
	public Kite9BridgeContext(UserAgent userAgent, DocumentLoader loader, XMLDiagramElementFactory factory, boolean textAsGlyphs) {
		super(userAgent, loader);
		this.factory = factory;
		factory.setElementContext(this);
		setTextAsGlyphs(textAsGlyphs);
		dBridge = new Kite9DiagramBridge(factory);
	}
	
	public void setTextAsGlyphs(boolean textAsGlyphs) {
		if (!textAsGlyphs) {
			setTextPainter(new LocalRenderingFlowTextPainter());
		} else {
			setTextPainter(null);
		}
	}
	
	/**
	 * Setting this true allows us to keep track of XML-GraphicsNode mapping.
	 */
	public boolean isInteractive() {
		return true;	
	}

	public boolean isDynamic() {
		return false;
	}

	private final XMLDiagramElementFactory factory;
	private final Kite9Bridge gBridge = new Kite9Bridge();
	private final Kite9DiagramBridge dBridge;
	
	@Override
	public void registerSVGBridges() {
		super.registerSVGBridges();
		putBridge(dBridge);
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

	
	
	@Override
	public Bridge getBridge(Element element) {
		if (element instanceof Kite9XMLElement) {
			if (((Kite9XMLElement) element).getDiagramElement() instanceof Diagram) {
				return dBridge;
			} else {
				return gBridge;
			}
		}
		
		return super.getBridge(element);
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
	public double getCssDoubleValue(String prop, Element e) {
		Value v = ((StyledKite9XMLElement) e).getCSSStyleProperty(prop);
		return v.getFloatValue();
	}

	@Override
	public String getCssStringValue(String prop, Element e) {
		Value v = ((StyledKite9XMLElement) e).getCSSStyleProperty(prop);
		return v == null ? null : v.getStringValue();
	}

	@Override
	public Object getCSSStyleProperty(String prop, Element e) {
    	Value v = ((StyledKite9XMLElement) e).getCSSStyleProperty(prop);
    	if (v == null) {
    		return null;
		}
    	return ((EnumValue) v).getTheValue();
    }

	@Override
	public List<DiagramElement> getChildDiagramElements(Element theElement, DiagramElement parent) {
		List<DiagramElement> out = new ArrayList<>();
		for(Kite9XMLElement child: ((Kite9XMLElement) theElement)) {
			DiagramElement de = child.getDiagramElement();
			out.add(de);
		}
		return out;
	}

	@Override
	public IntegerRange getCSSStyleRangeProperty(String prop, Element e) {
		IntegerRangeValue v = (IntegerRangeValue) ((StyledKite9XMLElement) e).getCSSStyleProperty(prop);
		return v;
	}


	public ConnectionAlignment getConnectionAlignment(String prop, Element e) {
		Value v = ((StyledKite9XMLElement) e).getCSSStyleProperty(prop);

		if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
			return new ConnectionAlignment(ConnectionAlignment.Measurement.PERCENTAGE, v.getFloatValue());
		} else if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
			return new ConnectionAlignment(ConnectionAlignment.Measurement.PIXELS, v.getFloatValue());
		}

		return ConnectionAlignment.Companion.getNONE();
	}

	@Override
	public DiagramElement getReferencedElement(String id, Element e) {
		ADLDocument owner = (ADLDocument) e.getOwnerDocument();
		Kite9XMLElement from = (Kite9XMLElement) owner.getChildElementById(owner, id);
		return from.getDiagramElement();
	}

	@Override
	public String getReference(String prop, Element e) {
		String ref = ((ReferencingKite9XMLElement)e).getIDReference(prop);
		return ref;
	}

	@Override
	public RuntimeException contextualException(String reason, Throwable t, Element e) {
		return new Kite9XMLProcessingException(reason, t, e);
	}

	@Override
	public RuntimeException contextualException(String reason, Element e) {
		return contextualException(reason, null, e);
	}
}