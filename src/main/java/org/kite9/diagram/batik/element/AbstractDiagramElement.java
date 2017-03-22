package org.kite9.diagram.batik.element;

import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.model.Container;
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

	/**
	 * Remove later.
	 */
	@Override
	public Container getContainer() {
		return (Container) getParent();
	}
	

}