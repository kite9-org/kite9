package org.kite9.diagram.visualization.batik.bridge;

import java.awt.geom.Dimension2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGAElementBridge;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.script.InterpreterPool;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.visualization.batik.BatikArrangementPipeline;
import org.kite9.diagram.visualization.batik.BatikDisplayer;
import org.kite9.diagram.visualization.batik.node.GraphicsNodeLookup;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.visualization.pipeline.full.ArrangementPipeline;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public final class Kite9BridgeContext extends SVG12BridgeContext implements GraphicsNodeLookup {

	public Kite9BridgeContext(UserAgent userAgent, DocumentLoader loader) {
		super(userAgent, loader);
	}

	public Kite9BridgeContext(UserAgent userAgent, InterpreterPool interpreterPool, DocumentLoader documentLoader) {
		super(userAgent, interpreterPool, documentLoader);
	}

	public Kite9BridgeContext(UserAgent userAgent) {
		super(userAgent);
	}

	public boolean isInteractive() {
		return false;
	}

	public boolean isDynamic() {
		return false;
	}
	
	private DiagramXMLElement theDiagram;
	private boolean sized = false;
	
	@Override
	public Bridge getBridge(Element element) {
		if (element instanceof XMLElement) {
			DiagramElement de = ((XMLElement) element).getDiagramElement();
			storeDiagramElement(element);
			
			
			
			return new Kite9DiagramGroupBridge(this);
		} else {
			return super.getBridge(element);
		}
	}

	private void storeDiagramElement(Element element) {
		if (element instanceof DiagramXMLElement) {
			if (theDiagram != null) {
				throw new Kite9ProcessingException("There should only be a single diagram element in the XML");
			}
			
			theDiagram = (DiagramXMLElement) element;
		}
	}

	@Override
	public Dimension2D getDocumentSize() {
		if ((sized == false) && (theDiagram != null)) {
			createPipeline().arrange(theDiagram);
			sized = true;
		}

		return super.getDocumentSize();
	}
	

	private ArrangementPipeline createPipeline() {
		return new BatikArrangementPipeline(new BatikDisplayer(false, 20, this));
	}
	
	private Map<XMLElement, GraphicsNode> mainMapping = new HashMap<>();
	

	@Override
	public GraphicsNode getNode(GraphicsLayerName gl, XMLElement element) {
		return mainMapping.get(element);
	}

	@Override
	public void storeNode(GraphicsLayerName gl, XMLElement element, GraphicsNode node) {
		mainMapping.put(element, node);
	}

	@Override
	public GVTBuilder getGVTBuilder() {
		return new GVTBuilder() {

			@Override
			protected void buildComposite(BridgeContext ctx, Element e, CompositeGraphicsNode parentNode) {
				CompositeGraphicsNode diagramNode = (CompositeGraphicsNode) mainMapping.get(theDiagram);
				if (e instanceof XMLElement) {
					// in this case, we add the elements to the diagram node, rather than the parent node.
				   for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			            if (n.getNodeType() == Node.ELEMENT_NODE) {
			                buildGraphicsNode(ctx, (Element)n, diagramNode);
			            }
			        }
					
				} else {
					super.buildComposite(ctx, e, parentNode);
				}
			}
			
		};
	}

	@Override
	public void registerSVGBridges() {
		super.registerSVGBridges();
		putBridge(new ScalablePathElementBridge());
	}

	
	
}