package org.kite9.diagram.batik.model;

import java.util.List;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.ContentTransform;

public class DecalContainerImpl extends AbstractRectangular implements Decal, Container {

	public DecalContainerImpl(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext context,
			Painter p, ContentTransform t) {
		super(el, parent, context, p, t);
	}
	
	private List<DiagramElement> contents;

	@Override
	public RectangleRenderingInformation getRenderingInformation() {
		return (RectangleRenderingInformation) parent.getRenderingInformation();
	}
	
	@Override
	public List<DiagramElement> getContents() {
		ensureInitialized();
		return contents;
	}

	@Override
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
	protected void initialize() {
		super.initialize();
		initLayout();
		initSizing();
		this.contents = initContents();
	}
}
