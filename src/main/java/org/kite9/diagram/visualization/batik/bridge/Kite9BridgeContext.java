package org.kite9.diagram.visualization.batik.bridge;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.batik.element.AbstractXMLDiagramElement;
import org.kite9.diagram.visualization.batik.element.Templater;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.serialization.Kite9DocumentFactory;

/**
 * The Kite9 bridge context has to manage the conversion of XML elements into {@link GraphicsNode} 
 * contents.   Since we also have `template` functionality now, it also has to manage loading 
 * templates correctly, and we'll use the {@link DocumentLoader} to handle this.
 * 
 * @author robmoffat
 *
 */
public final class Kite9BridgeContext extends SVG12BridgeContext {
	
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

	private Templater templater;
	
	public Kite9BridgeContext(UserAgent userAgent, Kite9DocumentFactory dbf) {
		this(userAgent, new Kite9DocumentLoader(userAgent, dbf));
		templater = new Templater(getDocumentLoader());
	}

	public boolean isInteractive() {
		return false;
	}

	public boolean isDynamic() {
		return false;
	}
	

	@Override
	public void registerSVGBridges() {
		super.registerSVGBridges();
		putBridge(new Kite9DiagramBridge(this));
		putBridge(new Kite9GBridge());
		putBridge(new TextBridge());
	}
	
	/**
	 * This needs to copy the template XML source into the destination.
	 */
	public void handleTemplateElement(XMLElement in, DiagramElement out) {
		templater.handleTemplateElement(in, (AbstractXMLDiagramElement) out);
	}

	public void registerDiagramRenderedSize(Diagram d) {
		RectangleRenderingInformation rri = d.getRenderingInformation();
		double width = rri.getPosition().x()+rri.getSize().getWidth();
		double height = rri.getPosition().y()+rri.getSize().getHeight();
		double oldWidth = getDocumentSize().getWidth();
		double oldHeight = getDocumentSize().getHeight();
		setDocumentSize(new Dimension2DDouble(Math.max(width,  oldWidth), Math.max(height, oldHeight)));
	}
	

}