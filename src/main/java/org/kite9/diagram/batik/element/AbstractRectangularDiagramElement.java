package org.kite9.diagram.batik.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.kite9.diagram.model.style.IntegerRange;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public abstract class AbstractRectangularDiagramElement extends AbstractBatikDiagramElement implements Rectangular {
	
	public static final ContainerPosition NO_CONTAINER_POSITION = new ContainerPosition() {};
	
	private RectangleRenderingInformation ri;
	private Layout layout;
	private List<DiagramElement> contents = new ArrayList<>();
	protected DiagramElementSizing sizing;	

	public AbstractRectangularDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<?> rp) {
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
	protected void postProcess(Element out) {
		// work out translation
		Dimension2D position = getRectangularRenderedPosition();
		
		if ((position.x() != 0) || (position.y() != 0)) {
			out.setAttribute("transform", "translate(" + position.x() + "," + position.y() + ")");
		}
	}	

	@Override
	protected Map<String, String> getReplacementMap(StyledKite9SVGElement theElement) {
		Map<String, String> out = super.getReplacementMap(theElement);
		Dimension2D size = getRectangularRenderedSize();
		double width = size.getWidth();
		double height = size.getHeight();
		out.put("x0", "0");
		out.put("y0", "0");
		out.put("x1", ""+width);
		out.put("y1", ""+height);	
		return out;
	}

	protected Dimension2D getRectangularRenderedSize() {
		RectangleRenderingInformation rri = getRenderingInformation();
		Dimension2D size = rri.getSize();
		return size;
	}
	
	protected Dimension2D getRectangularRenderedPosition() {
		RectangleRenderingInformation rri = getRenderingInformation();
		Dimension2D position = rri.getPosition();
		if (getParent() instanceof Container) {
			rri = ((Container) getParent()).getRenderingInformation();
			Dimension2D parentPosition = rri.getPosition();
			position = new Dimension2D(position.x() - parentPosition.x(), position.y() - parentPosition.y());
		}
		return position;
	}

}