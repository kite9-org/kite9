package org.kite9.diagram.common.elements.vertex;

import java.util.List;
import java.util.Set;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;

/**
 * Special marker vertex that allows us to represent fan turns.
 * 
 * @author robmoffat
 *
 */
public class FanVertex extends AbstractVertex implements NoElementVertex {
	
	private final boolean inner;
	private final List<Direction> fanFromSide;
	
	public List<Direction> getFanSides() {
		return fanFromSide;
	}

	public boolean isInner() {
		return inner;
	}

	public FanVertex(String id, boolean inner, List<Direction> fanFromSide) {
		super(id);
		this.inner = inner;
		this.fanFromSide = fanFromSide;
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return null;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return false;
	}
	
}