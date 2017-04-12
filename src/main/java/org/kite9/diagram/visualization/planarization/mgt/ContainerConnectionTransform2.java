package org.kite9.diagram.visualization.planarization.mgt;


import java.util.List;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Edges connecting to a container can connect to either a container corner vertex or
 * a container side vertex (or a vertex within the container == to be deprecated)
 * 
 * Where multiple edges connect to a container vertex, these need to be split out so that they connect 
 * to individual vertices around the perimeter of the container.  This means we have to add some new perimeter 
 * vertices and move the edges onto those.
 * 
 * This transform is necessary when you allow connections to containers.  This performs the transform on DIRECTED, NOT CONTRADICTING
 * edges only.  ContainerCornerVertexArranger is used to perform the transform on undirected edges, as these 
 * need to be transformed after orthogonalization.
 * 
 * @author robmoffat
 * 
 */
public class ContainerConnectionTransform2 implements PlanarizationTransform, Logable {
		
	private Kite9Log log = new Kite9Log(this);

	public ContainerConnectionTransform2(ElementMapper elementMapper) {
	}

	public void transform(Planarization pln) {
		createContainerEdgeVertices(pln);
	}

	Tools t = new Tools();	

	private void createContainerEdgeVertices(Planarization pln) {
		int number = 0;
		for (Vertex v : ((MGTPlanarization) pln).getVertexOrder()) {
			if ((v instanceof MultiCornerVertex) && (sideOrd((MultiCornerVertex)v)) && (v.getEdgeCount() > 3)) {
				Direction edgeDirectionToSplit = getDirectionForSideVertex((MultiCornerVertex)v);
				if (edgeDirectionToSplit != null) {
					Direction startContainerEdgeDirection = Direction.rotateAntiClockwise(edgeDirectionToSplit);
					number = splitEdgesGoing(edgeDirectionToSplit, startContainerEdgeDirection, true, (MultiCornerVertex) v, pln, number);
				}
				
			} else if ((v instanceof MultiCornerVertex) && (v.getEdgeCount() > 3)) {
				MultiCornerVertex cv = (MultiCornerVertex) v;
				boolean ymin = MultiCornerVertex.isMin(cv.getYOrdinal());
				boolean xmin = MultiCornerVertex.isMin(cv.getXOrdinal());
				boolean ymax = MultiCornerVertex.isMax(cv.getYOrdinal());
				boolean xmax = MultiCornerVertex.isMax(cv.getXOrdinal());
								
				if (MultiCornerVertex.isMin(cv.getXOrdinal()) && (cornerOrd(cv.getYOrdinal()))) {
					number = splitEdgesGoing(Direction.LEFT, ymin ? Direction.DOWN : Direction.UP, 
							ymin, cv, pln, number);
				}
				
				if ((cv.getXOrdinal() == BigFraction.ONE) && (cornerOrd(cv.getYOrdinal()))) {
					number = splitEdgesGoing(Direction.RIGHT, ymin ? Direction.DOWN : Direction.UP, 
							ymax, cv, pln, number);
				}
				
				if ((cv.getYOrdinal() == BigFraction.ZERO) && (cornerOrd(cv.getXOrdinal()))) {
					number = splitEdgesGoing(Direction.UP, xmin ? Direction.RIGHT : Direction.LEFT, 
							xmax, cv, pln, number);
				}
				
				if ((cv.getYOrdinal() == BigFraction.ONE) && (cornerOrd(cv.getXOrdinal()))) {
					number = splitEdgesGoing(Direction.DOWN, xmin ? Direction.RIGHT : Direction.LEFT, 
							xmin, cv, pln, number);
				}
			}
		}
	}

	private Direction getDirectionForSideVertex(MultiCornerVertex v) {
		BigFraction xOrd = v.getXOrdinal();
		if (MultiCornerVertex.isMin(xOrd)) {
			return Direction.LEFT;
		} else if (MultiCornerVertex.isMax(xOrd)) {
			return Direction.RIGHT;
		}
		
		BigFraction yOrd = v.getYOrdinal();
		if (MultiCornerVertex.isMin(yOrd)) {
			return Direction.UP;
		} else if (MultiCornerVertex.isMax(yOrd)) {
			return Direction.DOWN;
		}
		
		return null; 	// container vertex embedded within a grid
	}

