package org.kite9.diagram.batik.element;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.templater.XMLProcessor;
import org.kite9.diagram.batik.templater.Templater;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractRectangularDiagramElement extends AbstractSVGDiagramElement implements Rectangular {
	
	public static final ContainerPosition NO_CONTAINER_POSITION = new ContainerPosition() {};
	
	private RectangleRenderingInformation ri;
	private Layout layout;
	private List<DiagramElement> contents = new ArrayList<>();
	private DiagramElementSizing sizing;	

	public AbstractRectangularDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
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

	private void initElement(Kite9XMLElement theElement) {
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
	
	public void initLayout() {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.LAYOUT_PROPERTY);
		if (ev != null) {
			layout = (Layout) ev.getTheValue();
		}
	} 
	
	public void initSizing() {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.ELEMENT_SIZING_PROPERTY);
		this.sizing = (DiagramElementSizing) ev.getTheValue();
	}

	@Override
	public DiagramElementSizing getSizing() {
		ensureInitialized();
		return sizing;
	}
	
	private void initContainerPosition() {
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
	
	private boolean initializedChildren = false;

	@Override
	protected void initializeChildXMLElements() {
		ensureInitialized();
		if (!initializedChildren) {
			
			if (getSizing() != DiagramElementSizing.FIXED) {
				processSizesUsingTemplater(theElement, getRenderingInformation());
			}
			
			initializedChildren = true;
		}
	}

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  Any style and class properties are copied across, and 
	 * the tag name is also added as a class, prefixed with an underbar.
	 * 
	 * Finally a translate transform is applid so it appears in the right place.
	 */
	@Override
	public Element output(Document d, XMLProcessor t) {
		initializeChildXMLElements();
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		t.process(theElement, out);
		out.setAttribute(SVG12OMDocument.SVG_ID_ATTRIBUTE, getID());
		out.setAttribute("class", theElement.getCSSClass()+" "+theElement.getTagName());
		out.setAttribute("style", theElement.getAttribute("style"));
		
		// work out translation
		RectangleRenderingInformation rri = getRenderingInformation();
		Dimension2D position = rri.getPosition();
		if (getParent() != null) {
			rri = ((Container) getParent()).getRenderingInformation();
			Dimension2D parentPosition = rri.getPosition();
			position = new Dimension2D(position.x() - parentPosition.x(), position.y() - parentPosition.y());
		}
		
		out.setAttribute("transform", "translate("+position.x()+","+position.y()+")");
		return out;
	}
	
	
	
}