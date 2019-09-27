package org.kite9.diagram.visualization.planarization.mgt.face;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Turns the vertex-order planarization into a number of faces.  This is done by first joining everything in the diagram together, using temporary edges
 * then walking round the faces in an anti-clockwise direction.  
 * 
 * At this point, each face will have a temporary edge.  We can use this fact to determine which Rectangular each face is partOf, and set that.
 * 
 * Then, removing the temporary edges inserted to make the diagram completely connected.
 * 
 * @author robmoffat
 *
 */
public class FaceConstructorImpl implements FaceConstructor, Logable {

	private Kite9Log log = new Kite9Log(this);

	public void createFaces(MGTPlanarization pl) {
		List<TemporaryEdge> temps = introduceTemporaryEdges(pl);
		traceFaces(pl);
		assignRectangulars1(temps, pl);
		removeTemporaries(pl);
		assignRectangulars2(pl);
	}

	private void traceFaces(MGTPlanarization pl) {
		// walk through nodes in turn
		for (Vertex v : pl.getVertexOrder()) {
			for (PlanarizationEdge e : getEdgeOrdering(v, pl)) {
				// edges can only contribute to two faces, at most.
				// although they can contribute to the same face twice
				List<Face> map = pl.getEdgeFaceMap().get(e);
				if (map == null) {
					tracePath(v, e, pl);
				} else if (map.get(0) == null) {
					tracePath(e.getFrom(), e, pl);
				} else if (map.get(1) == null) {
					tracePath(e.getTo(), e, pl);
				}
			}
		}

		for (Face face : pl.getFaces()) {
			face.checkFaceIntegrity();
		}
	}



	/**
	 * Works out the rectangular diagram element containing this face for *most* faces.
	 * 
	 * This doesn't work if the face is completely surrounded by connections, we deal with that in assignRectangulars2
	 */
	private void assignRectangulars1(List<TemporaryEdge> temps, MGTPlanarization pl) {
		for (TemporaryEdge temporaryEdge : temps) {
			List<Face> faces = pl.getEdgeFaceMap().get(temporaryEdge);
			for (Face f : faces) {
				if (f.getPartOf() == null) {
					Rectangular r = determineInsideElementFromTemporaryEdge(f, temporaryEdge, pl);
					if (r != null) {
						log.send("1. Face  " + f.id +" "+ f.cornerIterator()+ " is part of " + r);
						f.setPartOf(r);
					}
				}
			}	
		}
	}
	
	
	private void assignRectangulars2(MGTPlanarization pl) {	
		for (Face f : pl.getFaces()) {
			identifyRectangular(f, new HashSet<>(), pl);
		}
	}

	private Rectangular identifyRectangular(Face f, Set<Face> visited, MGTPlanarization pl) {
		if (f.getPartOf() == null) {
			visited.add(f);
			for (Edge e : f.edgeIterator()) {
				if (e instanceof BiDirectionalPlanarizationEdge) {
					// the rectangular is the same on both sides
					List<Face> faces = pl.getEdgeFaceMap().get(e);
					for (Face face2 : faces) {
						if (!visited.contains(face2)) {
							Rectangular r = identifyRectangular(face2, visited, pl);
							log.send("2. Face " + f.id +" "+ f.cornerIterator()+ " is part of " + r);
							f.setPartOf(r);
							return r;
						}
					}
				}
			}
			
			return null;
		} else {
			return f.getPartOf();
		}
	}

	private Rectangular determineInsideElementFromTemporaryEdge(Face face, TemporaryEdge leave, MGTPlanarization pl) {
		Vertex from = leave.getFrom();
		Vertex to = leave.getTo();
		
		if (face.indexOf(from, leave) > -1) {
			// face is below the planarization line
			int leaveStart = face.indexOf(from, leave);
			Edge next = face.getBoundary(leaveStart-1);	
			Rectangular out = null;

			if (next instanceof BorderEdge) {
				BorderEdge be = (BorderEdge) next;
				
				switch (next.getDrawDirectionFrom(from)) {
				case UP:
					out = safeGetDiagramElement(be, Direction.RIGHT);
					break;
				case DOWN:
					out = safeGetDiagramElement(be, Direction.LEFT);
					break;
				case LEFT:
					out = safeGetDiagramElement(be, Direction.UP);
					break;
				case RIGHT:
					out = safeGetDiagramElement(be, Direction.DOWN);
					break;
					
				default:
					throw new LogicException();
				}
			}
			
			leave.setBelow(out);
			return out;
		} else if (face.indexOf(to, leave) > -1) {
			// above the planarization line
			int leaveStart = face.indexOf(to, leave);
			Edge next = face.getBoundary(leaveStart+1);	
			Rectangular out = null;
			if (next instanceof BorderEdge) {
				BorderEdge be = (BorderEdge) next;
				
				switch (next.getDrawDirectionFrom(from)) {
				case UP:
					out = safeGetDiagramElement(be, Direction.LEFT);
					break;
				case DOWN:
					out = safeGetDiagramElement(be, Direction.RIGHT);
					break;
				case LEFT:
					out = safeGetDiagramElement(be, Direction.DOWN);
					break;
				case RIGHT:
					out = safeGetDiagramElement(be, Direction.UP);
					break;
					
				default:
					throw new LogicException();
				}
			}
			
			leave.setAbove(out);
			return out;
		} else {
			throw new LogicException();
		}
	}

