package org.kite9.diagram.visualization.planarization.rhd.links;

import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.common.elements.grid.AbstractTemporaryConnected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.ConnectionAlignment;
import org.kite9.diagram.model.style.ConnectionsSeparation;
import org.kite9.diagram.model.style.ContainerPosition;

/**
 * This represents a particular coordinate on a grid (e.g. 3, 4).  
 * If cells span, they can contain several of these.
 * 
 * @author robmoffat
 *
 */
public class GridCoordinateConnected extends AbstractTemporaryConnected {

	Container c;
	int x;
	int y;
	
	public GridCoordinateConnected(Container c, int x, int y) {
		super(c);
		this.c = c;
		this.x = x;
		this.y = y;
		this.id = c.getID()+"-"+x+","+y;
	}

	@Override
	public ConnectionsSeparation getConnectionsSeparationApproach() {
		return null;
	}

	@Override
	public double getLinkGutter() {
		return 0;
	}

	@Override
	public double getLinkInset() {
		return 0;
	}

	@Override
	public ConnectionAlignment getConnectionAlignment(Direction side) {
		return null;
	}

	@Override
	public RectangleRenderingInformation getRenderingInformation() {
		return null;
	}

	@Override
	public ContainerPosition getContainerPosition() {
		return null;
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	public String toString() {
		return "[grid-coordinate: "+id+"]";
	}


}