	private boolean sideOrd(MultiCornerVertex v) {
		return (!cornerOrd(v.getXOrdinal())) || (!cornerOrd(v.getYOrdinal()));
	}

	private boolean cornerOrd(BigFraction ord) {
		return MultiCornerVertex.isMin(ord) || MultiCornerVertex.isMax(ord);
	}

	private int splitEdgesGoing(Direction edgeDirectionToSplit, Direction startContainerEdgeDirection, boolean turnClockwise, MultiCornerVertex v, Planarization pln, int n) {
		// find out the starting point for the turn, and how many edges go in the direction we want to split
		log.send("Fixing edges around vertex: "+v+" going "+edgeDirectionToSplit);

		VertexEdgeOrdering eo = (VertexEdgeOrdering) pln.getEdgeOrderings().get(v);
		List<PlanarizationEdge> originalOrder = eo.getEdgesAsList();
		
		int startPoint = 0;
		int edgesRequiringSplit = 0;
		int done = 0;
		for (int i = 0; done < originalOrder.size(); i=i+(turnClockwise ? 1 : -1)) {
			done++;
			PlanarizationEdge edge = getRot(originalOrder, i);
			if ((edge.getOriginalUnderlying() == v.getOriginalUnderlying()) && (getUsedEdgeDirection(v, edge) == startContainerEdgeDirection)) {
				startPoint = i;
			} else if (getUsedEdgeDirection(v, edge) == edgeDirectionToSplit) {
				edgesRequiringSplit++;
			}
		}
		
		PlanarizationEdge receivingEdge = getRot(originalOrder, startPoint);
		
		if (edgesRequiringSplit > 0) {
			int edgesDoneSplit = 0;
			for (int i = startPoint +(turnClockwise ? 1 : -1); edgesDoneSplit < edgesRequiringSplit; i=i+(turnClockwise ? 1 : -1)) {
				PlanarizationEdge edgeMoving = getRot(originalOrder, i);
				if (getUsedEdgeDirection(v, edgeMoving) == edgeDirectionToSplit) { 
					edgesDoneSplit ++;
				}

				receivingEdge = splitEdgeFromVertex(v.getOriginalUnderlying().getID()+"-"+edgeDirectionToSplit+edgesDoneSplit+n++, 
					v.getOriginalUnderlying(), v, pln, receivingEdge, edgeMoving, getRot(originalOrder, i+(turnClockwise ? 1 : -1)), turnClockwise);
				
			}
			
			log.send("Changed vertex "+v+" order now: ", eo.getEdgesAsList());
		}
		
		return n;
	}

	private Direction getUsedEdgeDirection(MultiCornerVertex v, Edge edge) {
		if (edge instanceof ConnectionEdge) {
			Connection und = ((ConnectionEdge)edge).getOriginalUnderlying();
			RouteRenderingInformation rri = (RouteRenderingInformation) und.getRenderingInformation();
			if (rri.isContradicting()) {
				return null;
			}
		} 
		
		return edge.getDrawDirectionFrom(v);
	}

	private PlanarizationEdge getRot(List<PlanarizationEdge> originalOrder, int i) {
		return originalOrder.get((i + originalOrder.size() + originalOrder.size()) % originalOrder.size());
	}

