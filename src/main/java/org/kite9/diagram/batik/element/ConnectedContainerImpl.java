package org.kite9.diagram.batik.element;

import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class ConnectedContainerImpl extends AbstractConnectedDiagramElement implements Container {
	
	public static final Map<Direction, String> TRAVERSAL_PROPERTIES = new HashMap<Direction, String>();
	
	static {
		TRAVERSAL_PROPERTIES.put(Direction.UP, CSSConstants.TRAVERSAL_TOP_PROPERTY);
		TRAVERSAL_PROPERTIES.put(Direction.DOWN, CSSConstants.TRAVERSAL_BOTTOM_PROPERTY);
		TRAVERSAL_PROPERTIES.put(Direction.LEFT, CSSConstants.TRAVERSAL_LEFT_PROPERTY);
		TRAVERSAL_PROPERTIES.put(Direction.RIGHT, CSSConstants.TRAVERSAL_RIGHT_PROPERTY);
	};
	
	public ConnectedContainerImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<?> rp) {
		super(el, parent, ctx, rp);
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

	public BorderTraversal getTraversalRule(Direction d) {
		EnumValue v = (EnumValue) getCSSStyleProperty(TRAVERSAL_PROPERTIES.get(d));
		BorderTraversal bt = (BorderTraversal) v.getTheValue();
		return bt;
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
	public CostedDimension getSize(Dimension2D within) {
		return getSizeBasedOnPadding();
	}	
}
