package org.kite9.diagram.batik.model;

import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.batik.transform.LeafTransformer;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
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
			return "";
		}
		
	};
	
	private RectangleRenderingInformation ri;
	private Layout layout;
	protected DiagramElementSizing sizing;	

	public AbstractRectangular(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
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
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.ELEMENT_SIZING_PROPERTY);
		this.sizing = (DiagramElementSizing) ev.getTheValue();
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
	public Map<String, String> getXPathVariables() {
		HashMap<String, String> out = new HashMap<>();
		out.put("x0", "0");
		out.put("y0", "0");
		double width = getRenderingInformation().getSize().getWidth();
		double height = getRenderingInformation().getSize().getHeight();
		out.put("x1", ""+ width);
		out.put("y1", ""+ height);
		out.put("width", ""+ width);
		out.put("height", ""+ height);
		
		return out;	
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
//		if (this instanceof SizedRectangular) {
//			Dimension2D min = ((SizedRectangular)this).getMinimumSize();
//			return new CostedDimension(
//					Math.max(c.getWidth(), min.getWidth()), 
//					Math.max(c.getHeight(), min.getHeight()), within);
//			
//		} else {
			return new CostedDimension(Math.max(c.getWidth(),1), Math.max(1, c.getHeight()), within);
//		}
	}
	
	private Dimension2D getLeafBounds() {
		Painter p = getPainter();
		if ((p instanceof LeafPainter) && (transformer instanceof LeafTransformer)) {
			return ((LeafTransformer)transformer).getBounds((LeafPainter)  p);
		}
		
		return CostedDimension.ZERO;
	}

}