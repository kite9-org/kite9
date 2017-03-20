package org.kite9.diagram.batik.element;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.kite9.framework.xml.Kite9XMLElement;

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

	private void initElement(Kite9XMLElement theElement) {
		for (Kite9XMLElement xmlElement : theElement) {
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
		ensureInitialized();
		return layout;
	}
	
	public void initLayout(Kite9XMLElement theElement) {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.LAYOUT_PROPERTY);
		if (ev != null) {
			layout = (Layout) ev.getTheValue();
		}
	}
	
	public void initSizing(Kite9XMLElement theElement) {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.ELEMENT_SIZING_PROPERTY);
		this.sizing = (DiagramElementSizing) ev.getTheValue();
	}

	@Override
	public DiagramElementSizing getSizing() {
		ensureInitialized();
		return sizing;
	}
	
	@Override
	public double getMargin(Direction d) {
		ensureInitialized();
		return margin[d.ordinal()];
	}

	@Override
	public double getPadding(Direction d) {
		ensureInitialized();
		return padding[d.ordinal()];
	}


}