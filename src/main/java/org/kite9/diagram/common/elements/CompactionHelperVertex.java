package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.DiagramElement;


/**
 * Vertex inserted to aid the compaction process.  Typically added during rectangularization.
 * 
 * @author robmoffat
 *
 */
public class CompactionHelperVertex extends AbstractVertex {

	public CompactionHelperVertex(String name) {
		super(name);
	}

	public DiagramElement getOriginalUnderlying() {
		return null;
	}

}