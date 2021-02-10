package org.kite9.diagram.dom.model;

import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.BasicDimension2D;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;

public abstract class AbstractCompactedRectangular extends AbstractRectangular implements SizedRectangular, AlignedRectangular {

	public AbstractCompactedRectangular(StyledKite9XMLElement el, DiagramElement parent, ElementContext ctx, Painter rp, ContentTransform t) {
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
		horizontalAlignment = (HorizontalAlignment) ctx.getCSSStyleProperty(CSSConstants.HORIZONTAL_ALIGNMENT, getTheElement());
		verticalAlignment = (VerticalAlignment) ctx.getCSSStyleProperty(CSSConstants.VERTICAL_ALIGNMENT, getTheElement());
	}
	
	private void initMinimumSize() {
		double w = getCssDoubleValue(CSSConstants.RECT_MINIMUM_WIDTH);
		double h = getCssDoubleValue(CSSConstants.RECT_MINIMUM_HEIGHT);
		this.minimumSize = new BasicDimension2D(w,h);
	}
	
	@Override
	public Dimension2D getMinimumSize() {
		ensureInitialized();
		return this.minimumSize;
	}

	@Override
	public DiagramElementSizing getSizing(boolean horiz) {
		ensureInitialized();
		return horiz ? this.getSizingHoriz() : this.getSizingVert();
	}

}