	/**
	 * Splits the receivingEdge with a new vertex, and moves "mover" onto it.  "after" is the edge following mover in the current ordering
	 */
	private PlanarizationEdge splitEdgeFromVertex(String vertexName, DiagramElement c, Vertex v, Planarization pln, PlanarizationEdge receivingEdge, PlanarizationEdge mover, PlanarizationEdge after, boolean clockwise) {
		// ok, splitting time - create a new vertex for the 'next' edge
		Vertex newVertex = t.breakEdge((PlanarizationEdge) receivingEdge, pln, vertexName, c);

		// need to move next to the new vertex
		if (mover.getFrom() == v) {
			mover.setFrom(newVertex);
			newVertex.addEdge(mover);
			v.removeEdge(mover);
		} else if (mover.getTo() == v) {
			mover.setTo(newVertex);
			newVertex.addEdge(mover);
			v.removeEdge(mover);
		} else {
			throw new LogicException("Could not move next");
		}

		// fix the faces
		PlanarizationEdge newArc = getNewArc(newVertex, v);
		PlanarizationEdge oldArc = getOldArc(newVertex, newArc);
		List<Face> faces = pln.getEdgeFaceMap().get(mover);
		Face onFace = faces.get(0).contains(newArc) ? faces.get(0) : faces.get(1);
		Face offFace = (!faces.get(0).contains(newArc)) ? faces.get(0) : faces.get(1);
		List<PlanarizationEdge> onList = onFace.getEdgesCopy();
		List<PlanarizationEdge> offList = (offFace!=onFace) ? offFace.getEdgesCopy() : onList;

		onList.remove(newArc);
		insertBetween(offList, newArc, newVertex, mover, after, v, clockwise);

		onFace.reset(onList);
		if (offFace!=onFace) {
			offFace.reset(offList);
		}

		// fix edge face map.  new arc should not meet face shared between old arc and next
		List<Face> newArcFaces = pln.getEdgeFaceMap().get(newArc);
		boolean ok = newArcFaces.remove(onFace);
		if (!ok) {
			throw new LogicException("Setting up edge face map didn't work");
		}
		newArcFaces.add(offFace);
		
		// fix vertex face map
		t.updateVertexFaceMap(pln, v);
		t.fixVertexFaceMap(pln, onFace, onFace.cornerIterator(), false);
		t.fixVertexFaceMap(pln, offFace, offFace.cornerIterator(), false);
		
		
		// fix the vertex edge ordering for v
		VertexEdgeOrdering newOrder = (VertexEdgeOrdering) pln.getEdgeOrderings().get(v);
		newOrder.remove(mover);
		newOrder.replace(receivingEdge, newArc.meets(v) ? newArc : oldArc);

		// fix vertex edge ordering for newVertex
		List<PlanarizationEdge> otherEndOrder = pln.getEdgeOrderings().get(newVertex).getEdgesAsList();
		int oldArcI = otherEndOrder.indexOf(clockwise ? oldArc : newArc);
		if (oldArcI==-1) {
			throw new LogicException("Can't find old arc "+oldArc);
		} else {
			otherEndOrder.add(oldArcI + 1 % 3, mover);
		}
		
		log.send("Order around new vertex "+newVertex, otherEndOrder);
		
		// move on
		receivingEdge = newArc;
		return receivingEdge;
	}

	private void insertBetween(List<PlanarizationEdge> offFace, PlanarizationEdge newArc, Vertex newVertex, PlanarizationEdge incoming, PlanarizationEdge outgoing, Vertex v, boolean clockwise) {
		for (int i = 0; i < offFace.size(); i++) {
			Edge before = offFace.get((i + offFace.size()) %offFace.size());
			int afteri = (i + offFace.size() + (clockwise ? -1: 1)) % offFace.size();
			Edge after = getRot(offFace, afteri);
			if (((before == incoming) && (after == outgoing))) {
				if (clockwise) {
					offFace.add(i, newArc);
				} else {
					offFace.add(afteri, newArc);
				}
				return;
			}
		}

		throw new LogicException("Couldn't find point to insert in " + offFace);
	}

	private PlanarizationEdge getNewArc(Vertex newVertex, Vertex v) {
		for (Edge e : newVertex.getEdges()) {
			if (e.meets(v)) {
				return (PlanarizationEdge) e;
			}
		}

		throw new LogicException("Could not find edge meeting " + v);
	}

	private PlanarizationEdge getOldArc(Vertex newVertex, Edge newArc) {
		for (Edge e : newVertex.getEdges()) {
			if (e != newArc) {
				return (PlanarizationEdge) e;
			}
		}

		throw new LogicException("Could not old arc");
	}
	
	public String getPrefix() {
		return "CET ";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
