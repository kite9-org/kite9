package org.kite9.diagram.visualization.compaction.position;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.logging.LogicException;


/**
 * Decorates the compaction process to handle Connections which have RouteRenderingInformation. 
 * 
 * Needs to be done after the positions of all edges have been set. 
 * 
 * @author robmoffat
 *
 */
public class ConnectionRouteCompactionStep implements CompactionStep {
	
	public void compact(Compaction c, Embedding r, Compactor cr) {
		if (r.isTopEmbedding()) {
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
					if (((Connection)de).getRenderingInformation().isRendered()) {
						out.add((Connection) de);
					}
				}
			}
		}	
		return out;
	}

	public void setRoute(Connection tle, List<Vertex> vertices, Compaction c) { 
		RouteRenderingInformation out = tle.getRenderingInformation();
		out.clear();
		boolean first = true;
		Dimension2D last = null;
		Dimension2D prev = null;
		
		for (Vertex v : vertices) {
			Dimension2D next = addToRoute(out, v, c, last);
			
			if (first && (last != null) && (next != null)) {
				setTerminatorPositionAndOrientation(tle.getFromDecoration(), last, next);
				first = false;
			}
			
			if (next != null) {
				prev = last;
				last = next;
			}
		}
		
		setTerminatorPositionAndOrientation(tle.getToDecoration(), last, prev);
	}

	private Dimension2D addToRoute(RouteRenderingInformation out, Vertex v, Compaction c, Dimension2D prev) {
		Slideable<Segment> horizSeg = c.getHorizontalSegmentSlackOptimisation().getVertexToSlidableMap().get(v);
		Slideable<Segment> vertSeg = c.getVerticalSegmentSlackOptimisation().getVertexToSlidableMap().get(v);
		
		double x2 = vertSeg == null ? prev.x() : vertSeg.getMinimumPosition();
		double y2 = horizSeg == null ? prev.y() : horizSeg.getMinimumPosition();
		
		Dimension2D p1 = new Dimension2D(x2,y2);
		//boolean hop = false;  // should be based on v.
		
		if (prev != null) {
			if ((prev.x() == p1.x()) && (prev.y() == p1.y())) {
				return null;	// prevent duplicates in the list
			}
		}
		
		out.add(p1);
		return p1;
	}
		
	private void setTerminatorPositionAndOrientation(Terminator t, Dimension2D pos, Dimension2D from) {
		int r = getTerminatorRotation(pos, from);

		double x1 = pos.x() - getRotatedSize(t,r,Direction.LEFT);
		double y1 = pos.y() - getRotatedSize(t,r,Direction.UP);
		double x2 = pos.x() + getRotatedSize(t,r,Direction.RIGHT);
		double y2 = pos.y() + getRotatedSize(t,r,Direction.DOWN);

		RectangleRenderingInformation rri = t.getRenderingInformation();
		
		rri.setPosition(new Dimension2D(x1, y1));
		rri.setSize(new Dimension2D(x2-x1, y2-y1));
	}

	private double getRotatedSize(Terminator t, int r, Direction d) {
		while (r > 0) {
			d = Direction.rotateClockwise(d);
			r --;
		}
	
		return t.getPadding(d);
	}

	protected int getTerminatorRotation(Dimension2D pos, Dimension2D from) {
		int r = 0;
		if (from.y() == pos.y()) {
			// horizontal
			if (from.x() < pos.x()) {
				// right
				r = 2;
			} else if (from.x() > pos.x()) {
				// left
				r = 0;
			} else {
				throw new LogicException();
			}
		} else if (from.x() == pos.x()){
			// vertical
			
			if (from.y() < pos.y()) {
				// down
				r = 1;
			} else if (from.y() > pos.y()) {
				// up
				r = 3;
			} else {
				throw new LogicException();
			}
			
		} else {
			throw new LogicException();
		}
		return r;
	}
	
}