	private Rectangular safeGetDiagramElement(BorderEdge be, Direction d) {
		DiagramElement de = be.getElementForSide(d);
		if (de == null) {
			de = be.getElementForSide(Direction.reverse(d)).getParent();
		}
		
		return (Rectangular)de;
	}

	protected List<TemporaryEdge> introduceTemporaryEdges(MGTPlanarization p) {
		int totalLength = p.getVertexOrder().size();
		List<TemporaryEdge> out = new ArrayList<>(totalLength+1);

		for (int pos = 0; pos < totalLength; pos++) {
			if (pos < totalLength - 1) {
				Vertex v1 = p.getVertexOrder().get(pos);
				Vertex v2 = p.getVertexOrder().get(pos + 1);

				if (!v1.isLinkedDirectlyTo(v2)) {
					out.add(createTemporaryEdge(p, v1, v2));
				}
			}
		}
		
		return out;
	}

	protected TemporaryEdge createTemporaryEdge(MGTPlanarization p, Vertex from, Vertex to) {
		TemporaryEdge e = new TemporaryEdge(from, to);
		p.addEdge(e, true, null);
		return e;
	}

	private Face tracePath(Vertex v, PlanarizationEdge e, Planarization pl) {
		Face f = pl.createFace();
		//System.out.println("Creating face "+f);
		PlanarizationEdge startEdge = e;
		Vertex startVertex = v;
		do {
			addToFaceMap(v, e, f, pl);
			f.add(v, e);
			//System.out.println("adding " + e);
			v = e.otherEnd(v);
			e = getLeftEdge(e, v, pl);
		} while ((e != startEdge) || (v != startVertex));

		return f;
	}

	private void addToFaceMap(Vertex from, Edge e, Face f, Planarization pln) {
		// edge face map
		List<Face> faces = pln.getEdgeFaceMap().get(e);
		if (faces == null) {
			faces = new ArrayList<Face>(2);
			pln.getEdgeFaceMap().put(e, faces);
			faces.add(null);
			faces.add(null);
		}

		if (from == e.getFrom()) {
			faces.set(0, f);
		} else if (from == e.getTo()) {
			faces.set(1, f);
		}
		
		// vertex face map
		faces = pln.getVertexFaceMap().get(from);
		if (faces == null) {
			faces = new LinkedList<Face>();
			pln.getVertexFaceMap().put(from, faces);
		}
		faces.add(f);
	}

	private PlanarizationEdge getLeftEdge(PlanarizationEdge incident, Vertex v, Planarization pl) {
		List<PlanarizationEdge> ordering = getEdgeOrdering(v, pl);

		int startIndex = ordering.indexOf(incident);
		int index = startIndex;
		if (index == 0) {
			index = ordering.size() - 1;
		} else {
			index = index - 1;
		}
	
		PlanarizationEdge out = null;
		out = ordering.get(index);
		return out;
	}

	private List<PlanarizationEdge> getEdgeOrdering(Vertex v, Planarization pl) {
		List<PlanarizationEdge> ordering = pl.getEdgeOrderings().get(v).getEdgesAsList();
		return ordering;
	}

	private void removeTemporaries(MGTPlanarization p) {

		Set<PlanarizationEdge> toRemove = new DetHashSet<PlanarizationEdge>();
		for (Vertex v : p.getVertexOrder()) {
			traverseAllLinks(v, toRemove);
		}

		if (toRemove.size() > 0) {
			removeTemporaries(toRemove, p);
		}

	}

	Tools t = new Tools();

	/**
	 * Adds extra logic to say only remove the temporaries where they are not providing direction information for orth.
	 * If they do, they must remain.
	 */
	private void removeTemporaries(Set<PlanarizationEdge> toRemove, Planarization p) {
		for (PlanarizationEdge temporaryEdge : toRemove) {
			if (temporaryEdge.getDrawDirection() == null) {
				t.removeEdge(temporaryEdge, p);
			}
		}
	}

	private void traverseAllLinks(Vertex vertex, Set<PlanarizationEdge> toRemove) {
		for (Edge edge : vertex.getEdges()) {
			if ((edge instanceof PlanarizationEdge)
					&& ((PlanarizationEdge) edge).removeBeforeOrthogonalization() == RemovalType.YES) {
				//System.out.println("Removing edge: "+edge);
				toRemove.add((PlanarizationEdge) edge);
			}
		}
	}



	@Override
	public String getPrefix() {
		return "FC  ";
	}



	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	

}
