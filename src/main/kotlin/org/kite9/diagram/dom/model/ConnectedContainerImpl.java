package org.kite9.diagram.dom.model;

import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.ContentTransform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectedContainerImpl extends AbstractConnected implements Container {
	
	public static final Map<Direction, String> TRAVERSAL_PROPERTIES = new HashMap<Direction, String>();
	
	static {
		TRAVERSAL_PROPERTIES.put(Direction.UP, CSSConstants.TRAVERSAL_TOP_PROPERTY);
		TRAVERSAL_PROPERTIES.put(Direction.DOWN, CSSConstants.TRAVERSAL_BOTTOM_PROPERTY);
		TRAVERSAL_PROPERTIES.put(Direction.LEFT, CSSConstants.TRAVERSAL_LEFT_PROPERTY);
		TRAVERSAL_PROPERTIES.put(Direction.RIGHT, CSSConstants.TRAVERSAL_RIGHT_PROPERTY);
	};
	
	public ConnectedContainerImpl(StyledKite9XMLElement el, DiagramElement parent, ElementContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}
	
	private List<DiagramElement> contents;
	
	@Override
	protected void initialize() {
		super.initialize();
		initLayout();
		initSizing();
		this.contents = initContents();
	}

	public BorderTraversal getTraversalRule(Direction d) {
		BorderTraversal bt = (BorderTraversal) ctx.getCSSStyleProperty(TRAVERSAL_PROPERTIES.get(d), getTheElement());
		return bt;
	}

	@Override
	public int getGridColumns() {
		if (getLayout() == Layout.GRID) {
			return (int) ctx.getCssDoubleValue(CSSConstants.GRID_COLUMNS_PROPERTY, getTheElement());
		} else {
			return 0;
		}
	}

	@Override
	public int getGridRows() {
		if (getLayout() == Layout.GRID) {
			return (int) ctx.getCssDoubleValue(CSSConstants.GRID_ROWS_PROPERTY, getTheElement());
		} else {
			return 0;
		}
	}

	public List<DiagramElement> getContents() {
		ensureInitialized();
		return contents;
	}	
}
