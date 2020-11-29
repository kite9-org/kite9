package org.kite9.diagram.visualization.planarization.mgt.face;

import java.util.Collections;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;

public class TemporaryEdge extends AbstractPlanarizationEdge {
	
	private Rectangular above, below;
	
	public Rectangular getAbove() {
		return above;
	}

	public void setAbove(Rectangular above) {
		this.above = above;
	}

	public Rectangular getBelow() {
		return below;
	}

	public void setBelow(Rectangular below) {
		this.below = below;
	}

	public TemporaryEdge(Vertex from, Vertex to) {
		super(from, to, null);
	}

	public void remove() {
		getFrom().removeEdge(this);
		getTo().removeEdge(this);
	}

	public Vertex getLabelEnd() {
		return null;
	}

	@Override
	public int getCrossCost() {
		return 0; // no cost for crossing temporaries
	}

	@Override
	public RemovalType removeBeforeOrthogonalization() {
		return RemovalType.YES;
	}

	private boolean layoutEnforcing;

	public boolean isLayoutEnforcing() {
		return layoutEnforcing;
	}

	public void setLayoutEnforcing(boolean le) {
		this.layoutEnforcing = le;
	}

	@Override
	public PlanarizationEdge[] split(Vertex toIntroduce) {
		PlanarizationEdge[] out = new PlanarizationEdge[2];
		out[0] = new TemporaryEdge(getFrom(), toIntroduce);
		out[1] = new TemporaryEdge(toIntroduce, getTo());
		return out;
	}

	@Override
	public boolean isStraightInPlanarization() {
		return false;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return false;
	}

	@Override
	public Map<DiagramElement, Direction> getDiagramElements() {
		return Collections.emptyMap();
	}
}