package org.kite9.diagram.visualization.planarization.mgt;

import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.LOWEST_ORD;
import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.HIGHEST_ORD;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.primitives.Connection;
import org.kite9.diagram.primitives.Container;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mapping.ConnectionEdge;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Edges connecting to a container either connect to either the container start vertex or the container end vertex,
 * a container side vertex (or a vertex within the container == to be deprecated)
 * 
 * Where multiple edges connect to a container vertex, these need to be split out so that they connect 
 * to individual vertices around the perimeter of the container.  This means we have to add some new perimeter 
 * vertices and move the edges onto those.
 * 
 * This transform is necessary when you allow connections to containers.  This performs the transform on DIRECTED
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
			if ((v instanceof ContainerVertex) && (sideOrd((ContainerVertex)v)) && (v.getEdgeCount() > 3)) {
				Direction edgeDirectionToSplit = getDirectionForSideVertex((ContainerVertex)v);
				Direction startContainerEdgeDirection = Direction.rotateAntiClockwise(edgeDirectionToSplit);
				
				number = splitEdgesGoing(edgeDirectionToSplit, startContainerEdgeDirection, true, (ContainerVertex) v, pln, number);
				
				
			} else if ((v instanceof ContainerVertex) && (v.getEdgeCount() > 2)) {
				ContainerVertex cv = (ContainerVertex) v;
								
				if ((cv.getXOrdinal() == LOWEST_ORD) && (cornerOrd(cv.getYOrdinal()))) {
					number = splitEdgesGoing(Direction.LEFT, cv.getYOrdinal()==LOWEST_ORD ? Direction.DOWN : Direction.UP, 
							cv.getYOrdinal()==LOWEST_ORD, cv, pln, number);
				}
				
				if ((cv.getXOrdinal() == HIGHEST_ORD) && (cornerOrd(cv.getYOrdinal()))) {
					number = splitEdgesGoing(Direction.RIGHT, cv.getYOrdinal()==LOWEST_ORD ? Direction.DOWN : Direction.UP, 
							cv.getYOrdinal()==HIGHEST_ORD, cv, pln, number);
				}
				
				if ((cv.getYOrdinal() == LOWEST_ORD) && (cornerOrd(cv.getXOrdinal()))) {
					number = splitEdgesGoing(Direction.UP, cv.getXOrdinal() == LOWEST_ORD ? Direction.RIGHT : Direction.LEFT, 
							cv.getXOrdinal() == HIGHEST_ORD, cv, pln, number);
				}
				
				if ((cv.getYOrdinal() == HIGHEST_ORD) && (cornerOrd(cv.getXOrdinal()))) {
					number = splitEdgesGoing(Direction.DOWN, cv.getXOrdinal() == LOWEST_ORD ? Direction.RIGHT : Direction.LEFT, 
							cv.getXOrdinal() == LOWEST_ORD, cv, pln, number);
				}
			}
		}
	}

	private Direction getDirectionForSideVertex(ContainerVertex v) {
		int xOrd = v.getXOrdinal();
		if (xOrd == ContainerVertex.LOWEST_ORD) {
			return Direction.LEFT;
		} else if (xOrd == HIGHEST_ORD) {
			return Direction.RIGHT;
		}
		
		int yOrd = v.getYOrdinal();
		if (yOrd == ContainerVertex.LOWEST_ORD) {
			return Direction.UP;
		} else if (yOrd == HIGHEST_ORD) {
			return Direction.DOWN;
		}
		
		throw new LogicException("Was expecting a Vertex Container on a side: "+v);
	}

	private boolean sideOrd(ContainerVertex v) {
		return (!cornerOrd(v.getXOrdinal())) || (!cornerOrd(v.getYOrdinal()));
	}

	private boolean cornerOrd(int ord) {
		return (ord == HIGHEST_ORD) || (ord == LOWEST_ORD);
	}

	private int splitEdgesGoing(Direction edgeDirectionToSplit, Direction startContainerEdgeDirection, boolean turnClockwise, ContainerVertex v, Planarization pln, int n) {
		// find out the starting point for the turn, and how many edges go in the direction we want to split
		log.send("Fixing edges around vertex: "+v+" going "+edgeDirectionToSplit);

		VertexEdgeOrdering eo = (VertexEdgeOrdering) pln.getEdgeOrderings().get(v);
		List<Edge> originalOrder = eo.getEdgesAsList();
		
		int startPoint = 0;
		int edgesRequiringSplit = 0;
		int done = 0;
		for (int i = 0; done < originalOrder.size(); i=i+(turnClockwise ? 1 : -1)) {
			done++;
			Edge edge = getRot(originalOrder, i);
			if ((edge.getOriginalUnderlying() == v.getOriginalUnderlying()) && (getUsedEdgeDirection(v, edge) == startContainerEdgeDirection)) {
				startPoint = i;
			} else if (getUsedEdgeDirection(v, edge) == edgeDirectionToSplit) {
				edgesRequiringSplit++;
			}
		}
		
		Edge receivingEdge = getRot(originalOrder, startPoint);
		
		if (edgesRequiringSplit > 0) {
			List<Edge> newOrder = new ArrayList<Edge>(originalOrder);
			int edgesDoneSplit = 0;
			for (int i = startPoint +(turnClockwise ? 1 : -1); edgesDoneSplit < edgesRequiringSplit; i=i+(turnClockwise ? 1 : -1)) {
				Edge edgeMoving = getRot(originalOrder, i);
				if (getUsedEdgeDirection(v, edgeMoving) == edgeDirectionToSplit) { 
					edgesDoneSplit ++;
				}

				receivingEdge = splitEdgeFromVertex(v.getOriginalUnderlying().getID()+"-"+edgeDirectionToSplit+edgesDoneSplit+n++, 
					v.getOriginalUnderlying(), v, pln, receivingEdge, newOrder, edgeMoving, getRot(originalOrder, i+(turnClockwise ? 1 : -1)), turnClockwise);
				
			}
			eo.replaceAll(newOrder);
			
			log.send("Changed vertex "+v+" order now: ", eo.getEdgesAsList());
		}
		
		return n;
	}

	private Direction getUsedEdgeDirection(ContainerVertex v, Edge edge) {
		if (edge instanceof ConnectionEdge) {
			Connection und = ((ConnectionEdge)edge).getOriginalUnderlying();
			RouteRenderingInformation rri = (RouteRenderingInformation) und.getRenderingInformation();
			if (rri.isContradicting()) {
				return null;
			}
		} 
		
		return edge.getDrawDirectionFrom(v);
	}

	private Edge getRot(List<Edge> originalOrder, int i) {
		return originalOrder.get((i + originalOrder.size() + originalOrder.size()) % originalOrder.size());
	}

	/**
	 * Splits the receivingEdge with a new vertex, and moves "mover" onto it.  "after" is the edge following mover in the current ordering
	 */
	private Edge splitEdgeFromVertex(String vertexName, Container c, Vertex v, Planarization pln, Edge receivingEdge, List<Edge> newOrder, Edge mover, Edge after, boolean clockwise) {
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
		Edge newArc = getNewArc(newVertex, v);
		Edge oldArc = getOldArc(newVertex, newArc);
		List<Face> faces = pln.getEdgeFaceMap().get(mover);
		Face onFace = faces.get(0).contains(newArc) ? faces.get(0) : faces.get(1);
		Face offFace = (!faces.get(0).contains(newArc)) ? faces.get(0) : faces.get(1);
		List<Edge> onList = onFace.getEdgesCopy();
		List<Edge> offList = (offFace!=onFace) ? offFace.getEdgesCopy() : onList;

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
		
		
		// fix the vertex edge ordering
		newOrder.remove(mover);
		int brokenEdge = newOrder.indexOf(receivingEdge);
		newOrder.set(brokenEdge, newArc.meets(v) ? newArc : oldArc);
		List<Edge> otherEndOrder = pln.getEdgeOrderings().get(newVertex).getEdgesAsList();
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

	private void insertBetween(List<Edge> offFace, Edge newArc, Vertex newVertex, Edge incoming, Edge outgoing, Vertex v, boolean clockwise) {
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

	private Edge getNewArc(Vertex newVertex, Vertex v) {
		for (Edge e : newVertex.getEdges()) {
			if (e.meets(v)) {
				return e;
			}
		}

		throw new LogicException("Could not find edge meeting " + v);
	}

	private Edge getOldArc(Vertex newVertex, Edge newArc) {
		for (Edge e : newVertex.getEdges()) {
			if (e != newArc) {
				return e;
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
