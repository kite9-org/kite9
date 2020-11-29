package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.logging.LogicException;

public class ConnectionEdgeRouteFinder extends AbstractBiDiEdgeRouteFinder {

	Set<Container> mustCrossContainers;
	
	protected boolean canCrossBorderEdge(BorderEdge crossing, EdgePath ep) {
		BorderTraversal traversalRule = getTraversalRule(crossing);
		if (traversalRule == BorderTraversal.NONE) {
			return false;
		} else if (traversalRule == BorderTraversal.LEAVING) {
			boolean allPresent = mustCrossContainers.containsAll(crossing.getDiagramElements().keySet());

			if (!allPresent) {
				return false;
			}
		}
		
		if (pathDirection==null) {
			return true;
		} else {
			if (Direction.isHorizontal(pathDirection) == Direction.isHorizontal(crossing.getDrawDirection())) {
				// edges must be perpendicular
				return false;
			}
			
			DiagramElement entering = crossing.getElementForSide(pathDirection);
			
			if (entering == null) {
				// we are actually leaving a container
				entering = crossing.getElementForSide(Direction.reverse(pathDirection)).getParent();
			}
		
			// check that the container is positioned somewhere that intersects with the edge direction 
			if (!checkContainerPathIntersection(ep, entering, pathDirection)) {
				log.send(e+" can't cross into "+entering);
				return false;
			}

//			if (incidentDirection != expectedDirection) {
//				log.send(e+" can't cross over "+crossing+" in direction: "+incidentDirection+", expected: "+expectedDirection);
//				return false;
//			}
			
			return true;
 		}		
	}

	private BorderTraversal getTraversalRule(BorderEdge crossing) {
		return crossing.getBorderTraversal();
	}

	private boolean checkContainerPathIntersection(EdgePath ep, DiagramElement c, Direction ed) {
		RoutingInfo cri = rh.getPlacedPosition(c);
		return rh.isInPlane(cri, startZone, (pathDirection == Direction.RIGHT) || (pathDirection == Direction.LEFT));
	}

	
	public ConnectionEdgeRouteFinder(MGTPlanarization p, RoutableReader rh, ConnectionEdge ci, ElementMapper em,  Direction path, CrossingType it, GeographyType gt) {
		super(p, rh, ci, em, path, it, gt);
		this.mustCrossContainers = getMustCrossContainers(ci.getOriginalUnderlying().getFrom(), ci.getOriginalUnderlying().getTo());
	}

	private Set<Container> getMustCrossContainers(Connected from, Connected to) {
		Set<Container> out = new HashSet<>();
		while (from != to) {
			int fromDepth = from.getDepth();
			int toDepth = to.getDepth();
			if (fromDepth > toDepth) {
				if (from instanceof Container) {
					out.add((Container) from);
				}
				from = (Connected) from.getParent();
				
			} else if (toDepth > fromDepth) {
				if (to instanceof Container) {
					out.add((Container) to);
				}
				to = (Connected) to.getParent();
				
			} else {
				if (from instanceof Container) {
					out.add((Container) from);
				}
				if (to instanceof Container) {
					out.add((Container) to);
				}
				from = (Connected) from.getParent();
				to = (Connected) to.getParent();
			}
		}
		
		return out;
	}



	
	protected boolean canAddToQueue(LocatedEdgePath ep) {
		if (maximumBoundedAxisDistance != null) {
			double currentAxisTotal = ep.costing.minimumBoundedAxisDistance;
			boolean out = currentAxisTotal <= maximumBoundedAxisDistance + tolerance;
			if (!out) {
				log.send("Exceeded maximumBoundedAxisDistance: "+currentAxisTotal+" (max="+maximumBoundedAxisDistance+")"+" for: "+ep);
			}
			return out;
		} else {
			return true;
		}
	}

	private Set<Direction> getCrossingDirections(Direction edgeDir, CrossingType it) {
		
		if ((edgeDir == null) || (it ==CrossingType.UNDIRECTED)) {
			return EnumSet.allOf(Direction.class);
		}
		
		if (it==CrossingType.NOT_BACKWARDS) {
			Set<Direction> out = EnumSet.allOf(Direction.class);
			out.remove(Direction.reverse(edgeDir));
			return out;
		} else {
			Set<Direction> out  = EnumSet.of(edgeDir);
			return out;
		}
	}
	
	protected boolean allowConnectionsToContainerContents() {
		return true;
	}
	
