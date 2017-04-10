package org.kite9.diagram.visualization.orthogonalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;


/**
 * Contains some utility methods that can be used by Orthogonalizers.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractOrthogonalizer implements Orthogonalizer, Logable {
	
	protected Kite9Log log = new Kite9Log(this);

	/**
	 * Used to create the dart ordering map, ensuring that darts leaving a
	 * vertex are ordered in the same order as in the Planarization. Ensures
	 * clockwise ordering.
	 */
	public static void createDartOrdering(Planarization pln,
			Orthogonalization om) {
		for (Vertex v : om.getAllVertices()) {
			if (v.hasDimension() || (v instanceof MultiCornerVertex)) {
				List<Dart> ld = new ArrayList<Dart>();
				for (Edge e : v.getEdges()) {
					if (e instanceof Dart) {
						ld.add((Dart) e);
					}
				}

				// sort according to edge order
				final VertexEdgeOrdering edgeOrdering = (VertexEdgeOrdering) pln.getEdgeOrderings().get(v);
				final List<Edge> edgeOrder = edgeOrdering.getEdgesAsList();
				final Vertex vv = v;
				Collections.sort(ld, new Comparator<Dart>() {

					public int compare(Dart arg0, Dart arg1) {
						int i0 = edgeOrder.indexOf(arg0.getUnderlying());
						int i1 = edgeOrder.indexOf(arg1.getUnderlying());
						if ((i0==-1) || (i1==-1)) {
							throw new LogicException("All edges should be part of the edge ordering for "+vv+" "+edgeOrder);
						}
						if (i0 < i1) {
							return -1;
						} else if (i0 > i1) {
							return 1;
						} else {
							boolean out0 = arg0.getFrom() == vv;
							boolean out1 = arg1.getFrom() == vv;
							if (out1) {
								return -1;
							} else if (out0) {
								return 1;
							} else {
								return 0;
							}
						}
					}

				});

				om.getDartOrdering().put(v, ld);

			}
		}
	}

	/**
	 * Helper method to ensure that the dart ordering starts with the top-left
	 * corner dart. Call after createDartOrdering
	 */
	public void orderDartsFromCorner(
			Map<Vertex, List<Dart>> cornerDarts, Orthogonalization om) {
		for (Vertex v : cornerDarts.keySet()) {

			if (v.hasDimension()) {
				
				List<Dart> ld = om.getDartOrdering().get(v);
				log.send(log.go() ? null : "Dart order (before): "+v+" is "+ld);

				List<Dart> corners = cornerDarts.get(v);
				if ((corners!=null) && (corners.size()>0)) {
					// move first corner round to start
					Dart cd = corners.get(0);
					int idx = ld.indexOf(cd);
					Collections.rotate(ld, -idx);
					log.send(log.go() ? null : "Dart order (after) : "+v+" is "+ld);
				}
			}
		}
	}

	public String getPrefix() {
		return "ORTH";
	}

	public boolean isLoggingEnabled() {
		return false;
	}
}
