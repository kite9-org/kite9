package org.kite9.diagram.batik.model;

import java.util.List;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.ContentTransform;

/**
 * Container and link-end labels. (TEMPORARY)
 * 
 * @author robmoffat
 * 
 */
public class LabelContainerImpl extends AbstractLabel implements Label, Container {
	
	public LabelContainerImpl(StyledKite9XMLElement el, DiagramElement parent, ElementContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}
	
	public BorderTraversal getTraversalRule(Direction d) {
		return BorderTraversal.NONE;
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

	private List<DiagramElement> contents;
	
	@Override
	protected void initialize() {
		super.initialize();
		this.contents = initContents();
	}

	@Override
	public List<DiagramElement> getContents() {
		ensureInitialized();
		return contents;
	}
	
}