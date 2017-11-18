package org.kite9.diagram.batik.bridge;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.kite9.diagram.batik.bridge.images.Kite9ImageElementBridge;
import org.kite9.diagram.batik.templater.DefsHandlingTemplater;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.framework.dom.Kite9DocumentFactory;
import org.kite9.framework.dom.XMLHelper;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Document;

/**
 * The Kite9 bridge context has to manage the conversion of XML elements into {@link GraphicsNode} 
 * contents.   Since we also have `template` functionality now, it also has to manage loading 
 * templates correctly, and we'll use the {@link DocumentLoader} to handle this.
 * 
 * @author robmoffat
 *
 */
public class Kite9BridgeContext extends SVG12BridgeContext {
	
	public Kite9BridgeContext(UserAgent userAgent, DocumentLoader loader) {
		super(userAgent, loader);
		this.setDocumentSize(new Dimension2DDouble(0,0));
	}
	
	static class Kite9DocumentLoader extends DocumentLoader {

		public Kite9DocumentLoader(UserAgent userAgent, Kite9DocumentFactory dbf) {
			super(userAgent);
			this.documentFactory = dbf;
		}
	}

	public Kite9BridgeContext(UserAgent userAgent, Kite9DocumentFactory dbf) {
		this(userAgent, new Kite9DocumentLoader(userAgent, dbf));
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
	
	private Kite9GBridge gBridge = new Kite9GBridge();
	
	@Override
	public void registerSVGBridges() {
		super.registerSVGBridges();
		putBridge(new Kite9DiagramBridge(this));
	}
	
	/**
	 * This needs to copy the template XML source into the destination.
	 */
	public void handleTemplateElement(Kite9XMLElement in, DiagramElement out) {
		new DefsHandlingTemplater(getDocumentLoader()).handleTemplateElement(in, out);
	}

	public void registerDiagramRenderedSize(Diagram d) {
		RectangleRenderingInformation rri = d.getRenderingInformation();
		double width = rri.getPosition().x()+rri.getSize().getWidth();
		double height = rri.getPosition().y()+rri.getSize().getHeight();
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
	public Bridge getBridge(String namespaceURI, String localName) {
		if (XMLHelper.KITE9_NAMESPACE.equals(namespaceURI)) {
			if (!XMLHelper.DIAGRAM_ELEMENT.equals(localName)) {
				return gBridge;
			}
		}
		return super.getBridge(namespaceURI, localName);
	}

	
	
	
}