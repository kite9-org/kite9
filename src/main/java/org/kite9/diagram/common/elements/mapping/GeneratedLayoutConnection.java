package org.kite9.diagram.common.elements.mapping;

import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.common.elements.AbstractBiDirectional;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Temporary;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformationImpl;
import org.kite9.diagram.model.style.BoxShadow;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;

/**
 * This connection is used with a {@link ContainerLayoutEdge} and is used to create a layout between
 * two elements of a container.  
 * 
 * Also, with {@link BorderEdge}, when two containers border each other.
 */
public class GeneratedLayoutConnection extends AbstractBiDirectional<Connected> implements Connection, Temporary {
	
	public GeneratedLayoutConnection(Connected from, Connected to, Direction drawDirection) {
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
	public RouteRenderingInformation getRenderingInformation() {
		if (rri == null) {
			rri = new RouteRenderingInformationImpl();
		}
		
		return rri;
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
	public int getDepth() {
		return 1;
	}

	@Override
	public Terminator getFromDecoration() {
		return null;
	}

	@Override
	public Terminator getToDecoration() {
		return null;
	}

	@Override
	public Label getFromLabel() {
		return null;
	}

	@Override
	public Label getToLabel() {
		return null;
	}

	@Override
	public int getRank() {
		return 0;
	}

	@Override
	public double getMargin(Direction d) {
		return 0;
	}

	@Override
	public double getPadding(Direction d) {
		return 0;
	}

	@Override
	public BoxShadow getShadow() {
		return null;
	}
}