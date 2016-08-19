package org.kite9.diagram.visualization.format.svg;

import java.awt.Font;

import org.apache.batik.svggen.SVGGraphics2D;
import org.kite9.diagram.adl.IdentifiableDiagramElement;
import org.kite9.diagram.style.DiagramElement;
import org.kite9.diagram.visualization.format.BasicGraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGGraphicsLayer extends BasicGraphicsLayer {
	
	GraphicsLayerName name;
	Document document;
	Element originalTopGroup;
	boolean externalFonts;

	public SVGGraphicsLayer(SVGGraphics2D g2, GraphicsLayerName name, Document document, Element topGroup, boolean externalFonts) {
		super(g2);
		this.name = name;
		this.document = document;
		this.originalTopGroup = topGroup;
		this.externalFonts = externalFonts;
	}

	@Override
	public void startElement(DiagramElement de) {
		Element group = document.createElement("g");
		group.setAttribute("layer", name.name());
		if (de instanceof IdentifiableDiagramElement) {
			group.setAttribute("element-id", ((IdentifiableDiagramElement) de).getID());
		}
		
		((SVGGraphics2D)g2).setTopLevelGroup(group);
		super.startElement(de);
	}

	@Override
	public void endElement(DiagramElement de) {
		Element topGroup = getTopLevelGroup();
		if (worthKeeping(topGroup)) {
			originalTopGroup.appendChild(topGroup);
		}
		
		super.endElement(de);
		((SVGGraphics2D)g2).setTopLevelGroup(topGroup);
	}

	protected Element getTopLevelGroup() {
		return ((SVGGraphics2D)g2).getTopLevelGroup(false);
	}

	protected boolean worthKeeping(Element topGroup) {
		return topGroup.getChildNodes().getLength() > 0;
	}

	@Override
	public void outputText(Font font, double y, double x, String line) {
		if (!externalFonts) {
			super.outputText(font, y, x, line);
		} else {
			g2.setFont(font);
			g2.drawString(line, (float) x, (float) y); 
		}
	}
	
	
}
