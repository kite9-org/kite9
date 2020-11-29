package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.List;

import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.SimpleNode;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.logging.LogicException;

/**
 * A portion models a part of a face that must contain a certain number of corners. The number of corners is allocated
 * by the constraint system. Portions start and end with constrained edges (i.e. edges where a direction is set), unless
 * they represent a whole face without constrained edges.
 * 
 */
public class PortionNode extends SimpleNode {

	public PortionNode(String id, int supply, Face f, int edgeStartPosition, int edgeEndPosition) {
		super(id, supply, null);
		setType(AbstractFlowOrthogonalizer.PORTION_NODE);
		this.edgeEndPosition = edgeEndPosition;
		this.edgeStartPosition = edgeStartPosition;
		this.face = f;
	}

	private int edgeStartPosition = -1;

	public int getEdgeStartPosition() {
		return edgeStartPosition;
	}

	public int getEdgeEndPosition() {
		return edgeEndPosition;
	}

	private int edgeEndPosition = -1;

	Face face;

	public Face getFace() {
		return face;
	}

	Arc faceArc;

	public Arc getFaceArc() {
		return faceArc;
	}

	public void setFaceArc(Arc a) {
		this.faceArc = a;
	}

	/**
	 * Returns true if the vertex is within the boundary of the face covered by this portion
	 */
	public boolean containsVertexForEdge(Edge e, Vertex v) {
		if (containsInPortion(e)) {
			return true;
		}

		if (edgeStartPosition == edgeEndPosition) {
			// only one edge
			return false;
		}

		if (face.getBoundary(edgeStartPosition) == e) {
			// start edge
			Vertex startVertex = face.getCorner(edgeStartPosition + 1);
			boolean meets = v == startVertex;
			return meets;

		} else if (face.getBoundary(edgeEndPosition) == e) {
			Vertex endVertex = face.getCorner(edgeEndPosition);
			boolean meets = v == endVertex;
			return meets;

		} else {
			throw new LogicException("edge " + e + " not in portion: " + this);
		}

	}

	private boolean containsInPortion(Edge e) {
		if (edgeStartPosition == -1) {
			return true;
		} else {
			List<Integer> ep = face.indexOf(e);
			if (ep.size()!=1) {
				throw new LogicException("Was expecting edge to only appear once in face: "+e);
			}
			int first = ep.get(0);
			if (first > edgeStartPosition) {
				if (edgeEndPosition < edgeStartPosition) {
					return true;
				} else if (first < edgeEndPosition) {
					return true;
				} 
			}
			
			return false;
		}
	}

	public boolean containsFacePart(int i) {
		if (edgeStartPosition == -1)
			return true;

		if (i >= edgeStartPosition) {
			if (i <= edgeEndPosition) {
				return true;
			} else if (edgeEndPosition < edgeStartPosition) {
				return true;
			}
		} else {
			if (edgeEndPosition < edgeStartPosition) {
				if (i <= edgeEndPosition) {
					return true;
				}
			}
		}

		return false;
	}

	public Edge getConstrainedEdgeStart() {
		if (edgeStartPosition == -1)
			return null;
		return face.getBoundary(edgeStartPosition);
	}

	public Edge getConstrainedEdgeEnd() {
		if (edgeEndPosition == -1)
			return null;
		return face.getBoundary(edgeEndPosition);
	}

	public Edge getEdge(int i) {
		if (edgeStartPosition != -1) {
			return face.getBoundary(i + edgeStartPosition);
		} else {
			return face.getBoundary(i);
		}
	}

}