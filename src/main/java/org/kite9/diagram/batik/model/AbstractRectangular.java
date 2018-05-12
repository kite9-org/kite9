package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
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

public abstract class AbstractRectangular extends AbstractBatikDiagramElement implements Rectangular {
	
	public static final ContainerPosition NO_CONTAINER_POSITION = new ContainerPosition() {};
	
	private RectangleRenderingInformation ri;
	private Layout layout;
	protected DiagramElementSizing sizing;	

	public AbstractRectangular(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp) {
		super(el, parent, ctx, rp);
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
		initializeDirectionalCssValues(padding, CSSConstants.KITE9_CSS_PADDING_PROPERTY_PREFIX);
		initializeDirectionalCssValues(margin, CSSConstants.KITE9_CSS_MARGIN_PROPERTY_PREFIX);
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

	protected double margin[] = new double[4];

	protected double padding[] = new double[4];
	

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
	protected ContentTransform getDefaultTransform() {
		return ContentTransform.POSITION;
	}

	public double getMargin(Direction d) {
		ensureInitialized();
		return margin[d.ordinal()];
	}

	public double getPadding(Direction d) {
		ensureInitialized();
		return padding[d.ordinal()];
	}

	protected void initializeDirectionalCssValues(double[] vals, String prefix) {
		vals[Direction.UP.ordinal()] = getCssDoubleValue(prefix+CSSConstants.TOP);
		vals[Direction.DOWN.ordinal()] = getCssDoubleValue(prefix+CSSConstants.BOTTOM);
		vals[Direction.LEFT.ordinal()] = getCssDoubleValue(prefix+CSSConstants.LEFT);
		vals[Direction.RIGHT.ordinal()] = getCssDoubleValue(prefix+CSSConstants.RIGHT);	
	}

	
}