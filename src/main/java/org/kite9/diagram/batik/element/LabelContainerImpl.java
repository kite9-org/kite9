package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * Container and link-end labels. (TEMPORARY)
 * 
 * @author robmoffat
 * 
 */
public class LabelContainerImpl extends AbstractLabelImpl implements Label, Container {
	
	
	public LabelContainerImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}
	
	public BorderTraversal getTraversalRule(Direction d) {
		EnumValue v = (EnumValue) getCSSStyleProperty(ConnectedContainerImpl.TRAVERSAL_PROPERTIES.get(d));
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
	public boolean hasContent() {
		return true;
	}
	
	
}