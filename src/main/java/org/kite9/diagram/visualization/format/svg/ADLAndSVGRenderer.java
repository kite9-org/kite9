package org.kite9.diagram.visualization.format.svg;

import java.io.IOException;

import org.apache.batik.svggen.DOMTreeManager;
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
 * Renders the ADL output with embedded SVG rendering information.
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
		ensureDisplayData(getDiagramDefs(), diagramRendering);
		g2.setTopLevelGroup(topGroup);
		return new XMLHelper().toXML(something);
	}

	private RenderingInformation diagramRendering;
	
	@Override
	protected SVGGraphicsLayer createGraphicsLayer(GraphicsLayerName name) {
		return new SVGGraphicsLayer(g2, name, document, topGroup) {

			@Override
			public void endElement(DiagramElement de) {
				Element thisGroup= getTopLevelGroup();
				if ((worthKeeping(thisGroup)) && (de instanceof PositionableDiagramElement)) {
					RenderingInformation ri = ((PositionableDiagramElement)de).getRenderingInformation();
					
					ensureDisplayData(thisGroup, ri);
				}
					
				// to make sure topGroup defs get added to the diagram
				if (de instanceof Diagram) {
					diagramRendering = ((PositionableDiagramElement)de).getRenderingInformation();
				}
				
				super.endElement(de);
			}
		};
	}
	
	private DOMTreeManager getDOMTreeManager() {
		return ((SVGGraphics2D)g2).getDOMTreeManager();
	}

	private Element getDiagramDefs() {
		Element e3 = getDOMTreeManager().getTopLevelGroup(true);
		return (Element) e3.getChildNodes().item(0);
	}

	private void ensureDisplayData(Element topGroup, RenderingInformation ri) {
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
	
	
}
