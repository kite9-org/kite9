package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.batik.transform.LeafTransformer;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.kite9.diagram.model.style.IntegerRange;
import org.kite9.framework.logging.LogicException;

public abstract class AbstractRectangular extends AbstractBatikDiagramElement implements Rectangular, XPathAware {
	
	public static final ContainerPosition NO_CONTAINER_POSITION = new ContainerPosition() {
		
		public String toString() {
			return "none";
		}
		
	};
	
	private RectangleRenderingInformation ri;
	private Layout layout;
	protected DiagramElementSizing sizingHoriz;	
	protected DiagramElementSizing sizingVert;	
	

	public AbstractRectangular(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
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
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.LAYOUT_PROPERTY);
		if (ev != null) {
			layout = (Layout) ev.getTheValue();
		}
	} 
	
	protected void initSizing() {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY);
		this.sizingHoriz = (DiagramElementSizing) ev.getTheValue();
		ev = (EnumValue) getCSSStyleProperty(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY);
		this.sizingVert = (DiagramElementSizing) ev.getTheValue();
	}
	
	protected void initContainerPosition() {
		if (containerPosition == null) {
			if (getParent() instanceof Container) {
				if (getContainer().getLayout() == Layout.GRID) {
					IntegerRange x = (IntegerRange) getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY);
					IntegerRange y = (IntegerRange) getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY);
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
			return ""+getRenderingInformation().getSize().getHeight();
		} else if ("x1".equals(name) || "width".equals(name)) {
			return ""+getRenderingInformation().getSize().getWidth();
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

	
	
	public final CostedDimension getSize(Dimension2D within) {
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
			return ensureMinimumSize(new Dimension2D(left + right + bounds.getWidth(), up + down + bounds.getHeight()), within);
		}
	
		throw new LogicException("Not sure how to size: "+this);
	}
	
	private CostedDimension ensureMinimumSize(Dimension2D c, Dimension2D within) {
		Dimension2D min = CostedDimension.ZERO;
		if (this instanceof SizedRectangular) {
			min = ((SizedRectangular)this).getMinimumSize();
		}
		
		return new CostedDimension(
				Math.max(c.getWidth(), min.getWidth()), 
				Math.max(c.getHeight(), min.getHeight()), within);
	}
	
	private Dimension2D getLeafBounds() {
		Painter p = getPainter();
		if ((p instanceof LeafPainter) && (transformer instanceof LeafTransformer)) {
			return ((LeafTransformer)transformer).getBounds((LeafPainter)  p);
		}
		
		return CostedDimension.ZERO;
	}

}