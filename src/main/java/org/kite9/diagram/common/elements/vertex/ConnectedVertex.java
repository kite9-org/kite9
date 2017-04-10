package org.kite9.diagram.common.elements.vertex;

import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.DiagramElement;

/**
 * Vertex to represent a single {@link Connected} element during planarization.
 * 
 * @author robmoffat
 *
 */
public class ConnectedVertex extends AbstractVertex implements SingleElementVertex {

	@Override
	public boolean hasDimension() {
		return true;
	}

	Connected underlying;
	
	public ConnectedVertex(String id, Connected underlying) {
		super(id);
		this.underlying = underlying;
	}
	
	public Connected getOriginalUnderlying() {
		return underlying;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return underlying == de;
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return Collections.singleton(underlying);
	}
	
}
