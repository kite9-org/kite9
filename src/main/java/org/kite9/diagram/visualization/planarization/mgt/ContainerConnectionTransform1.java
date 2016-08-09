package org.kite9.diagram.visualization.planarization.mgt;

import java.util.Collection;
import java.util.Map.Entry;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.Container;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Edges connecting to a container either connect to either the container start vertex or the container end vertex,
 * or one of the vertices witin the container.
 * 
 * When connecting to one of the vertices within the container, we must remove the remainder of the edge
 * which is inside the container itself, leaving just the part connecting to the container edge.
 * 
 * This transform is necessary when you allow connections to containers.
 * 
 * @author robmoffat
 * 
 */
public class ContainerConnectionTransform1 implements PlanarizationTransform, Logable {
		
	private Kite9Log log = new Kite9Log(this);

	public ContainerConnectionTransform1(ElementMapper elementMapper) {
	}

	public void transform(Planarization pln) {
		modifyInternalEdges(pln);
	}

	Tools t = new Tools();
	
	@SuppressWarnings("unchecked")
	private void modifyInternalEdges(Planarization pln) {		
		Collection<Edge> toRemove = new DetHashSet<Edge>();
		for (Entry<DiagramElement, EdgeMapping> mapping : pln.getEdgeMappings().entrySet()) {
			DiagramElement de = mapping.getKey();
			if (de instanceof BiDirectional<?>) {
				Connected from = ((BiDirectional<Connected>) de).getFrom();
				Connected to = ((BiDirectional<Connected>) de).getTo();
				
				if ((from instanceof Container) || (to instanceof Container)) {
					eraseEnds(mapping.getValue(), de, from, to, toRemove);
					
					if (toRemove.size() > 0) {
						mapping.getValue().remove(toRemove);
						for (Edge edge : toRemove) {
							t.removeEdge(edge, pln);
						}
						toRemove.clear();
					}
				}
			}
		}
	}
	
	private static enum EraseState { BEFORE, OK, AFTER };
	
	private void eraseEnds(EdgeMapping mapping,
			DiagramElement de, Connected from, Connected to, Collection<Edge> toRemove) {
		
		Vertex start = mapping.getStartVertex();
		
		EraseState clear = EraseState.BEFORE;
		
		for (Edge edge : mapping.getEdges()) {
			
			DiagramElement under = start.getOriginalUnderlying();
			if ((under == from) || (under==to)) {
				if (clear == EraseState.BEFORE) {
					clear = EraseState.OK;
				} else if (clear == EraseState.OK) {
					clear = EraseState.AFTER;
				}
			}
			
			if ((clear == EraseState.BEFORE) || (clear == EraseState.AFTER)) {
				log.send(log.go() ? null : "Removing edge "+edge+" as it's not part of "+de);
				toRemove.add(edge);
			}
			
			start = edge.otherEnd(start);
			
		}
	}

	public String getPrefix() {
		return "CET1";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
