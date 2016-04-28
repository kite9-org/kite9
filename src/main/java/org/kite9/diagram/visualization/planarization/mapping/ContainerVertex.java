package org.kite9.diagram.visualization.planarization.mapping;

import org.kite9.diagram.common.elements.CornerVertex;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.primitives.Container;

/**
 * Represents the special start and end vertices in the diagram, and for each container which are added
 * to assist planarization and orthogonalization to represent containment of
 * other vertices
 * 
 * @author robmoffat
 * 
 */
public class ContainerVertex extends CornerVertex {

	public static final int LOWEST_ORD = 0;
	public static final int HIGHEST_ORD = 1000;
	
	@Override
	public boolean hasDimension() {
		return false;
	}

	int xOrd, yOrd;
	
	public ContainerVertex(Container c, int xOrd, int yOrd) {
		super(c.getID()+"_"+xOrd+"_"+yOrd, xOrd == 0 ? HPos.LEFT : HPos.RIGHT, yOrd == 0 ? VPos.UP : VPos.DOWN, c);
		this.xOrd = xOrd;
		this.yOrd = yOrd;
	}

	public Container getOriginalUnderlying() {
		return (Container) super.getOriginalUnderlying();
	}
	
	public int getXOrdinal() {
		return xOrd;
	}
	
	public int getYOrdinal() {
		return yOrd;
	}

}