package org.kite9.framework.dom;

import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;

public abstract class AbstractDiagramElement implements DiagramElement {

	protected DiagramElement parent;
	protected HintMap hints;
	
	public AbstractDiagramElement(DiagramElement parent) {
		super();
		this.parent = parent;
	}

	public int compareTo(DiagramElement o) {
		return getID().compareTo(o.getID());
	}

	@Override
	public int hashCode() {
		String id = getID();
		return id.hashCode();
	}

	public AbstractDiagramElement() {
		super();
	}

	@Override
	public DiagramElement getParent() {
		return parent;
	}
	
	private int depth = -1;

	@Override
	public int getDepth() {
		if (depth == -1) {
			if (this instanceof Diagram) {
				depth = 0;
			} else {
				depth = getParent().getDepth() + 1;
			}
		}

		return depth;
	}
	
	

}