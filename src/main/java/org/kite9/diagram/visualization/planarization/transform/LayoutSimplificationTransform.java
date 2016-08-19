package org.kite9.diagram.visualization.planarization.transform;

import java.util.List;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.style.DiagramElement;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mapping.GeneratedLayoutElement;

/**
 * Simplifies the layout of the planarization by looking for layout edges, and, if they are part of a small
 * square face with a connection and two container edges, remove the layout edge and use the connection for 
 * enforcing the layout instead.
 * 
 */
public class LayoutSimplificationTransform implements PlanarizationTransform {

	Tools t = new Tools();
	
	@Override
	public void transform(Planarization pln) {
//		if (true) 
//			return;
		
		List<Face> faces = pln.getFaces();
		int currentFace = 0;
		
		while (currentFace < faces.size()) {
			Face f = faces.get(currentFace);
			boolean removalDone = false;
			if ((f.size() == 4) && (!f.isOuterFace())) {
				// case of two containers directed against each other
				for (int i = 0; i < 4; i++) {
					Edge e = f.getBoundary(i);
					DiagramElement under = e.getOriginalUnderlying();
					if ((under instanceof GeneratedLayoutElement) && (((PlanarizationEdge)e).isLayoutEnforcing()) 
						&& isContainerEdge(f.getBoundary(i + 1)) 
						&& isContainerEdge(f.getBoundary(i - 1)) 
						&& isConnectionEdge(f.getBoundary(i+2))) {
						Vertex es = f.getCorner(i);
						Edge c = f.getBoundary(i+2);
						Vertex cs = f.getCorner(i+2);
						
						removalDone = performRemoval(pln, e, es, c, cs);
						break;
					}
				}
					
					
			} else if ((f.size() == 2) && (!f.isOuterFace())) {
				// case of 2 vertices directed against each other
				for (int i = 0; i < 2; i++) {
					Edge e = f.getBoundary(i);
					DiagramElement under = e.getOriginalUnderlying();
					if ((under instanceof GeneratedLayoutElement) && (((PlanarizationEdge)e).isLayoutEnforcing()) 
						&& isConnectionEdge(f.getBoundary(i+1))) {
						Vertex es = f.getCorner(i);
						Edge c = f.getBoundary(i+1);
						Vertex cs = f.getCorner(i+1);
						
						removalDone = performRemoval(pln, e, es, c, cs);
						break;
					}
				}
			} 
			else if ((f.size() == 3) && (!f.isOuterFace())) {
				// case of one container, one dimensioned vertex directed against each other
				for (int i = 0; i < 3; i++) {
					Edge e = f.getBoundary(i);
					DiagramElement under = e.getOriginalUnderlying();
					if ((under instanceof GeneratedLayoutElement) && (((PlanarizationEdge)e).isLayoutEnforcing()) && hasDimensionedEnd(e)) {
						if (isContainerEdge(f.getBoundary(i + 1)) && isConnectionEdge(f.getBoundary(i - 1))) {
							removalDone = performRemoval(pln, f, e, i-1, i);
						} else if (isContainerEdge(f.getBoundary(i - 1)) && isConnectionEdge(f.getBoundary(i + 1))) {
							removalDone = performRemoval(pln, f, e, i+1, i);
						} 
						break;
					}
				}
			}
			
			if (removalDone) {
				removalDone = false;
			} else {
				currentFace ++;
			}
		}
	}

	private boolean hasDimensionedEnd(Edge e) {
		return e.getFrom().hasDimension() || e.getTo().hasDimension();
	}

	private boolean performRemoval(Planarization pln, Face f, Edge e, int ci,
			int ei) {
		boolean removalDone;
		Vertex es = f.getCorner(ei);
		Edge c = f.getBoundary(ci);
		Vertex cs = f.getCorner(ci);
		removalDone = performRemoval(pln, e, es, c, cs);
		return removalDone;
	}

	private boolean performRemoval(Planarization pln, Edge e, Vertex es, Edge c,
			Vertex cs) {
		
		if (Tools.isUnderlyingContradicting(c)) {
			return false;
		}
		
		Direction layoutDirection = e.getDrawDirectionFrom(es);
		Direction connectionDirection = c.getDrawDirectionFrom(cs);
		
		if (connectionDirection == null) {
			c.setDrawDirectionFrom(Direction.reverse(layoutDirection), cs);
		}
		
		((PlanarizationEdge)c).setLayoutEnforcing(true);
		t.removeEdge(e, pln);
		
		return true;
	}

	private boolean isConnectionEdge(Edge boundary) {
		return boundary.getOriginalUnderlying() instanceof Connection;
	}

	private boolean isContainerEdge(Edge boundary) {
		return boundary.getOriginalUnderlying() instanceof Container;
	}

}
