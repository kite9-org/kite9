package org.kite9.diagram.common.elements.mapping;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.common.elements.AbstractBiDirectional;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformationImpl;

/**
 * This connection is used with a {@link ContainerLayoutEdge} and is used to create a layout between
 * two elements of a container.  
 * 
 * Also, with {@link BorderEdge}, when two containers border each other.
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
	public double getMargin(Direction d) {
		return 0;
	}

	@Override
	public int getDepth() {
		return 1;
	}
}