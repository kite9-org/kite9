package org.kite9.diagram.visualization.compaction.route;

import java.util.Collections;
import java.util.List;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.EdgeCrossingVertex;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Tools;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Link;

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

	public void compactDiagram(Compaction c) {
		Orthogonalization o = c.getOrthogonalization();
		setEdgeRoutes(o);
	}

	/**
	 * Works out the route taken by an Edge by the intermediate 'waypoint' vertices
	 */
	private void setEdgeRoutes(Orthogonalization o) {
		for (Edge e : o.getEdges()) {
			if (e.getOriginalUnderlying() instanceof Connection) {
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
			} else if (e.getOriginalUnderlying() instanceof Container) {
				setBounds((Container) e.getOriginalUnderlying(), e.getFrom());
				setBounds((Container) e.getOriginalUnderlying(), e.getTo());
			}
		}
	}
		
	private void setBounds(Container de, Vertex v) {
		RectangleRenderingInformation rri = de.getRenderingInformation();
		double x = v.getX();
		double y = v.getY();
		
		if (de instanceof Diagram) {
			increaseBounds(rri, x, y);
		} else if ((rri.getPosition().x() == 0) && (rri.getPosition().y() == 0)) {
			// initialize bounds
			rri.setPosition(new Dimension2D(x, y));
			rri.setSize(new Dimension2D(0,0));
		} else {
			increaseBounds(rri, x, y);
		}
		
	}

	private void increaseBounds(RectangleRenderingInformation rri, double x, double y) {
		double minx = rri.getPosition().x(), maxx=rri.getSize().x() + minx, 
			miny = rri.getPosition().y(), maxy = rri.getSize().y() + miny;
		
		minx = Math.min(x, minx);
		maxx = Math.max(x, maxx);
		miny = Math.min(y, miny);
		maxy = Math.max(y, maxy);
		rri.setPosition(new Dimension2D(minx, miny));
		rri.setSize(new Dimension2D(maxx - minx, maxy - miny));
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
					
					if (originalUnderlying instanceof Link) {
						// TODO: remove link ref here
						if (edgeStyle==null) {
							edgeStyle = ((Link)originalUnderlying).getStyle();
						} else if (edgeStyle != ((Link)originalUnderlying).getStyle()) {
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
