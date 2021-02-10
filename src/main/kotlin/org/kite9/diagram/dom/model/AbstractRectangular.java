package org.kite9.diagram.dom.model;

import org.kite9.diagram.common.range.IntegerRange;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.LeafPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.dom.transform.LeafTransformer;
import org.kite9.diagram.logging.LogicException;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.*;
import org.kite9.diagram.model.position.*;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.GridContainerPosition;

public abstract class AbstractRectangular extends AbstractModelDiagramElement implements Rectangular, XPathAware {
	
	public static final ContainerPosition NO_CONTAINER_POSITION = new ContainerPosition() {
		
		public String toString() {
			return "none";
		}
		
	};
	
	private RectangleRenderingInformation ri;
	private Layout layout;
	protected DiagramElementSizing sizingHoriz;	
	protected DiagramElementSizing sizingVert;

	public AbstractRectangular(StyledKite9XMLElement el, DiagramElement parent, ElementContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}

	@Override
	public RectangleRenderingInformation getRenderingInformation() {
		if (ri == null) {
			ri = new RectangleRenderingInformationImpl();
		}
		
		return ri;
	}

	public void setRenderingInformation(RenderingInformation ri) {
		this.ri = (RectangleRenderingInformation) ri;
	}

	@Override
	protected void initialize() {
		super.initialize();
		initContainerPosition();
		initSizing();
		initLayout();
	}

	public Layout getLayout() {
		ensureInitialized();
		return layout;
	}
	
	protected void initLayout() {
		layout = (Layout) ctx.getCSSStyleProperty(CSSConstants.LAYOUT_PROPERTY, getTheElement());
	} 
	
	protected void initSizing() {
		this.sizingHoriz = (DiagramElementSizing) ctx.getCSSStyleProperty(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY, getTheElement());
		this.sizingVert = (DiagramElementSizing) ctx.getCSSStyleProperty(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY, getTheElement());
	}
	
	protected void initContainerPosition() {
		if (containerPosition == null) {
			if (getParent() instanceof Container) {
				if (getContainer().getLayout() == Layout.GRID) {
					IntegerRange x = ctx.getCSSStyleRangeProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY, getTheElement());
					IntegerRange y = ctx.getCSSStyleRangeProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, getTheElement());
					containerPosition = new GridContainerPosition(x, y);
				}
			}
			
			
			if (containerPosition == null) {
				containerPosition = NO_CONTAINER_POSITION;
			}
		}
	}

	private ContainerPosition containerPosition = null;

	@Override
	public ContainerPosition getContainerPosition() {
		ensureInitialized();
		return containerPosition;
	}
	
	@Override
	public Container getContainer() {
		return (Container) getParent();
	}
	
	@Override
	public String getXPathVariable(String name) {
		if (("x0".equals(name) )|| ("y0".equals(name))) {
			return "0";
		} else if ("y1".equals(name) || "height".equals(name)) {
			return ""+getRenderingInformation().getSize().getH();
		} else if ("x1".equals(name) || "width".equals(name)) {
			return ""+getRenderingInformation().getSize().getW();
		} else if ((getLayout() == Layout.GRID) && (this instanceof Container)) {
			boolean cellX = name.startsWith("cell-x-");
			boolean cellY = name.startsWith("cell-y-");
			
			if (cellX) {
				int idx = safeParseInt(name);
				return idx > -1 ? ""+ getRenderingInformation().getCellXPositions()[idx] : null;
			} else if (cellY) {
				int idx = safeParseInt(name);
				return idx > -1 ? ""+ getRenderingInformation().getCellYPositions()[idx]: null;
			}
		}
		
		return null;
	}

	protected int safeParseInt(String name) {
		try {
			return Integer.parseInt(name.substring(7));
		} catch (Exception e) {
			return -1;
		}
	}

	
	
	public final CostedDimension2D getSize(Dimension2D within) {
		if (this instanceof Decal) {
			throw new LogicException("Shouldn't be using size for decals");
		}else if (this instanceof Terminator) {
			throw new LogicException("Shouldn't be using size for terminators");
		} else if (this instanceof Container) {
			return ensureMinimumSize(getSizeBasedOnPadding(), within);
		} else if (this instanceof Leaf) {
			double left = getPadding(Direction.LEFT);
			double right = getPadding(Direction.RIGHT);
			double up = getPadding(Direction.UP);
			double down = getPadding(Direction.DOWN);
			Dimension2D bounds = getLeafBounds();
			return ensureMinimumSize(new BasicDimension2D(left + right + bounds.getW(), up + down + bounds.getH()), within);
		}
	
		throw new LogicException("Not sure how to size: "+this);
	}
	
	private CostedDimension2D ensureMinimumSize(Dimension2D c, Dimension2D within) {
		Dimension2D min = CostedDimension2D.Companion.getZERO();
		if (this instanceof SizedRectangular) {
			min = ((SizedRectangular)this).getMinimumSize();
		}
		
		return new CostedDimension2D(
				Math.max(c.getW(), min.getW()),
				Math.max(c.getH(), min.getH()), within);
	}
	
	private Dimension2D getLeafBounds() {
		Painter p = getPainter();
		if ((p instanceof LeafPainter) && (transformer instanceof LeafTransformer)) {
			return ((LeafTransformer)transformer).getBounds((LeafPainter)  p);
		}
		
		return CostedDimension2D.Companion.getZERO();
	}

}