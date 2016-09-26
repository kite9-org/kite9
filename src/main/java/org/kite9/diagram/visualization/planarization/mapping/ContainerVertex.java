package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractAnchoringVertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;

/**
 * Represents the special start and end vertices in the diagram, and for each container which are added
 * to assist planarization and orthogonalization to represent corners and points along the sides of the container.
 * 
 * For containers with grid-layout, these also represent points within the grid that will need to be connected up.
 * 
 * other vertices
 * 
 * @author robmoffat
 * 
 */
public class ContainerVertex extends AbstractAnchoringVertex {

	public static final int LOWEST_ORD = 0;
	public static final int HIGHEST_ORD = 1000;
	public static final int MID_ORD = 500;
	
	
	public static int getOrdForXDirection(Direction d) {
		switch (d) {
		case LEFT:
			return LOWEST_ORD;
		case RIGHT:
			return HIGHEST_ORD;
		default:
			return MID_ORD;
		}
	}
	
	public static int getOrdForYDirection(Direction d) {
		switch (d) {
		case UP:
			return LOWEST_ORD;
		case DOWN:
			return HIGHEST_ORD;
		default:
			return MID_ORD;
		}
	}
	
	@Override
	public boolean hasDimension() {
		return false;
	}

	private int xOrd, yOrd;
	private Container c;
	private List<Anchor> anchors = new ArrayList<AbstractAnchoringVertex.Anchor>(4);
	
	public ContainerVertex(Container c, int xOrd, int yOrd) {
		super(c.getID()+"_"+xOrd+"_"+yOrd);
		this.c = c;
		this.xOrd = xOrd;
		this.yOrd = yOrd;
	}

	public Container getOriginalUnderlying() {
		return c;
	}
	
	public int getXOrdinal() {
		return xOrd;
	}
	
	@Override
	public void setX(double x) {
		super.setX(x);
		for (Anchor anchor : anchors) {
			anchor.setX(x);
		}
	}

	@Override
	public void setY(double y) {
		super.setY(y);
		for (Anchor anchor : anchors) {
			anchor.setY(y);
		}
	}

	public int getYOrdinal() {
		return yOrd;
	}
	
	public void addAnchor(HPos lr, VPos ud, DiagramElement underlying) {
		anchors.add(new Anchor(ud, lr, underlying));
	}
	
}