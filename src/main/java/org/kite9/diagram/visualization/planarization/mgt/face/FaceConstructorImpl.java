package org.kite9.diagram.visualization.planarization.mgt.face;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.NoElementPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;

/**
 * Turns the vertex-order planarization into a number of faces.  This is done by first joining everything in the diagram together,
 * then walking round the faces in an anti-clockwise direction.  Then, removing the temporary edges inserted to make the diagram completely
 * connected.
 * 
 * @author robmoffat
 *
 */
public class FaceConstructorImpl implements FaceConstructor {
	

	public class TemporaryEdge extends AbstractPlanarizationEdge implements NoElementPlanarizationEdge {
		
		public TemporaryEdge(Vertex from, Vertex to) {
			super(from, to, null);
		}

		public void remove() {
			getFrom().removeEdge(this);
			getTo().removeEdge(this);
		}

		public Vertex getLabelEnd() {
			return null;
		}

		@Override
		public int getCrossCost() {
			return 0; // no cost for crossing temporaries
		}

		@Override
		public RemovalType removeBeforeOrthogonalization() {
			return RemovalType.YES;
		}

		private boolean layoutEnforcing;

		public boolean isLayoutEnforcing() {
			return layoutEnforcing;
		}

		public void setLayoutEnforcing(boolean le) {
			this.layoutEnforcing = le;
		}

		@Override
		public PlanarizationEdge[] split(Vertex toIntroduce) {
			PlanarizationEdge[] out = new PlanarizationEdge[2];
			out[0] = new TemporaryEdge(getFrom(), toIntroduce);
			out[1] = new TemporaryEdge(toIntroduce, getTo());
			return out;
		}

		@Override
		public int getLengthCost() {
			return 0;
		}

		@Override
		public boolean isStraightInPlanarization() {
			return false;
		}

		@Override
		public boolean isPartOf(DiagramElement de) {
			return false;
		}

		@Override
		public Map<DiagramElement, Direction> getDiagramElements() {
			return Collections.emptyMap();
		}
	}

	public void createFaces(MGTPlanarization pl) {
		introduceTemporaryEdges((MGTPlanarization)pl);
		
		// System.out.println(pl);
		
		
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
		
		removeTemporaries((MGTPlanarization)pl);
	}


	protected void introduceTemporaryEdges(MGTPlanarization p) {
		int totalLength = p.getVertexOrder().size();

		for (int pos = 0; pos < totalLength; pos++) {
			if (pos < totalLength - 1) {
				Vertex v1 = p.getVertexOrder().get(pos);
				Vertex v2 = p.getVertexOrder().get(pos + 1);

				if (!v1.isLinkedDirectlyTo(v2)) {
					createTemporaryEdge(p, v1, v2);
				}
			}
		}
	}

	protected PlanarizationEdge createTemporaryEdge(MGTPlanarization p, Vertex from, Vertex to) {
		PlanarizationEdge e = new TemporaryEdge(from, to);
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

	public PlanarizationEdge getLeftEdge(PlanarizationEdge incident, Vertex v, Planarization pl) {
		List<PlanarizationEdge> ordering = getEdgeOrdering(v, pl);

		int startIndex = ordering.indexOf(incident);
		int index = startIndex;
		PlanarizationEdge out = null;
		// do {
		if (index == 0) {
			index = ordering.size() - 1;
		} else {
			index = index - 1;
		}
		// if (index==startIndex)
		// throw new RuntimeException("Can't process this - no edges out!");
		out = ordering.get(index);
		// } while ((pl.edgeFaceMap.get(out)!=null) && (pl.edgeFaceMap.get(out).size()==2));

		return out;
	}

	private List<PlanarizationEdge> getEdgeOrdering(Vertex v, Planarization pl) {
		List<PlanarizationEdge> ordering = pl.getEdgeOrderings().get(v).getEdgesAsList();
		return ordering;
	}

	public void removeTemporaries(MGTPlanarization p) {

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
	protected void removeTemporaries(Set<PlanarizationEdge> toRemove, Planarization p) {
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
	

}
