package org.kite9.diagram.visualization.planarization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.planarization.ordering.BasicVertexEdgeOrdering;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Utility functions for manipulating the Planarization
 * 
 * @author robmoffat
 * 
 */
public class Tools implements Logable {

	Kite9Log log = new Kite9Log(this);

	int elementNo = 0;

	public String getPrefix() {
		return "PLNT";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

	/**
	 * This inserts the new EdgeCrossingVertex into an edge to break it in two
	 * @param underlying if provided, it is assumed we are breaking the container boundary.
	 */
	public Vertex breakEdge(PlanarizationEdge e, Planarization pln, Vertex split) {
		List<Face> faces = pln.getEdgeFaceMap().get(e);
		Vertex from = e.getFrom();
		Vertex to = e.getTo();

		VertexEdgeOrdering fromEdgeOrdering = (VertexEdgeOrdering) pln.getEdgeOrderings().get(from);
		VertexEdgeOrdering toEdgeOrdering = (VertexEdgeOrdering) pln.getEdgeOrderings().get(to);
		log.send(log.go() ? null : "Original edge order around " + from + " = " + fromEdgeOrdering);
		log.send(log.go() ? null : "Original edge order around " + to + " = " + toEdgeOrdering);

		// split the existing edge to create two edges		
		PlanarizationEdge[] newEdges = splitEdge(e, split, pln);

		// new edges will have same faces
		pln.getEdgeFaceMap().remove(e);
		pln.getEdgeFaceMap().put(newEdges[0], new ArrayList<Face>(faces));
		pln.getEdgeFaceMap().put(newEdges[1], new ArrayList<Face>(faces));
		
		// new vertex will have same faces as edge
		pln.getVertexFaceMap().put(split, new LinkedList<Face>(faces));

		// add to the edge ordering map. since there are only 2 edges, order not
		// important yet.
		List<PlanarizationEdge> edges = new ArrayList<PlanarizationEdge>();
		edges.add(newEdges[0]);
		edges.add(newEdges[1]);
		BasicVertexEdgeOrdering splitEdgeOrdering = new BasicVertexEdgeOrdering(edges, split);
		pln.getEdgeOrderings().put(split, splitEdgeOrdering);

		// update the from/to edge ordering
		fromEdgeOrdering.replace(e, newEdges[0].meets(from) ? newEdges[0] : newEdges[1]);
		toEdgeOrdering.replace(e, newEdges[1].meets(to) ? newEdges[1] : newEdges[0]);
		log.send(log.go() ? null : "New edge order around " + from + " = " + fromEdgeOrdering);
		log.send(log.go() ? null : "New edge order around " + to + " = " + toEdgeOrdering);
		log.send(log.go() ? null : "New edge order around " + split + " = " + splitEdgeOrdering);

		// now repair the face. there are two edges instead of the original one
		for (Face face : faces) {
			face.checkFaceIntegrity();
			List<Integer> indexes = face.indexOf(e);
			while (indexes.size() > 0) {
				int i = indexes.get(0);
				if ((face.getCorner(i) == from) && (face.getCorner(i + 1) == to)) {
					face.remove(i);
					face.add(i, from, newEdges[0]);
					face.add(i + 1, split, newEdges[1]);
				} else if ((face.getCorner(i) == to) && (face.getCorner(i + 1) == from)) {
					face.remove(i);
					face.add(i, to, newEdges[1]);
					face.add(i + 1, split, newEdges[0]);
				} else {
					throw new LogicException("Should be one way around or the other");
				}
				indexes = face.indexOf(e);
			}

			face.checkFaceIntegrity();
		}

		return split;
	}

	/**
	 * Ensures that the edges that used to be adjacent to face f are now
	 * adjacent to f2. Used when breaking / merging faces.
	 * 
	 * @param part
	 */
	private void fixEdgeFaceMap(Planarization pln, Face f, Iterable<PlanarizationEdge> movedFace, Face f2, PlanarizationEdge part) {
		for (PlanarizationEdge edge : movedFace) {
			if (edge != part) {
				List<Face> faces = pln.getEdgeFaceMap().get(edge);
				faces.remove(f);
				faces.add(f2);
				if (faces.size() != 2) {
					throw new LogicException("Should be exactly 2 faces for each edge" + edge);
				}
			}
		}
	}
	
	public void fixVertexFaceMap(Planarization pln, Face a, Iterable<Vertex> toConsider, boolean faceIsDeleted) {
		for (Vertex vertex : toConsider) {
			List<Face> faces = pln.getVertexFaceMap().get(vertex);
			if (faceIsDeleted) {
				faces.remove(a);
			} else if (a.contains(vertex)) {
				if (!faces.contains(a)) {
					faces.add(a);
				}
			} else {
				if (faces.contains(a)) {
					faces.remove(a);
				}
			}
		}
	}
	
	public void updateVertexFaceMap(Planarization pln, Vertex v) {
		List<Face> faces = pln.getVertexFaceMap().get(v);
		for (Iterator<Face> iterator = faces.iterator(); iterator.hasNext();) {
			Face face = (Face) iterator.next();
			if (!face.contains(v)) {
				iterator.remove();
			}
		}
		
		if (faces.size()==0) {
			pln.getVertexFaceMap().remove(v);
		}
	}
	

	/**
	 * This is used when a temporary edge is removed, leaving behind 'island'
	 * vertices within a face. This creates a new outer face containing those
	 * vertices.
	 */
	private void splitFaces(PlanarizationEdge toRemove, Planarization pln) {
		List<Face> faces = pln.getEdgeFaceMap().get(toRemove);
		Face original = faces.get(0);
		Face newFace = original.split(toRemove);
		Vertex from = toRemove.getFrom();
		Vertex to = toRemove.getTo();

		((VertexEdgeOrdering)pln.getEdgeOrderings().get(from)).remove(toRemove);
		((VertexEdgeOrdering)pln.getEdgeOrderings().get(to)).remove(toRemove);
		
		from.removeEdge(toRemove);
		to.removeEdge(toRemove);
		
		pln.getEdgeFaceMap().remove(toRemove);
		fixEdgeFaceMap(pln, original, newFace.edgeIterator(), newFace, null);
		
		// make sure the faces all refer to each other 
		Collection<Face> containedFaces = original.getContainedFaces();
		newFace.getContainedFaces().addAll(containedFaces);
		for (Face face : containedFaces) {
			face.getContainedFaces().add(newFace);
		}
		
		original.getContainedFaces().add(newFace);
		newFace.getContainedFaces().add(original);
		
		pln.getVertexFaceMap().get(from).add(newFace);
		pln.getVertexFaceMap().get(to).add(newFace);
		
		newFace.setPartOf(original.getPartOf());
		
		log.send(log.go() ? null : "Removed" + toRemove + " splitting into " + original.getId() + " and " + newFace.getId());
		log.send(log.go() ? null : "Original:" + original);
		log.send(log.go() ? null : "NewFace:" + newFace);
		
		fixVertexFaceMap(pln, original, original.cornerIterator(), false);
		fixVertexFaceMap(pln, original, newFace.cornerIterator(), false);
		fixVertexFaceMap(pln, newFace, original.cornerIterator(), false);
		fixVertexFaceMap(pln, newFace, newFace.cornerIterator(), false);
		

	}

	/**
	 * Removes the edge from the planarization, preserving the new state of the
	 * remaining faces, whatever that may be.
	 */
	public void removeEdge(PlanarizationEdge toRemove, Planarization pln) {
		List<Face> faces = pln.getEdgeFaceMap().get(toRemove);
		
		Vertex from = toRemove.getFrom();
		Vertex to = toRemove.getTo();
		
		if (toRemove instanceof BiDirectionalPlanarizationEdge) {
			DiagramElement underlying = ((BiDirectionalPlanarizationEdge) toRemove).getOriginalUnderlying();
			if (underlying != null) {
				EdgeMapping el = pln.getEdgeMappings().get(underlying);
				el.remove(toRemove);
				log.send("Route for "+underlying+" is now ",el.getEdges());
			}
		}
		
		if (faces.get(0) == faces.get(1)) {
			splitFaces(toRemove, pln);
		} else {
			mergeFace(toRemove, pln);
		}
		
		checkRemoveVertex(pln, from);
		checkRemoveVertex(pln, to);
	}

	/**
	 * Merges 2 faces together by removing toRemove.
	 */
	private void mergeFace(PlanarizationEdge toRemove, Planarization pln) {
		List<Face> faces = pln.getEdgeFaceMap().get(toRemove);

		List<Vertex> newCorners = new ArrayList<Vertex>();
		List<PlanarizationEdge> newBoundary = new ArrayList<PlanarizationEdge>();

		int face = 0;
		// this is the number of vertices that should be in the merged face
		int vertexCount = faces.get(0).vertexCount() + faces.get(1).vertexCount() - 2;
		int vertexNo = 0;

		do {
			Face currentFace = faces.get(face);
			Vertex currentVertex = currentFace.getCorner(vertexNo);
			PlanarizationEdge boundEdge = currentFace.getBoundary(vertexNo);
			if (boundEdge != toRemove) {
				newCorners.add(currentVertex);
				newBoundary.add(boundEdge);
			} else {
				// move onto the other face
				face = face == 0 ? 1 : 0;
				currentFace = faces.get(face);
				vertexNo = currentFace.indexOf(toRemove.otherEnd(currentVertex), toRemove);
			}
			vertexNo = (vertexNo + 1) % currentFace.vertexCount();
		} while (newCorners.size() < vertexCount);

		Face a = faces.get(0);
		Face b = faces.get(1);	// removing this one
		
		a.reset(newBoundary, newCorners);
		pln.getFaces().remove(b);
		
		if (a.getPartOf() == null) {
			a.setPartOf(b.getPartOf());
		} else {
			if ((b.getPartOf() != null) && ((b.getPartOf() != a.getPartOf()))) { 
				throw new LogicException("PartOf set wrongly");
			}
		} 


		// move all references from b to a
		fixEdgeFaceMap(pln, b, b.edgeIterator(), a, null);
		pln.getEdgeFaceMap().remove(toRemove);
		
		// fix vertex maps
		fixVertexFaceMap(pln, a, b.cornerIterator(), false);
		fixVertexFaceMap(pln, a, a.cornerIterator(), false);
		fixVertexFaceMap(pln, b, b.cornerIterator(), true);
		fixVertexFaceMap(pln, b, a.cornerIterator(), true);

		// remove the edge from the planarization
		VertexEdgeOrdering set1 = (VertexEdgeOrdering) pln.getEdgeOrderings().get(toRemove.getFrom());
		set1.remove(toRemove);
		VertexEdgeOrdering set2 = (VertexEdgeOrdering) pln.getEdgeOrderings().get(toRemove.getTo());
		set2.remove(toRemove);
		toRemove.remove();

		// check integrity of created face
		a.checkFaceIntegrity();

		// just tidying up, shouldn't be needed
		pln.removeEdge(toRemove);
		log.send(log.go() ? null : "Removed " + toRemove + " merging " + a.getId() + " and " + b.getId() + " gives " + a.getId()
				+ " with " + a.cornerIterator() + " \n " + a.edgeIterator());
		
		// tidy up face hierarchy
		for (Face inside : b.getContainedFaces()) {
			a.getContainedFaces().add(inside);
			inside.getContainedFaces().add(a);
			inside.getContainedFaces().remove(b);
		}

	}

	private void removeVertex(Vertex toGo, Planarization pln) {
		if (toGo.getEdgeCount() != 2) {
			throw new LogicException("Can't remove a vertex with anything other than 2 edges");
		}

		Iterator<Edge> it = toGo.getEdges().iterator();

		Edge a = it.next();
		Edge b = it.next();
		Vertex farB = b.otherEnd(toGo);
		

		log.send(log.go() ? null : "Removing: " + toGo + "involving " + a + " and " + b);

		boolean loopback = a.meets(farB);
		if (loopback) {
			log.send(log.go() ? null : "Cannot introduce loopback, finishing");
			return;
		}
		
		if (a instanceof ConnectionEdge) {
			Connected otherEnd = b.getFrom() == toGo ? ((ConnectionEdge) b).getToConnected() : ((ConnectionEdge) b).getFromConnected();
			
			if (a.getFrom() == toGo) {
				((ConnectionEdge) a).setFromConnected(otherEnd);
			} else if (a.getTo() == toGo) {
				((ConnectionEdge) a).setToConnected(otherEnd);
			} else {
				throw new LogicException("Couldn't find end");
			}
		}

		if (a.getFrom() == toGo) {
			((PlanarizationEdge) a).setFrom(farB);
		} else {
			((PlanarizationEdge) a).setTo(farB);
		}

		toGo.removeEdge(a);
		toGo.removeEdge(b);
		farB.removeEdge(b);
		farB.addEdge(a);

		log.send(log.go() ? null : "Created: " + a);
		log.send(log.go() ? null : "removed: " + b);

		// remove toGo from the faces
		for (Face f : pln.getVertexFaceMap().get(toGo)) {

			for (int i = 0; i < f.edgeCount(); i++) {
				Vertex v = f.getCorner(i);
				if (v == toGo) {
					f.remove(i);
				}
			}

			// make sure we are always keeping the correct edge
			f.replaceEdge((PlanarizationEdge) b, (PlanarizationEdge) a);

			f.checkFaceIntegrity();
		}
		
		pln.getVertexFaceMap().remove(toGo);

		pln.getEdgeOrderings().remove(toGo);
		pln.getEdgeFaceMap().remove(b);
		
		((PlanarizationEdge) a).getDiagramElements().keySet().stream().forEach(underlying -> {
			EdgeMapping list = pln.getEdgeMappings().get(underlying);
			//log.send("Edge Mapping before: "+list);
			if (list != null) {
				list.remove(b);
				log.send("Route for "+underlying+" is now ",list.getEdges());
			}
		});
		
		// fix up vertex edge ordering - only works if vertex is a connected item
		VertexEdgeOrdering orderingOfTo = (VertexEdgeOrdering) pln.getEdgeOrderings().get(farB);
		orderingOfTo.replace((PlanarizationEdge) b, (PlanarizationEdge) a);
	}

	/**
	 * Splits an edge into two parts, preserving the original intact. First edge
	 * in the array is the from end, second is to end
	 */
	public PlanarizationEdge[] splitEdge(PlanarizationEdge parent, Vertex toIntroduce, Planarization pln) {
		log.send(log.go() ? null : "Splitting: "+parent);
		PlanarizationEdge[] out = parent.split(toIntroduce);
		
		for (DiagramElement de : parent.getDiagramElements().keySet()) {
			EdgeMapping list = pln.getEdgeMappings().get(de);
			if (list != null) {
				list.replace(parent, out[0], out[1]);
			}
		}
		
		
		parent.getFrom().removeEdge(parent);
		parent.getTo().removeEdge(parent);
		pln.getEdgeFaceMap().remove(parent);
		log.send(log.go() ? null : "Made: \n\t"+out[0]+"\n\t"+out[1]);

		return out;
	}

	public void checkRemoveVertex(Planarization pln, Vertex v) {
		if (canBeRemoved(v)) {
			int linkCount = v.getEdgeCount();
			if ((linkCount==2)) {
				Iterator<Edge> edges = v.getEdges().iterator();
				Edge a = edges.next();
				Edge b = edges.next();
				if (a.getDrawDirectionFrom(v) == Direction.reverse(b.getDrawDirectionFrom(v))) {
					removeVertex(v, pln);
				}
			} 
		}
	}

	private boolean canBeRemoved(Vertex v) {
		return v.hasDimension()==false;
	}
	
	public static void setUnderlyingContradiction(BiDirectional<?> c, boolean state) {
		if (c instanceof BiDirectionalPlanarizationEdge) {
			DiagramElement underlying = ((BiDirectionalPlanarizationEdge) c).getOriginalUnderlying();
			if (underlying instanceof Connection) {
				setConnectionContradiction((Connection)underlying, state, true);
			} else {
				throw new LogicException("Wasn't expecting to set contradiction on "+underlying);
			}
		}
	}
	
	public static boolean isUnderlyingContradicting(BiDirectional<?> c2) {
		if (c2 instanceof BiDirectionalPlanarizationEdge) {
			DiagramElement underlying = ((BiDirectionalPlanarizationEdge)c2).getOriginalUnderlying();
			if (underlying instanceof Connection) {
				return isConnectionContradicting((Connection)underlying);
			}
			
			return false;	
		} else {
			return false;
		}
		
	}

	public static boolean isConnectionContradicting(Connection c) {
		RouteRenderingInformation rri = c.getRenderingInformation();
		return rri.isContradicting();
	}
	
	public static boolean isConnectionRendered(Connection c) {
		RouteRenderingInformation rri = c.getRenderingInformation();
		return rri.isRendered();
	}
	
	
	public static void setConnectionContradiction(Connection c, boolean contradicting, boolean rendering) {
		RouteRenderingInformation rri = c.getRenderingInformation();
		if (c.getDrawDirection() != null) {
			rri.setContradicting(contradicting);
		}
		
		rri.setRendered(rendering);
	}
}
