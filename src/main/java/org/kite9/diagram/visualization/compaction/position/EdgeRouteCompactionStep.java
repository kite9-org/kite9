package org.kite9.diagram.visualization.compaction.position;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.vertex.EdgeCrossingVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
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
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.common.Kite9ProcessingException;

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

	private CompleteDisplayer displayer;
	
	public EdgeRouteCompactionStep(CompleteDisplayer displayer) {
		this.displayer = displayer;
	}

	public void compact(Compaction c, Rectangular r, Compactor cr) {
		if (r instanceof Diagram) {
			Orthogonalization o = c.getOrthogonalization();
			setVertexPositions(c.getHorizontalSegmentSlackOptimisation());
			setVertexPositions(c.getVerticalSegmentSlackOptimisation());
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
				List<Vertex> waypoints = o.getWaypointsForEdge((ConnectionEdge) e);
				if (waypoints!=null) {
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
		

	private Connected getUnderlyingConnected(ConnectionEdge e, Vertex from) {
		Connection und = e.getOriginalUnderlying();
		Connected out = null;
		for (DiagramElement de : from.getDiagramElements()) {
			if (de instanceof Connected) {
				if (und.meets((Connected) de)) {
					if (out == null) {
						out = (Connected) de;
					} else {
						throw new Kite9ProcessingException();
					}
				}
			}
		}
		
		return out;
	}

	private boolean checkForHop(Vertex v) {
		if (v instanceof EdgeCrossingVertex) {
			if (!consistsOfTwoConnections(v)) {
				return false;
			}
			
			Iterator<DiagramElement> it = v.getDiagramElements().iterator();
			
			Connection a = (Connection) it.next(); 
			Connection b = (Connection) it.next();
			
			return displayer.requiresHopForVisibility(a, b);
		}
		
		return false;
	}

	private boolean consistsOfTwoConnections(Vertex v) {
		for (DiagramElement de : v.getDiagramElements()) {
			if (!(de instanceof Connection)) {
				return false;
			}
		}
		
		if (v.getDiagramElements().size() != 2) {
			throw new Kite9ProcessingException("Should be two connections!");
		}
		
		return true;
	}
}
