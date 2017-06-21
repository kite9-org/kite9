package org.kite9.diagram.common.elements.vertex;

import java.util.Set;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.DiagramElement;

/**
 * Special marker vertex that allows us to represent fan turns.
 * 
 * @author robmoffat
 *
 */
public class FanVertex extends AbstractVertex implements NoElementVertex {
	
	private final boolean inner;
	private final Connected fanForEnd;
	
	
	public Connected getFanForEnd() {
		return fanForEnd;
	}

	public boolean isInner() {
		return inner;
	}

	public FanVertex(String id, boolean inner, Connected c) {
		super(id);
		this.inner = inner;
		this.fanForEnd = c;
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