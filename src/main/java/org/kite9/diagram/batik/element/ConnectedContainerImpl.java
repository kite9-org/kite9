package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class ConnectedContainerImpl extends AbstractConnectedDiagramElement implements Container {
	
	Label label;
	
	public ConnectedContainerImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	public Label getLabel() {
		ensureInitialized();
		return label;
	}

	@Override
	protected void addLabelReference(Label de) {
		this.label = de;
	}
	
	protected void initialize() {
		super.initialize();
		traversal[Direction.UP.ordinal()] = getTraversalRule(CSSConstants.TRAVERSAL_TOP_PROPERTY);
		traversal[Direction.DOWN.ordinal()] = getTraversalRule(CSSConstants.TRAVERSAL_BOTTOM_PROPERTY);
		traversal[Direction.LEFT.ordinal()] = getTraversalRule(CSSConstants.TRAVERSAL_LEFT_PROPERTY);
		traversal[Direction.RIGHT.ordinal()] = getTraversalRule(CSSConstants.TRAVERSAL_RIGHT_PROPERTY);	
	}
	
	private BorderTraversal[] traversal = new BorderTraversal[4];

	public BorderTraversal getTraversalRule(Direction d) {
		ensureInitialized();
		return traversal[d.ordinal()];
	}
	
	private BorderTraversal getTraversalRule(String p) {
		EnumValue v = (EnumValue) getCSSStyleProperty(p);
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
	
}
