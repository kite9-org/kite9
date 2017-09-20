package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class DecalImpl extends AbstractRectangularDiagramElement implements Decal {

	public DecalImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	protected void initializeChildXMLElement(Element child) {
		RectangleRenderingInformation rri = getContainer().getRenderingInformation();

		if (getRenderingInformation().getSize() == null) {
			// set size of this element same as the parent
			getRenderingInformation().setSize(rri.getSize());
			getRenderingInformation().setPosition(rri.getPosition());
		}
		
		processSizesUsingTemplater(child, rri);
	}
	
	@Override
	public DiagramElementSizing getSizing() {
		return DiagramElementSizing.SCALED;
	}

}
