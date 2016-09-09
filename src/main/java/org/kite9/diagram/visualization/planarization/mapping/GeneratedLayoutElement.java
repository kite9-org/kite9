package org.kite9.diagram.visualization.planarization.mapping;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.HintMap;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.common.elements.AbstractBiDirectional;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformationImpl;

/**
 * This connection is used with a {@link ContainerLayoutEdge} and is used to create a layout between
 * two elements of a container.  
 */
public class GeneratedLayoutElement extends AbstractBiDirectional<Connected> implements DiagramElement {
	
	public GeneratedLayoutElement(Connected from, Connected to, Direction drawDirection) {
		super(from, to, drawDirection);
	}

	@Override
	public String toString() {
		return "cle-"+getID();
	}

	@Override
	public int compareTo(DiagramElement o) {
		if (o instanceof AbstractBiDirectional<?>) {
			return this.getID().compareTo(((AbstractBiDirectional<?>) o).getID());
		} else {
			return -1;
		}
	}
	
	private RouteRenderingInformation rri;

	@Override
	public RenderingInformation getRenderingInformation() {
		if (rri == null) {
			rri = new RouteRenderingInformationImpl();
		}
		
		return rri;
	}

	@Override
	public Value getCSSStyleProperty(String prop) {
		return null;
	}

	@Override
	public void setRenderingInformation(RenderingInformation ri) {
		this.rri = (RouteRenderingInformation) ri;
	}

	@Override
	public String getShapeName() {
		return null;
	}

	@Override
	public DiagramElement getParent() {
		return null;
	}
	
	public Container getContainer() {
		return null;
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	@Override
	public void setPositioningHints(HintMap hints) {
	}
}