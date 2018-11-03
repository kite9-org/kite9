package org.kite9.diagram.batik.bridge;

import java.util.List;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeExtension;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGBridgeExtension;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12BridgeExtension;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.kite9.diagram.batik.text.LocalRenderingFlowRootElementBridge;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.template.BasicTemplater;
import org.kite9.diagram.dom.processors.xpath.XPathValueReplacer;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	
	private Kite9Bridge gBridge = new Kite9Bridge();
	private Kite9DiagramBridge dBridge = new Kite9DiagramBridge();
	
	private XMLProcessor xmlProcessor;
	
	@Override
	public void registerSVGBridges() {
		super.registerSVGBridges();
		putBridge(new Kite9DiagramBridge());
		putBridge(new LocalRenderingFlowRootElementBridge());
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
	@SuppressWarnings({"unchecked", "cast", "rawtypes"})
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

	public XMLProcessor getXMLPreProcessor() {
		if (xmlProcessor == null) {
			xmlProcessor = createXMLPreProcessor();
		}
		return xmlProcessor;
	}

	protected XMLProcessor createXMLPreProcessor() {
		XPathValueReplacer vr = new XPathValueReplacer((ADLDocument) getDocument());
		return new BasicTemplater(vr, (Kite9DocumentLoader)  getDocumentLoader());
	}
	
}