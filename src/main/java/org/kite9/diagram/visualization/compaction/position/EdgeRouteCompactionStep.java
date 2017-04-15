package org.kite9.diagram.visualization.compaction.position;

import java.util.Collections;
import java.util.List;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.vertex.EdgeCrossingVertex;
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
import org.kite9.diagram.visualization.compaction.Tools;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * Having identified the position of each segment, this step sets the rendering information
 * on the edge routes to give the coordinate routes through which they should go.
 * 
 * This step must come after a {@link SegmentPositioner} step.
 * 
 * @author robmoffat
 *
 */
public class EdgeRouteCompactionStep implements CompactionStep {

	public void compact(Compaction c, Rectangular r, Compactor cr) {
		if (r instanceof Diagram) {
			Orthogonalization o = c.getOrthogonalization();
			setVertexPositions(c.getXSlackOptimisation());
			setVertexPositions(c.getYSlackOptimisation());
			setEdgeRoutes(o);
		}
	}

	private void setVertexPositions(SegmentSlackOptimisation so) {
		for (Slideable<Segment> s : so.getAllSlideables()) {
			Segment und = s.getUnderlying();
			double pos = s.getMinimumPosition();
			und.setPosition(pos);
		}
	}

	/**
	 * Works out the route taken by an Edge by the intermediate 'waypoint' vertices
	 */
	private void setEdgeRoutes(Orthogonalization o) {
		for (Edge e : o.getEdges()) {
			if (e instanceof ConnectionEdge) {
				List<Vertex> waypoints = o.getWaypointMap().get(e);
				if (waypoints!=null) {
					// waypoints needs to be in same order as edge
					if ((Tools.getUltimateElement(e.getFrom())!=Tools.getUltimateElement(waypoints.get(0))) || (Tools.getUltimateElement(e.getTo())!=Tools.getUltimateElement(waypoints.get(waypoints.size()-1)))) {
						Collections.reverse(waypoints);
					}
					
					RouteRenderingInformation route = (RouteRenderingInformation) e.getRenderingInformation();
					
					Dimension2D last = null;
					for (Vertex vertex : waypoints) {
						Dimension2D p1 = vertex.getPosition();
						Tools.checkSingleDimensionChange(p1, last);
						route.add(new Dimension2D(p1.x(), p1.y()));
						last = p1;
					}
					
					if (checkForHop(waypoints.get(0))) {
						route.setHop(0);
					}
					
					if (checkForHop(waypoints.get(waypoints.size()-1))) {
						route.setHop(waypoints.size()-1);
					}
				}			
			} 
		}
	}
		

	private boolean checkForHop(Vertex v) {
		String edgeStyle = null;
		if (v instanceof EdgeCrossingVertex) {
			// this next part checks to see we are crossing another edge, and not a container border
			for (Edge e : v.getEdges()) {
				if (!(e instanceof Dart)) {
					DiagramElement originalUnderlying = e.getOriginalUnderlying();
					if (!(originalUnderlying instanceof Connection)) {
						return false;
					}
					
					if (originalUnderlying instanceof Connection) {
						// TODO: remove link ref here
						if (edgeStyle==null) {
							edgeStyle = ((Connection)originalUnderlying).getStyle();
						} else if (edgeStyle != ((Connection)originalUnderlying).getStyle()) {
							// means incident edges are diferent styles, no hop needed
							return false;
						}
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
}
