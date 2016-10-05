package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractAnchoringVertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;
import org.kite9.framework.common.Kite9ProcessingException;

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
	
	public static BigFraction getOrdForXDirection(Direction d) {
		switch (d) {
		case LEFT:
			return BigFraction.ZERO;
		case RIGHT:
			return BigFraction.ONE;
		default:
			return BigFraction.ONE_HALF;
		}
	}
	
	public static BigFraction getOrdForYDirection(Direction d) {
		switch (d) {
		case UP:
			return BigFraction.ZERO;
		case DOWN:
			return BigFraction.ONE;
		default:
			return BigFraction.ONE_HALF;
		}
	}
	
	@Override
	public boolean hasDimension() {
		return false;
	}

	private BigFraction xOrd, yOrd;
	private Container c;
	private List<Anchor> anchors = new ArrayList<AbstractAnchoringVertex.Anchor>(4);
	
	public ContainerVertex(Container c, BigFraction xOrd, BigFraction yOrd) {
		super(c.getID()+"_"+xOrd+"_"+yOrd);
		this.c = c;
		this.xOrd = xOrd;
		this.yOrd = yOrd;
	}

	public Container getOriginalUnderlying() {
		return c;
	}
	
	public BigFraction getXOrdinal() {
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

	public BigFraction getYOrdinal() {
		return yOrd;
	}
	
	public void addAnchor(HPos lr, VPos ud, DiagramElement underlying) {
		anchors.add(new Anchor(ud, lr, underlying));
	}
	
	public VPos getVPosFor(Container c) {
		for (Anchor anchor : anchors) {
			if (anchor.getDe() == c) {
				return anchor.getUd();
			}
		}
		
		throw new Kite9ProcessingException("No anchor found for container "+c);
	}
	
	public HPos getHPosFor(Container c) {
		for (Anchor anchor : anchors) {
			if (anchor.getDe() == c) {
				return anchor.getLr();
			}
		}
		
		throw new Kite9ProcessingException("No anchor found for container "+c);
	}
	
	public Set<DiagramElement> getAllAnchoredContainers() {
		Set<DiagramElement> out = new HashSet<>();
		for (Anchor a : anchors) {
			out.add(a.getDe());
		}
		
		return out;
	}
	
}