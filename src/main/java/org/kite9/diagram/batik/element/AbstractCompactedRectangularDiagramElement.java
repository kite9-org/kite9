package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.CompactedRectangular;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;

public abstract class AbstractCompactedRectangularDiagramElement extends AbstractRectangularDiagramElement implements CompactedRectangular {

	public AbstractCompactedRectangularDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	private VerticalAlignment verticalAlignment;
	private HorizontalAlignment horizontalAlignment;

	@Override
	public double getMargin(Direction d) {
		ensureInitialized();
		return margin[d.ordinal()];
	}

	public double getPadding(Direction d) {
		ensureInitialized();
		return padding[d.ordinal()];
	}
	
	

	@Override
	public VerticalAlignment getVerticalAlignment() {
		ensureInitialized();
		return verticalAlignment;
	}

	@Override
	public HorizontalAlignment getHorizontalAlignment() {
		ensureInitialized();
		return horizontalAlignment;
	}

	@Override
	protected void initialize() {
		super.initialize();
		initAlignment();
	}

	private void initAlignment() {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.HORIZONTAL_ALIGNMENT);
		horizontalAlignment = (HorizontalAlignment) ev.getTheValue();
		ev = (EnumValue) getCSSStyleProperty(CSSConstants.VERTICAL_ALIGNMENT);
		verticalAlignment = (VerticalAlignment) ev.getTheValue();
	}
	
}