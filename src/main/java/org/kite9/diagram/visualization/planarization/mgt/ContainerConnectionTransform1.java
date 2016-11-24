package org.kite9.diagram.visualization.planarization.mgt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Edges connecting to a container either connect to either a container vertex,
 * or one of the vertices within the container.
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
					EdgeMapping edgeMapping = mapping.getValue();
					List<Edge> forwardList = edgeMapping.getEdges();
					List<Edge> backwardList = new ArrayList<>(forwardList);
					Collections.reverse(backwardList);
					eraseEnd(edgeMapping, de, from, to, toRemove, forwardList, edgeMapping.getStartVertex());
					eraseEnd(edgeMapping, de, from, to, toRemove, backwardList, edgeMapping.getEndVertex());
					
					
					if (toRemove.size() > 0) {
						edgeMapping.remove(toRemove);
						for (Edge edge : toRemove) {
							t.removeEdge(edge, pln);
						}
						toRemove.clear();
					}
				}
			}
		}
	}
		
	private void eraseEnd(EdgeMapping mapping, DiagramElement de, DiagramElement from, DiagramElement to, Collection<Edge> toRemove, List<Edge> edges, Vertex start) {
		
		from = rootContainer(from);
		to = rootContainer(to);
		
		for (Edge edge : edges) {
			DiagramElement under = start.getOriginalUnderlying();
			under = rootContainer(under);
			boolean part2 = (under == from) || (under==to) ;
			if (part2) {
				return;
			}
			
			log.send(log.go() ? null : "Removing edge "+edge+" as it's not part of "+de);
			toRemove.add(edge);
			
			start = edge.otherEnd(start);
		}
	}

	private DiagramElement rootContainer(DiagramElement from) {
		return from instanceof Container ? ContainerVertex.getRootGridContainer((Container) from) : from;
	}

	public String getPrefix() {
		return "CET1";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
