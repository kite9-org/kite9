package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;

public abstract class AbstractCompactedRectangular extends AbstractRectangular implements SizedRectangular, AlignedRectangular {

	public AbstractCompactedRectangular(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
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
		double w = getCssDoubleValue(CSSConstants.RECT_MINIMUM_WIDTH);
		double h = getCssDoubleValue(CSSConstants.RECT_MINIMUM_HEIGHT);
		this.minimumSize = new Dimension2D(w,h);
	}
	
	@Override
	public Dimension2D getMinimumSize() {
		return this.minimumSize;
	}

	
}
