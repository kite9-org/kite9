package org.kite9.diagram.batik.layers;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Collections;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.PaintServer;
import org.apache.batik.bridge.SVGShapeElementBridge;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

/**
 * Shadow layer effectively has to create a shape of all the normal nodes and offset it in the output.
 * 
 * @author robmoffat
 *
 */
public class ShadowLayerCreator extends AbstractLayerCreator {

	@Override
	protected List<GraphicsNode> initSVGGraphicsContents(Element theElement, Kite9BridgeContext ctx, DiagramElement de) {
		Area compound = new Area();
		if (de.getShadow() != null) {
			
			List<GraphicsNode> nodes = super.initSVGGraphicsContents(theElement, ctx, de);
			
			for (GraphicsNode graphicsNode : nodes) {
				compound = reduceShape(graphicsNode, compound);
			}
			
		}
			
		if (compound.isEmpty()) {
			return Collections.emptyList(); 
		} else {
			ShapeNode sn = createGraphicsNodeForArea(theElement, ctx, compound);
			return Collections.singletonList(sn);
		}
		
	}

	private ShapeNode createGraphicsNodeForArea(Element theElement, Kite9BridgeContext ctx, Area compound) {
		SVGShapeElementBridge shapeBridge = new SVGShapeElementBridge() {
			
			@Override
			public String getLocalName() {
				return null;
			}
			
			@Override
			protected void buildShape(BridgeContext ctx, Element e, ShapeNode node) {
				node.setShape(compound);
			}

			@Override
			protected ShapePainter createShapePainter(BridgeContext ctx, Element e, ShapeNode shapeNode) {
				Shape shape = shapeNode.getShape();
				FillShapePainter fp = new FillShapePainter(shape);
		        Paint paint = convertFillPaint((StyledKite9SVGElement) e, shapeNode, ctx);
				fp.setPaint(paint);
		        return fp;
			}
			
			
		};
		ShapeNode sn = (ShapeNode) shapeBridge.createGraphicsNode(ctx, theElement);
		shapeBridge.buildGraphicsNode(ctx, theElement, sn);
		return sn;
	}

	@SuppressWarnings("unchecked")
	private Area reduceShape(GraphicsNode gn, Area compound) {
		if (gn instanceof ShapeNode) {
			compound.add(new Area(((ShapeNode) gn).getShape()));
			return compound;
		} else if (gn instanceof CompositeGraphicsNode) {
			CompositeGraphicsNode cgn = (CompositeGraphicsNode) gn;
			for (GraphicsNode n : (List<GraphicsNode>) cgn.getChildren()) {
				compound = reduceShape(n, compound);
			}
		}
		
		return compound;
	}
	
    public static Paint convertFillPaint(StyledKite9SVGElement shadowedElement,
                                         GraphicsNode filledNode,
                                         BridgeContext ctx) {
        Value v = shadowedElement.getCSSStyleProperty(CSSConstants.BOX_SHADOW_OPACITY_PROPERTY);
        float opacity = PaintServer.convertOpacity(v);
        v = shadowedElement.getCSSStyleProperty(CSSConstants.BOX_SHADOW_COLOR_PROPERTY);

        return PaintServer.convertPaint(shadowedElement,
                            filledNode,
                            v,
                            opacity,
                            ctx);
    }
	
}
