package org.kite9.diagram.visitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;


/**
 * Walks the diagram element containment hierarchy.
 * 
 * @author robmoffat
 * 
 */
public class DiagramElementVisitor {

	Set<DiagramElement> visited = new HashSet<DiagramElement>();
	
	
	/**
	 * Start here by calling this method on the top-level diagram element container.
	 */
	public void visit(Container d, VisitorAction va) {
		push(d, visited, va);
	}

	public void visit(Collection<? extends DiagramElement> toVisit, VisitorAction va) {
		for (DiagramElement element : toVisit) {
			push(element, visited, va);
		}
	}

	protected void push(DiagramElement d, Set<DiagramElement> visited, VisitorAction va) {
		if (visited.contains(d))
			return;
	    	visited.add(d);
		va.visit(d);
		visitChildren(d,visited,va);
	}

	protected void visit(Container c, Set<DiagramElement> visited, VisitorAction va) {
		if (c==null)
			return;
		
		for (DiagramElement d : c.getContents()) {
			push(d, visited, va);
		}
		
		if (c.getLabel()!=null) {
			push(c.getLabel(), visited, va);
		}
		
		if (c instanceof Connected) {
		    for (Connection con : ((Connected)c).getLinks()) {
		    	push(con, visited, va);
		    }    
		}
		
		
	}

	protected void visitChildren(DiagramElement de, Set<DiagramElement> visited, VisitorAction va) {
		if (de instanceof Container) {
			visit((Container) de, visited, va);
		} else if (de instanceof Connected) {
			visit((Connected) de, visited, va);
		} else if (de instanceof Connection) {
			visit((Connection) de, visited, va);
		}
	}

	protected void visit(Connected v, Set<DiagramElement> visited, VisitorAction va) {
		for (Connection element : v.getLinks()) {
			push(element, visited, va);
		}
	}

	protected void visit(Connection element, Set<DiagramElement> visited, VisitorAction va) {
		push(element.getFrom(), visited, va);
		push(element.getTo(),  visited, va);
		
		if (element.getFromLabel()!=null) {
			push(element.getFromLabel(), visited, va);
		}
		
		if (element.getToLabel()!=null) {
			push(element.getToLabel(), visited, va);
		}
		
	}

}
