package org.kite9.diagram.batik.model;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.Painter;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.kite9.diagram.model.style.IntegerRange;

public abstract class AbstractRectangularDiagramElement extends AbstractBatikDiagramElement implements Rectangular {
	
	public static final ContainerPosition NO_CONTAINER_POSITION = new ContainerPosition() {};
	
	private RectangleRenderingInformation ri;
	private Layout layout;
	private List<DiagramElement> contents = new ArrayList<>();
	protected DiagramElementSizing sizing;	

	public AbstractRectangularDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp) {
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

	public List<DiagramElement> getContents() {
		ensureInitialized();
		return contents;
	}

	@Override
	protected void initialize() {
		super.initialize();
		initElement(theElement);
		initContainerPosition();
	}

	protected void initElement(Kite9XMLElement theElement) {
		for (Kite9XMLElement xmlElement : theElement) {
			DiagramElement de = xmlElement.getDiagramElement();			
			if (de instanceof Connection) {
				addConnectionReference((Connection) de);
			} else if (de != null) { 
				contents.add(de);
			} else {
				initElement(xmlElement);
			}
		}
		
		initLayout();
		initSizing();
	}

	protected void addConnectionReference(Connection de) {
		((ConnectedContainerImpl) getDiagram()).addConnectionReference(de);
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
	protected ContentTransform getDefaultTransform() {
		return ContentTransform.POSITION;
	}

	
}