package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.element.Templater.ValueReplacer;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class DecalImpl extends AbstractRectangularDiagramElement implements Decal {

	public DecalImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	public void setParentSize(final double[] x, final double[] y) {
		RectangleRenderingInformation rri = getContainer().getRenderingInformation();
		getRenderingInformation().setSize(rri.getSize());
		getRenderingInformation().setPosition(rri.getPosition());
		ctx.getTemplater().performReplace(theElement, new ValueReplacer() {
			
			@Override
			public String getText() {
				return null;
			}
			
			@Override
			public String getReplacementValue(String prefix, String attr) {
				if ("x".equals(prefix) || "y".equals(prefix)) {
					int index = Integer.parseInt(attr);
					double v = "x".equals(prefix) ? x[index] : y[index];
					return ""+v;
				} else {
					return prefix+attr;
				}
			}
		});
	}

	@Override
	public DiagramElementSizing getSizing() {
		return DiagramElementSizing.FIXED;
	}

}
