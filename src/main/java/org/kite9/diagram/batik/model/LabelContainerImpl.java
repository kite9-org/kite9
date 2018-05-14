package org.kite9.diagram.batik.model;

import java.util.List;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementSizing;

/**
 * Container and link-end labels. (TEMPORARY)
 * 
 * @author robmoffat
 * 
 */
public class LabelContainerImpl extends AbstractLabel implements Label, Container {
	
	public LabelContainerImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
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