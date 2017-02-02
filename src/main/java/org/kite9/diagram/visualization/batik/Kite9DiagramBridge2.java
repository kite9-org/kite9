package org.kite9.diagram.visualization.batik;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.SVGRectElementBridge;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.gvt.ShapeNode;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Rectangular;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.display.complete.ADLBasicCompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.GriddedCompleteDisplayer;
import org.kite9.diagram.visualization.format.png.BufferedImageRenderer;
import org.kite9.diagram.visualization.pipeline.full.ArrangementPipeline;
import org.kite9.diagram.visualization.pipeline.full.BufferedImageProcessingPipeline;
import org.kite9.diagram.visualization.pipeline.full.ProcessingPipeline;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class Kite9DiagramBridge2 extends SVGRectElementBridge {

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
		return new Kite9DiagramBridge2();
	}
	
	private ArrangementPipeline createPipeline() {
		return new BatikArrangementPipeline();
	}

    /**
     * Constructs a rectangle according to the specified parameters.
     *
     * @param ctx the bridge context to use
     * @param e the element that describes a rect element
     * @param shapeNode the shape node to initialize
     */
    protected void buildShape(BridgeContext ctx,
                              Element e,
                              ShapeNode shapeNode) { 

        try {
        	if (e instanceof DiagramXMLElement) {
        		createPipeline().arrange((DiagramXMLElement)e);
        	}
        	
            StyledKite9SVGElement re = (StyledKite9SVGElement) e;
            DiagramElement de = re.getDiagramElement();
            
            if (de instanceof Rectangular) {
            	RectangleRenderingInformation rri = ((Rectangular)de).getRenderingInformation();
            	
            float x = (float) rri.getPosition().x();
            float y = (float) rri.getPosition().y();
            float w = (float) rri.getSize().getWidth();
            float h = (float) rri.getSize().getHeight();
            float rx = 4;
            float ry = 4;

            Shape shape;
            if (rx == 0 || ry == 0) {
                shape = new Rectangle2D.Float(x, y, w, h);
            } else {
                shape = new RoundRectangle2D.Float(x, y, w, h, rx * 2, ry * 2);
            }
            shapeNode.setShape(shape);
            
            }
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }

}
