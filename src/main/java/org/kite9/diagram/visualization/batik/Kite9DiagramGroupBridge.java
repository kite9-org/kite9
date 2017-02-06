package org.kite9.diagram.visualization.batik;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGGElementBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.XMLElement;
import org.w3c.dom.Element;

public class Kite9DiagramGroupBridge extends SVGGElementBridge {
	
	private final GraphicsNodeLookup lookup;

	public Kite9DiagramGroupBridge(GraphicsNodeLookup lookup) {
		super();
		this.lookup = lookup;
	}

	@Override
	public String getNamespaceURI() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public Bridge getInstance() {
		return new Kite9DiagramGroupBridge(lookup);
	}

	@Override
	protected void associateSVGContext(BridgeContext ctx, Element e, GraphicsNode node) {
		super.associateSVGContext(ctx, e, node);
		lookup.storeNode(getLayer(), (XMLElement) e, node);
	}

	private GraphicsLayerName getLayer() {
		return GraphicsLayerName.MAIN;
	}
	
	

//    /**
//     * Constructs a rectangle according to the specified parameters.
//     *
//     * @param ctx the bridge context to use
//     * @param e the element that describes a rect element
//     * @param shapeNode the shape node to initialize
//     */
//    protected void buildShape(BridgeContext ctx,
//                              Element e,
//                              ShapeNode shapeNode) { 
//
//        try {
//        	if (e instanceof DiagramXMLElement) {
//        		createPipeline().arrange((DiagramXMLElement)e);
//        	}
//        	
//            StyledKite9SVGElement re = (StyledKite9SVGElement) e;
//            DiagramElement de = re.getDiagramElement();
//            
//            if (de instanceof Rectangular) {
//            	RectangleRenderingInformation rri = ((Rectangular)de).getRenderingInformation();
//            	
//            float x = (float) rri.getPosition().x();
//            float y = (float) rri.getPosition().y();
//            float w = (float) rri.getSize().getWidth();
//            float h = (float) rri.getSize().getHeight();
//            float rx = 4;
//            float ry = 4;
//
//            Shape shape;
//            if (rx == 0 || ry == 0) {
//                shape = new Rectangle2D.Float(x, y, w, h);
//            } else {
//                shape = new RoundRectangle2D.Float(x, y, w, h, rx * 2, ry * 2);
//            }
//            shapeNode.setShape(shape);
//            
//            }
//        } catch (LiveAttributeException ex) {
//            throw new BridgeException(ctx, ex);
//        }
//    }

}
