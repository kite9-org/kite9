package org.kite9.diagram.visualization.format.svg;

import java.io.IOException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.XMLFragments;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Element;

/**
 * Renders the ADL output with embedded SVG rendering information
 * 
 * @author robmoffat
 *
 */
public class ADLAndSVGRenderer extends SVGRenderer {

	public ADLAndSVGRenderer() {
		super();
	}

	public ADLAndSVGRenderer(Integer width, Integer height) {
		super(width, height);
	}

	@Override
	protected String output(Diagram something) throws SVGGraphics2DIOException, IOException {
		return new XMLHelper().toXML(something);
	}

	@Override
	protected SVGGraphicsLayer createGraphicsLayer(GraphicsLayerName name) {
		return new SVGGraphicsLayer(g2, name, document, topGroup) {

			@Override
			public void endElement(DiagramElement de) {
				Element topGroup = ((SVGGraphics2D)g2).getTopLevelGroup();
				if ((worthKeeping(topGroup)) && (de instanceof PositionableDiagramElement)) {
					RenderingInformation ri = ((PositionableDiagramElement)de).getRenderingInformation();
					
					Object displayData = ri.getDisplayData();
					
					if (displayData == null) {
						displayData = new XMLFragments();
						ri.setDisplayData(displayData);
					}
					
					if (displayData instanceof XMLFragments) {
						((XMLFragments) displayData).getParts().add(topGroup);
						
					} else {
						throw new Kite9ProcessingException("Mixed rendering: "+displayData.getClass());
					}
					
				}
				
				super.endElement(de);
			}
			
			
		};
	}

	
	
}
