package org.kite9.diagram.common.elements.edge;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;

/**
 * This is an edge created in the planarization process.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractPlanarizationEdge extends AbstractEdge implements PlanarizationEdge {

	public int getBendCost() {
		return 1;
	}
	
    public AbstractPlanarizationEdge(Vertex from, Vertex to, Direction d) {
    	super(from, to , d);		
		from.addEdge(this);
		to.addEdge(this);
	}

	
	@Override
	public String toString() {
		return "["+getID()+"/"+from+"-"+to+"]";
	}
	
	public void remove() {
		from.removeEdge(this);
		to.removeEdge(this);
	}
	
	public abstract RemovalType removeBeforeOrthogonalization();
	
	public abstract int getCrossCost();

	protected boolean straight = true;

	public boolean isStraightInPlanarization() {
		return straight;
	}

	public void setStraight(boolean straight) {
		this.straight = straight;
	}

	@Override
	public Direction getFromArrivalSide() {
		return Direction.reverse(drawDirection);
	}

	@Override
	public Direction getToArrivalSide() {
		return drawDirection;
	}

	
}
