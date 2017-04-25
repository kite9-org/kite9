package org.kite9.diagram.visualization.planarization.mgt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.edge.SingleElementPlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
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
		Collection<PlanarizationEdge> toRemove = new DetHashSet<PlanarizationEdge>();
		for (Entry<DiagramElement, EdgeMapping> mapping : pln.getEdgeMappings().entrySet()) {
			DiagramElement de = mapping.getKey();
			if (de instanceof BiDirectional<?>) {
				Connected from = ((BiDirectional<Connected>) de).getFrom();
				Connected to = ((BiDirectional<Connected>) de).getTo();
				
				if ((from instanceof Container) || (to instanceof Container)) {
					EdgeMapping edgeMapping = mapping.getValue();
					List<PlanarizationEdge> forwardList = edgeMapping.getEdges();
					eraseEnds(edgeMapping, de, from, to, toRemove, forwardList, edgeMapping.getStartVertex());
					
					if (toRemove.size() > 0) {
						edgeMapping.remove(toRemove);
						for (PlanarizationEdge edge : toRemove) {
							t.removeEdge(edge, pln);
						}
						toRemove.clear();
					}
				}
			}
		}
	}
	
	/**
	 * Work along the edges (edgeMapping list) until you are no longer inside the diagram element
	 */
	private void eraseEnds(EdgeMapping mapping, DiagramElement de, DiagramElement from, DiagramElement to, Collection<PlanarizationEdge> toRemove, List<PlanarizationEdge> edges, Vertex start) {
		boolean outside = true;
		for (PlanarizationEdge edge : edges) {
			boolean change = start.isPartOf(from) || start.isPartOf(to);
			
			if (change) {
				outside = !outside;
				log.send("Changing to: outside="+outside);
			} 
			
			
			if (outside) {
				log.send(log.go() ? null : "Removing edge "+edge+" as it's not part of "+de);
				toRemove.add(edge);
			} else {
				// do nothing, good edge
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
