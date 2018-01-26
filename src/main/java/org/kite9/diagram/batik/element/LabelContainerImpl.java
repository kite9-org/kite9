package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.RectangularPainter;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * Container and link-end labels. (TEMPORARY)
 * 
 * @author robmoffat
 * 
 */
public class LabelContainerImpl extends AbstractLabelImpl implements Label, Container {
	
	
	public LabelContainerImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Container> rp) {
		super(el, parent, ctx, rp);
	}
	
	public BorderTraversal getTraversalRule(Direction d) {
		return BorderTraversal.NONE;
	}
	
	@Override
	public int getGridColumns() {
		if (getLayout() == Layout.GRID) {
			return (int) getCSSStyleProperty(CSSConstants.GRID_COLUMNS_PROPERTY).getFloatValue();
		} else {
			return 0;
		}
	}

	@Override
	public int getGridRows() {
		if (getLayout() == Layout.GRID) {
			return (int) getCSSStyleProperty(CSSConstants.GRID_ROWS_PROPERTY).getFloatValue();
		} else {
			return 0;
		}
	}

	@Override
	public DiagramElementSizing getSizing() {
		ensureInitialized();
		return this.sizing;
	}

	@Override
	protected void initSizing() {
		super.initSizing();
		// only MAXIMIZE and MINIMIZE are allowed, MINIMIZE is the default.
		if (this.sizing != DiagramElementSizing.MAXIMIZE) {
			this.sizing = DiagramElementSizing.MINIMIZE;
		}
	} 
	
	@Override
	public CostedDimension getSize(Dimension2D within) {
		double left = getPadding(Direction.LEFT);
		double right = getPadding(Direction.RIGHT);
		double up = getPadding(Direction.UP);
		double down = getPadding(Direction.DOWN);
		return new CostedDimension(left + right, up + down, CostedDimension.UNBOUNDED);
	}
	
}