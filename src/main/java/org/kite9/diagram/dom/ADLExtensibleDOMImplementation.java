package org.kite9.diagram.dom;

import java.net.URL;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.css.dom.CSSOMSVGViewCSS;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SVG12CSSEngine;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.RGBColorValue;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.engine.value.svg.MarkerManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.util.HashTable;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.ContentsElement;
import org.kite9.diagram.dom.elements.GenericKite9XMLElement;
import org.kite9.diagram.dom.managers.ConnectionAlignmentLengthManager;
import org.kite9.diagram.dom.managers.EnumManager;
import org.kite9.diagram.dom.managers.FourDirectionalShorthandManager;
import org.kite9.diagram.dom.managers.GridSizeManager;
import org.kite9.diagram.dom.managers.GridSizeShorthandManager;
import org.kite9.diagram.dom.managers.IntegerRangeManager;
import org.kite9.diagram.dom.managers.LinkLengthManager;
import org.kite9.diagram.dom.managers.OccupiesShorthandManager;
import org.kite9.diagram.dom.managers.PaddingLengthManager;
import org.kite9.diagram.dom.managers.SizeShorthandManager;
import org.kite9.diagram.dom.managers.TemplateManager;
import org.kite9.diagram.dom.managers.TraversalShorthandManager;
import org.kite9.diagram.dom.managers.WidthHeightManager;
import org.kite9.diagram.dom.model.DiagramElementFactory;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.ConnectionsSeparation;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.RectangularElementUsage;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.stylesheets.StyleSheet;

import com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import com.sun.org.apache.xerces.internal.dom.DOMOutputImpl;
import com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl;

/**
 * Extends the SVG DOM Implementation by adding Kite9 Namespace support, and
 * handing for Kite9 CSS definitions.
 * 
 * @author robmoffat
 *
 */
public class ADLExtensibleDOMImplementation extends SVG12DOMImplementation implements Logable, DOMImplementationLS {
	
	private final Kite9Log log = new Kite9Log(this);
	
	public static final boolean USE_GENERIC_XML_ELEMENT = true;

