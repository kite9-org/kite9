package org.kite9.diagram.common.elements.vertex;

import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.model.DiagramElement;

/**
 * Vertex inserted to aid the compaction process.  Typically added during rectangularization.
 * 
 * @author robmoffat
 *
 */
public class CompactionHelperVertex extends AbstractVertex implements NoElementVertex {

	public CompactionHelperVertex(String name) {
		super(name);
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return false;
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return Collections.emptySet();
	}

	@Override
	public DiagramElement getOriginalUnderlying() {
		return null;
	}

}