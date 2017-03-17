package org.kite9.diagram.model.visitors;

import org.kite9.diagram.model.DiagramElement;

public interface VisitorAction {

	public void visit(DiagramElement de);
}