	@Override
	protected void createInitialPaths(State<LocatedEdgePath> pq) {
		Vertex from = e.getFrom();
		
		if (from instanceof MultiCornerVertex) {
			Container c = (Container) ((BiDirectionalPlanarizationEdge)e).getFromConnected();
			checkContainerNotWithinGrid(c);
			CornerVertices cvs = em.getOuterCornerVertices(c);
			for (MultiCornerVertex v : cvs.getPerimeterVertices()) {
				if (!v.isPartOf(c)) {
					// ensure anchors are set correctly for the perimeter.
					v.addAnchor(null, null, c);
				}
				
				if (onCorrectSideOfContainer((MultiCornerVertex) v, false)) {
					createInitialPathsFrom(pq, v);
				}
			}
			
			if (allowConnectionsToContainerContents()) {
				for (DiagramElement con : c.getContents()) {
					if (con instanceof Connected) {
						if (!(con instanceof Container)) {
							Vertex vcon = em.getPlanarizationVertex((Connected) con);
							createInitialPathsFrom(pq, vcon);
						}
					}
				}
			}
		} else {
			createInitialPathsFrom(pq, from);
		}
	}
	
	protected void createInitialPathsFrom(State<LocatedEdgePath> pq, Vertex from) {
		try {
			generatePaths(null, p.getAboveBackwardLinks(from), pq, from,Going.BACKWARDS, PlanarizationSide.ENDING_ABOVE);
			generatePaths(null, p.getAboveForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_ABOVE);
			generatePaths(null, p.getBelowBackwardLinks(from), pq, from, Going.BACKWARDS, PlanarizationSide.ENDING_BELOW);
			generatePaths(null, p.getBelowForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_BELOW);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Axis getBoundedAxis(Edge e, GeographyType gt) {
		Direction dir = e.getDrawDirection();
		if (dir==null) {
			return null;
		}
		
		if (gt ==GeographyType.RELAXED) {
			return null;
		}
		
		Axis out = null;
		
		switch (dir) {
		case UP:
		case DOWN:
			out = Axis.VERTICAL;
			break;
		case LEFT:
		case RIGHT:
			out = Axis.HORIZONTAL;
			break;
		}
		
		return out;
	}

	private static Axis getExpensiveAxis(Edge e, GeographyType it) {
		Direction edgeDir = e.getDrawDirection();
		boolean flip = Tools.isUnderlyingContradicting(e);
		if (edgeDir==null) {
			return null;
		}
		
		Axis out = null;
		
		switch (edgeDir) {
		case UP:
		case DOWN:
			out = flip ? Axis.VERTICAL : Axis.HORIZONTAL;
			break;
		case LEFT:
		case RIGHT:
			out = flip ? Axis.HORIZONTAL : Axis.VERTICAL;
			break;
		}
		
		return out;
	}
	
	private Double getMaximumBoundedAxisDistance(Axis ax) {
		if ((e.getFrom() instanceof MultiCornerVertex) || (e.getTo() instanceof MultiCornerVertex)) {
			// this method is unsafe for containers because they have gutters around the positions of the things inside them.
			return null;
		}
		
		
		if (ax != null) {
			LineRoutingInfo minPath = rh.move(null, startZone, null);
			minPath = rh.move(minPath, endZone, null);
			if (ax==Axis.HORIZONTAL) {
				return minPath.getHorizontalRunningCost();
			} else if (ax==Axis.VERTICAL) {
				return minPath.getVerticalRunningCost();
			} else {
				throw new LogicException("No axis to process");
			}
		} else {
			return null;
		}
	}

	@Override
	protected boolean canTravel(int pathVertex, Going endingDirection, boolean b) {
		return true;
	}

	@Override
	protected boolean isTerminationVertex(int v) {
		DiagramElement originalUnderlying = ((ConnectionEdge)e).getToConnected();
		Vertex candidate = p.getVertexOrder().get(v);
		
		if (candidate instanceof MultiCornerVertex) {
			
			//DiagramElement und = candidate.getOriginalUnderlying();
			// return true if this is a container vertex for the container we're trying to get to
			if ((candidate.isPartOf(originalUnderlying)) && (onCorrectSideOfContainer((MultiCornerVertex) candidate, true))) {
				return true;
			}
			
			
			boolean out = false;
			if (allowConnectionsToContainerContents()) {
				// return false if this element is not in the correct container.
				if (!candidateIsContainedIn((MultiCornerVertex) candidate, originalUnderlying)) {
					return false;
				}
				
				RoutingInfo ri = candidate.getRoutingInfo();
				if (ri == null) {
					return false;
				}
				
				// return true if the vertex is within the container.
				out = rh.isWithin(endZone, ri);
			}
			
			return out;
			
		} else {
			return candidate == e.getTo();
		}
	}
	
	private boolean candidateIsContainedIn(MultiCornerVertex candidate, DiagramElement originalUnderlying) {
		for (DiagramElement de : candidate.getDiagramElements()) {
			if (de.getParent() == originalUnderlying) {
				return true;
			}
		}
		
		return false;
	}


}
