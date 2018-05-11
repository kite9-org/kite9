package org.kite9.diagram.batik.elements;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.Painter;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.elements.StyledKite9SVGElement;
import org.kite9.framework.dom.managers.EnumValue;

public abstract class AbstractCompactedRectangularDiagramElement extends AbstractRectangularDiagramElement implements SizedRectangular, AlignedRectangular {

	public AbstractCompactedRectangularDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp) {
		super(el, parent, ctx, rp);
	}

	private VerticalAlignment verticalAlignment;
	private HorizontalAlignment horizontalAlignment;
	private Dimension2D minimumSize;

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
		initMinimumSize();
	}

	private void initAlignment() {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.HORIZONTAL_ALIGNMENT);
		horizontalAlignment = (HorizontalAlignment) ev.getTheValue();
		ev = (EnumValue) getCSSStyleProperty(CSSConstants.VERTICAL_ALIGNMENT);
		verticalAlignment = (VerticalAlignment) ev.getTheValue();
	}
	
	private void initMinimumSize() {
		double w = getCssDoubleValue(this.theElement, CSSConstants.RECT_MINIMUM_WIDTH);
		double h = getCssDoubleValue(this.theElement, CSSConstants.RECT_MINIMUM_HEIGHT);
		this.minimumSize = new Dimension2D(w,h);
	}

	
	@Override
	public Dimension2D getMinimumSize() {
		return this.minimumSize;
	}

	
}
