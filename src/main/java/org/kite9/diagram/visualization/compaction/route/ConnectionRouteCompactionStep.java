package org.kite9.diagram.visualization.compaction.route;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Tools;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.framework.logging.LogicException;


/**
 * Decorates the compaction process to handle connections which have RouteRenderingInformation. 
 * 
 * Needs to be done after the positions of all edges have been set. 
 * 
 * @author robmoffat
 *
 */
public class ConnectionRouteCompactionStep implements CompactionStep {
	
	public void compactDiagram(Compaction c) {
		Orthogonalization o = c.getOrthogonalization();
		Set<Connection> edgeSet = createTopElementSet(o.getEdges());
		Planarization p = c.getOrthogonalization().getPlanarization();
		
		for (Connection de : edgeSet) {
			setRoute(de, p.getEdgeMappings().get(de));
		}
	}

	private Set<Connection> createTopElementSet(Set<Edge> allEdges) {
		Set<Connection> out  = new LinkedHashSet<Connection>();
		for (Edge e : allEdges) {
			DiagramElement underlying = e.getOriginalUnderlying();
			if (underlying instanceof Connection) {
				out.add((Connection) underlying);
			}
		}	
		return out;
	}

	public void setRoute(Connection tle, EdgeMapping partSet) { 
		RouteRenderingInformation out = (RouteRenderingInformation) tle.getRenderingInformation();
		out.clear();
		Vertex start = partSet.getStartVertex();
		Vertex end = traceRoute(start, out, partSet.getEdges());
		
		if (start.isPartOf(tle.getFrom())) {
			// right way round
		} else if (end.isPartOf(tle.getFrom())) {
			out.reverse();
		} else {
			throw new LogicException("Can't determine whether route is right way round");
		}
	}


	private Vertex traceRoute(Vertex start, RouteRenderingInformation out, Deque<Edge> partSet) {
		Iterator<Edge> it = partSet.iterator();
		while (it.hasNext()) {
			Edge e = it.next();
			RouteRenderingInformation toUse = (RouteRenderingInformation) e.getRenderingInformation();
			boolean backwards = e.getTo() == start;
			addToRoute(toUse, out, backwards);
			start = e.otherEnd(start);
		}
		
		return start;
	}

	private void addToRoute(RouteRenderingInformation r, RouteRenderingInformation out, boolean reverse) {
		Dimension2D last = out.getRoutePositions().size()==0 ? null : (Dimension2D) out.getRoutePositions().get(out.getRoutePositions().size()-1);;
		if (!reverse) {
			for (int i = 0; i < r.size(); i++) {
				Dimension2D p1 = r.getWaypoint(i);
				Tools.checkSingleDimensionChange(p1, last);
				out.add(p1);
				if (r.isHop(i)) {
					out.setHop(out.size()-1);
				}
				last = p1;
			}
		} else {
			for (int i = r.size()-1; i >= 0; i--) {
				Dimension2D p1 = r.getWaypoint(i);
				Tools.checkSingleDimensionChange(p1, last);
				out.add(p1);
				if (r.isHop(i)) {
					out.setHop(out.size()-1);
				}
				last = p1;
			}
		}
	}
		
	
}