	public ADLExtensibleDOMImplementation() {
		super();
		registerCustomElementFactory(XMLHelper.KITE9_NAMESPACE, XMLHelper.CONTENTS_ELEMENT, new ElementFactory() {
			 
			public Element create(String prefix, Document doc) {
				ContentsElement out = new ContentsElement((ADLDocument) doc);
				out.setOwnerDocument(doc);
				return out;
			}
		});
		
		// PADDING CSS
		registerCustomCSSShorthandManager(new FourDirectionalShorthandManager(CSSConstants.PADDING_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_LEFT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_RIGHT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_TOP_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_BOTTOM_PROPERTY));

		// MARGIN CSS
		// We are using Kite9 margin here to differentiate from the one in regular CSS (not sure if this is a good idea)
		registerCustomCSSShorthandManager(new FourDirectionalShorthandManager(CSSConstants.MARGIN_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_LEFT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_RIGHT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_TOP_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_BOTTOM_PROPERTY));
		
		
		// ELEMENT TYPE / SIZING / LAYOUT CONTROL
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ELEMENT_TYPE_PROPERTY, DiagramElementType.class, DiagramElementType.UNSPECIFIED, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ELEMENT_SIZING_PROPERTY, DiagramElementSizing.class, DiagramElementSizing.MINIMIZE, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ELEMENT_USAGE_PROPERTY, RectangularElementUsage.class, RectangularElementUsage.REGULAR, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.LAYOUT_PROPERTY, Layout.class, null, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.CONTENT_TRANSFORM, ContentTransform.class, ContentTransform.DEFAULT, false));
		
		// GRIDS
		registerCustomCSSValueManager(new IntegerRangeManager(CSSConstants.GRID_OCCUPIES_X_PROPERTY));
		registerCustomCSSValueManager(new IntegerRangeManager(CSSConstants.GRID_OCCUPIES_Y_PROPERTY));
		registerCustomCSSValueManager(new GridSizeManager(CSSConstants.GRID_ROWS_PROPERTY));
		registerCustomCSSValueManager(new GridSizeManager(CSSConstants.GRID_COLUMNS_PROPERTY));
		registerCustomCSSShorthandManager(new GridSizeShorthandManager());
		registerCustomCSSShorthandManager(new OccupiesShorthandManager());
		
		// CONNECTION TRAVERSAL
		registerCustomCSSShorthandManager(new TraversalShorthandManager());
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_BOTTOM_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_RIGHT_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_LEFT_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_TOP_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		
		// TEMPLATES
		registerCustomCSSValueManager(new TemplateManager());
		
		// CONNECTION SIDES
		registerCustomCSSValueManager(new EnumManager(CSSConstants.CONNECTIONS_PROPERTY, ConnectionsSeparation.class, ConnectionsSeparation.SAME_SIDE, false));
		
		// ALIGNMENT
		registerCustomCSSValueManager(new EnumManager(CSSConstants.VERTICAL_ALIGNMENT, VerticalAlignment.class, VerticalAlignment.CENTER, true));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.HORIZONTAL_ALIGNMENT, HorizontalAlignment.class, HorizontalAlignment.CENTER, true));
		
		registerCustomCSSShorthandManager(new FourDirectionalShorthandManager(CSSConstants.CONNECTION_ALIGN_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_LEFT_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_RIGHT_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_TOP_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_BOTTOM_PROPERTY));
		
		// LINK LENGTHS
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.LINK_INSET, 0f));
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.LINK_MINIMUM_LENGTH, 0f));
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.LINK_GUTTER, 0f));
		
		// TERMINATORS
		registerCustomCSSValueManager(new MarkerManager(CSSConstants.MARKER_START_REFERENCE));
		registerCustomCSSValueManager(new MarkerManager(CSSConstants.MARKER_END_REFERENCE));
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.MARKER_RESERVE, 0f));

		// RECTANGLE SIZING
		registerCustomCSSShorthandManager(new SizeShorthandManager(CSSConstants.RECT_MINIMUM_WIDTH, CSSConstants.RECT_MINIMUM_HEIGHT, CSSConstants.RECT_MINIMUM_SIZE));
		registerCustomCSSValueManager(new WidthHeightManager(CSSConstants.RECT_MINIMUM_WIDTH, 0f));
		registerCustomCSSValueManager(new WidthHeightManager(CSSConstants.RECT_MINIMUM_HEIGHT, 0f));
	}

	public static final RGBColorValue NO_COLOR = new RGBColorValue(
			new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, -1),
			new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, -1),
			new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, -1));


	@Override
	public Element createElementNS(AbstractDocument document, String namespaceURI, String qualifiedName) {
		if (USE_GENERIC_XML_ELEMENT) {
			if (XMLHelper.KITE9_NAMESPACE.equals(namespaceURI)) {
				if (!"contents".equals(qualifiedName)) {
					return createGenericADLElement(document, qualifiedName);
				}
			} 
		}
		
		return super.createElementNS(document, namespaceURI, qualifiedName);
	}

	public Element createGenericADLElement(AbstractDocument document, String qualifiedName) {
		return new GenericKite9XMLElement(qualifiedName, (ADLDocument) document);
	}

	public CSSStyleSheet createCSSStyleSheet(String title, String media) throws DOMException {
        throw new UnsupportedOperationException("StyleSheetFactory.createCSSStyleSheet is not implemented"); // XXX
	}

	public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException {
		ADLDocument result = new ADLDocument(this);
		result.setIsSVG12(false);		// prevent creation of extraneous xml:id fields.
        if (qualifiedName != null)
            result.appendChild(result.createElementNS(namespaceURI,
                                                      qualifiedName));
        return result;
	}
	

	public StyleSheet createStyleSheet(Node node, HashTable pseudoAttrs) {
        throw new UnsupportedOperationException("StyleSheetFactory.createStyleSheet is not implemented"); // XXX
	}
	
	

	public CSSEngine createCSSEngine(AbstractStylableDocument doc, CSSContext ctx, ExtendedParser ep, ValueManager[] vms, ShorthandManager[] sms) {
		if (doc.getCSSEngine() != null) {
			return doc.getCSSEngine();
		}
		
		ParsedURL durl = null; // ((ADLDocument)doc).getParsedURL();
		CSSEngine result = new SVG12CSSEngine(doc, durl, ep, vms, sms, ctx);
		
		ep.setErrorHandler(new ErrorHandler() {
			
			private String getLocation(CSSParseException arg0) {
				return "("+arg0.getLineNumber()+","+arg0.getColumnNumber()+")";
			}
			
			@Override
			public void warning(CSSParseException arg0) throws CSSException {
				log.send("Warning: "+getLocation(arg0), arg0);
			}
			
			@Override
			public void fatalError(CSSParseException arg0) throws CSSException {
				log.send("Fatal: "+getLocation(arg0), arg0);
			}
			
			@Override
			public void error(CSSParseException arg0) throws CSSException {
				log.send("Error: "+getLocation(arg0), arg0);
			}
		});

		URL url = getClass().getResource("resources/UserAgentStyleSheet.css");
		if (url != null) {
			ParsedURL purl = new ParsedURL(url);
			InputSource is = new InputSource(purl.toString());
			result.setUserAgentStyleSheet(result.parseStyleSheet(is, purl, "all"));
		}

		return result;
	}
	

	@Override
	public ViewCSS createViewCSS(AbstractStylableDocument doc) {
        return new CSSOMSVGViewCSS(doc.getCSSEngine());
	}
	
    private DiagramElementFactory diagramElementFactory;

	public DiagramElementFactory getDiagramElementFactory() {
		return diagramElementFactory;
	}

	public void setDiagramElementFactory(DiagramElementFactory diagramElementFactory) {
		this.diagramElementFactory = diagramElementFactory;
	}

	@Override
	public String getPrefix() {
		return "ADOM";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	
    public LSSerializer createLSSerializer() {
        return new DOMSerializerImpl();
    }
    /**
     * DOM Level 3 LS CR - Experimental.
     * Create a new empty input source.
     * @return  The newly created input object.
     */
    public LSInput createLSInput() {
            return new DOMInputImpl();
    }

    public LSOutput createLSOutput() {
        return new DOMOutputImpl();
    }

	@Override
	public LSParser createLSParser(short arg0, String arg1) throws DOMException {
		return ((DOMImplementationLS) CoreDOMImplementationImpl.getDOMImplementation()).createLSParser(arg0, arg1);
	}
    
    
}
