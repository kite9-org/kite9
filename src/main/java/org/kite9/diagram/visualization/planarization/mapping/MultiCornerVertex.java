package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractAnchoringVertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.VPos;
import org.kite9.framework.common.Kite9ProcessingException;

/**
 * Represents corners of diagrams, containers and any other rectangular content.
 * For elements with grid-layout, these also represent points within the grid that will need to be connected up.
 * Multi-corners can be the corners of multiple different diagram elements.
 */
public class MultiCornerVertex extends AbstractAnchoringVertex {
	
	public static final boolean isMin(BigFraction b) {
		return b.equals(BigFraction.ZERO);
	}
	
	public static final boolean isMax(BigFraction b) {
		return b.equals(BigFraction.ONE);
	}
	
	
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
	private DiagramElement c;
	private List<Anchor> anchors = new ArrayList<AbstractAnchoringVertex.Anchor>(4);
	
	public List<Anchor> getAnchors() {
		return anchors;
	}

	public MultiCornerVertex(String id, DiagramElement c, BigFraction xOrd, BigFraction yOrd) {
		super(id+"_"+xOrd+"_"+yOrd);
		this.c = c;
		this.xOrd = xOrd;
		this.yOrd = yOrd;
	}

	/**
	 * Not reliable with MultiCornerVertex.  Consider removing, or using isPartOf, below.
	 * Will return the top-level container of a grid structure, but within a grid a vertex
	 * could be involved in multiple containers.
	 */
	public DiagramElement getOriginalUnderlying() {
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
	
	public boolean hasAnchorFor(DiagramElement c) {
		for (Anchor anchor : anchors) {
			if (anchor.getDe() == c) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean isPartOf(DiagramElement c) {
		return hasAnchorFor(c);
	}
	
	public static Container getRootGridContainer(DiagramElement cc) {
		Container parent = cc.getContainer();
		if (parent == null) {
			return (Container) cc;
		}
		
		if (parent.getLayout() == Layout.GRID) {
			return getRootGridContainer(parent);
		}
		
		return (Container) cc;
	}
}