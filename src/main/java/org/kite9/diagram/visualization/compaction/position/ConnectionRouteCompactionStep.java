package org.kite9.diagram.visualization.compaction.position;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;


/**
 * Decorates the compaction process to handle connections which have RouteRenderingInformation. 
 * 
 * Needs to be done after the positions of all edges have been set. 
 * 
 * @author robmoffat
 *
 */
public class ConnectionRouteCompactionStep implements CompactionStep {
	
	public void compact(Compaction c, Rectangular r, Compactor cr) {
		if (r instanceof Diagram) {
			Set<Connection> renderedConnections = createTopElementSet(c.getOrthogonalization());
			
			for (Connection de : renderedConnections) {
				setRoute(de, c.getOrthogonalization().getWaypointsForBiDirectional(de), c);
			}
		}
	}

	private Set<Connection> createTopElementSet(Orthogonalization c) {
		Set<Connection> out  = new LinkedHashSet<Connection>();
		for (Dart e : c.getAllDarts()) {
			for (DiagramElement de : e.getDiagramElements().keySet()) {
				if (de instanceof Connection) {
					out.add((Connection) de);
				}
			}
		}	
		return out;
	}

	public void setRoute(Connection tle, List<Vertex> vertices, Compaction c) { 
		RouteRenderingInformation out = (RouteRenderingInformation) tle.getRenderingInformation();
		out.clear();
		Dimension2D last = null;
		
		for (Vertex v : vertices) {
			last = addToRoute(out, v, c, last);
		}
	}

	private Dimension2D addToRoute(RouteRenderingInformation out, Vertex v, Compaction c, Dimension2D prev) {
		Slideable<Segment> horizSeg = c.getHorizontalSegmentSlackOptimisation().getVertexToSlidableMap().get(v);
		Slideable<Segment> vertSeg = c.getVerticalSegmentSlackOptimisation().getVertexToSlidableMap().get(v);
		
		double x2 = vertSeg == null ? prev.x() : vertSeg.getMinimumPosition();
		double y2 = horizSeg == null ? prev.y() : horizSeg.getMinimumPosition();
		
		Dimension2D p1 = new Dimension2D(x2,y2);
		//boolean hop = false;  // should be based on v.
		
		out.add(p1);
		return p1;
	}
		
	
}
