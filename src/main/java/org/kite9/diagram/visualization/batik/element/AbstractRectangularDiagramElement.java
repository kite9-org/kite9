package org.kite9.diagram.visualization.batik.element;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Rectangular;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.style.DiagramElementSizing;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.serialization.CSSConstants;
import org.kite9.framework.serialization.EnumValue;

public abstract class AbstractRectangularDiagramElement extends AbstractSVGDiagramElement implements Rectangular {
	
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
	}

	private void initElement(XMLElement theElement) {
		for (XMLElement xmlElement : theElement) {
			DiagramElement de = xmlElement.getDiagramElement();			
			if (de instanceof Label) {
				addLabelReference((Label) de);
			} else if (de instanceof Connection) {
				addConnectionReference((Connection) de);
			} else if (de != null) { 
				contents.add(de);
			} else {
				initElement(xmlElement);
			}
		}
		
		initLayout(theElement);
		initSizing(theElement);
	}

	protected void addLabelReference(Label de) {
	}
	

	protected void addConnectionReference(Connection de) {
		((ConnectedContainerImpl) getDiagram()).addConnectionReference(de);
	}

	public Layout getLayout() {
		return layout;
	}
	
	public void initLayout(XMLElement theElement) {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.LAYOUT_PROPERTY);
		if (ev != null) {
			layout = (Layout) ev.getTheValue();
		}
	}
	
	public void initSizing(XMLElement theElement) {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.ELEMENT_SIZING_PROPERTY);
		this.sizing = (DiagramElementSizing) ev.getTheValue();
	}

	@Override
	public DiagramElementSizing getSizing() {
		return sizing;
	}
	
	@Override
	public double getMargin(Direction d) {
		return margin[d.ordinal()];
	}

	@Override
	public double getPadding(Direction d) {
		return padding[d.ordinal()];
	}


}