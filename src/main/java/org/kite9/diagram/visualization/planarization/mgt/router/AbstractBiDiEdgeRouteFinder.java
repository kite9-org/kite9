package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.EnumSet;
import java.util.Set;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException;
import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.NoElementVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

public abstract class AbstractBiDiEdgeRouteFinder extends AbstractRouteFinder {

	Double maximumBoundedAxisDistance;
	Set<Direction> allowedCrossingDirections;
	RoutingInfo startZone;
	RoutingInfo completeZone;
	ElementMapper em;
	CrossingType it;
	GeographyType gt;
	
	/**
	 * Figure out if the edge is crossing in a perpendicular direction.
	 */
	protected boolean canCrossBidiEdge(BiDirectionalPlanarizationEdge a, boolean goingDown) {
		if (a.getDrawDirection()==null) {
			return true;
		}
		
		// check that the crossing will be orthogonal
		Direction crossDirection = Direction.values()[(a.getDrawDirection().ordinal() + (goingDown ? -1 : 1) + 4) % 4];
		boolean ok = (allowedCrossingDirections.contains(crossDirection));
		
		if (ok) {
			if (pathDirection != null) {
				// check that the two edges intersect
				@SuppressWarnings("unchecked")
				BiDirectional<Connected> c = (BiDirectional<Connected>) a.getOriginalUnderlying();
				RoutingInfo from = rh.getPlacedPosition(c.getFrom());
				RoutingInfo to = rh.getPlacedPosition(c.getTo());
				RoutingInfo area = rh.increaseBounds(from, to);
				if (rh.overlaps(area, completeZone)) {
					return true;
				}  else {
					log.send(a+" is not local to "+e+" - no cross");
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	protected boolean canCrossBorderEdge(BorderEdge crossing, EdgePath ep) {
		return false;
	}

	protected boolean canRouteToVertex(Vertex to, PlanarizationEdge edge, boolean pathAbove, Going g, boolean arriving) {
			if ((to.getPosition() == null) || (to instanceof NoElementVertex)) {
				return false;
			}
			
			boolean forwards = g == Going.FORWARDS;
			boolean clockwise = (forwards == pathAbove);
			Direction dd;
			if (!arriving) {
				clockwise = !clockwise;
				dd = exitDirection;
			} else {
				dd = Direction.reverse(entryDirection);
			}
			
			if (dd==null) {
				return true;
			}
			
			VertexEdgeOrdering eo = (VertexEdgeOrdering) p.getEdgeOrderings().get(to);
			
			if (eo == null) {
				return true;
			}
			
			if (eo.getEdgeDirections() != VertexEdgeOrdering.MUTLIPLE_DIRECTIONS) {
				return true;
			}
			
	//		System.out.println(p);
	//		System.out.println("Can route to="+to+" edge = "+edge+" above="+pathAbove+" going="+g+" arriving="+arriving);

			if (edge==null) {
				boolean forwardSet = arriving ? g==Going.BACKWARDS : g==Going.FORWARDS;
				edge = p.getFirstEdgeAfterPlanarizationLine(to, forwardSet, pathAbove);
				boolean out = eo.canInsert(edge, dd, !clockwise, log);
				return out;
			} else {
				return eo.canInsert(edge, dd, clockwise, log);
			}
		}

	/**
	 * When the path crosses an edge, the direction of the edge must be 90 degrees advanced from the path direction,
	 * otherwise there will be a contradiction in the planarization.
	 */
	protected boolean canCross(Edge e2, EdgePath ep, boolean goingDown) {
		if (e2 instanceof BorderEdge) {
			return canCrossBorderEdge((BorderEdge) e2, ep);
		} else if (e2 instanceof BiDirectionalPlanarizationEdge) {
			// regular connection edge
			return canCrossBidiEdge((BiDirectionalPlanarizationEdge) e2, goingDown);
		} else {
			throw new Kite9ProcessingException("Don't know edge type: "+e2.getClass());
		}
	}
	
	public AbstractBiDiEdgeRouteFinder(MGTPlanarization p, RoutableReader rh, BiDirectionalPlanarizationEdge ci, ElementMapper em, Direction path, Direction entry, Direction exit, CrossingType it, GeographyType gt) {
		super(p, rh, getEndZone(rh, ci), getExpensiveAxis(ci, gt), getBoundedAxis(ci, gt), ci);
		this.startZone = getRoutingZone(rh, ci, true);
		RoutingInfo endZone = getRoutingZone(rh, ci, false);
		this.completeZone = getCompleteZone(ci);
		this.em = em;
		this.it = it;
		this.gt = gt;
		
		this.maximumBoundedAxisDistance = getMaximumBoundedAxisDistance(this.bounded);
		this.allowedCrossingDirections = getCrossingDirections(path, it);
		this.illegalEdgeCross = getIllegalEdgeCrossAxis(path, it);
		this.entryDirection = getDirection(entry, it);
		this.exitDirection = getDirection(exit, it);
		this.pathDirection = getDirection(path, it);
	
		if (rh.isWithin(startZone, endZone) || rh.isWithin(endZone, startZone)) {
			throw new EdgeRoutingException("Edge can't be routed as it is from something inside something else: "+e);
		}
		
		this.preferredSide = decidePreferredSide(ci);
		
		
 		log.send("Preferred Side: "+preferredSide);
		log.send("Route Finding for: "+e);
	}

	private PlanarizationSide decidePreferredSide(Edge ci) {
		if (ci instanceof ConnectionEdge) {
			String id = ((ConnectionEdge)ci).getID();
			int hash = id.hashCode();
			return (hash % 2 == 1) ? PlanarizationSide.ENDING_ABOVE : PlanarizationSide.ENDING_BELOW;
		} else {
			return null;
		}
	}

	private static RoutingInfo getEndZone(RoutableReader rh, BiDirectionalPlanarizationEdge ci) {
		return getRoutingZone(rh, ci, false);
	}
	
	protected static void checkContainerNotWithinGrid(Container c) {
		Container parent = c.getContainer();
		if ((parent != null) && (parent.getLayout() == Layout.GRID)) {
			throw new EdgeRoutingException("Edge can't be routed as it can't come from a container embedded in a grid: "+c);
		}				
	}
 	
	/**
	 * The routing zone is the area of the DiagramElement, as opposed to the vertices representing it.
	 */
	private static RoutingInfo getRoutingZone(RoutableReader rh, BiDirectionalPlanarizationEdge ci, boolean from) {
		return rh.getPlacedPosition(from ? ci.getFromConnected() : ci.getToConnected());
	}

	private RoutingInfo getCompleteZone(BiDirectionalPlanarizationEdge ci) {
		RoutingInfo from = getRoutingZone(rh, ci, true);
		RoutingInfo to = getRoutingZone(rh, ci, false);
		return rh.increaseBounds(from, to);
	}

	private Direction getDirection(Direction edgeDir, CrossingType it) {
		if (it == CrossingType.UNDIRECTED) {
			return null;
		} else {
			return edgeDir;
		}
	}

	/**
	 * Provides an extra directional check
	 */
	@Override
	public LocatedEdgePath createShortestPath() throws NoFurtherPathException {
		if ((e.getDrawDirection() != null) && (gt != GeographyType.RELAXED)) {
			// check that destination is in the correct direction for this to even work
			if (!isBasicEdgeDirectionAllowed()) {
				throw new NoFurtherPathException("Edge direction incompatible with start/end positions");
			}
		}
		
		return super.createShortestPath();
	}

	private boolean isBasicEdgeDirectionAllowed() {
		switch (e.getDrawDirection()) {
		case UP:
			return startZone.compareY(endZone) == 1;
		case DOWN:
			return startZone.compareY(endZone) == -1;
		case LEFT:
			return startZone.compareX(endZone) == 1;
		case RIGHT:
			return startZone.compareX(endZone) == -1;
		default:
			return true;
		}
	}
	
	private Axis getIllegalEdgeCrossAxis(Direction edgeDir, CrossingType it) {
		if ((it==CrossingType.UNDIRECTED) || (edgeDir==null)) {
			return null;
		} else {
			switch (edgeDir) {
			case UP:
			case DOWN:
				return Axis.VERTICAL;
			case LEFT:
			case RIGHT:
				return Axis.HORIZONTAL;
			}
		}
		
		return null;
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
		BiDirectionalPlanarizationEdge bpe = (BiDirectionalPlanarizationEdge) e;
		if (bpe.getDrawDirection() == null) {
			return null;
		}
		
		// work out furthest possible vertices apart
		Vertex from = getFarthestVertex(bpe.getFromConnected(), bpe.getDrawDirection());
		Vertex to = getFarthestVertex(bpe.getToConnected(), Direction.reverse(bpe.getDrawDirection()));
		
		
		if (ax != null) {
			LineRoutingInfo minPath = rh.move(null, from.getRoutingInfo(), null);
			minPath = rh.move(minPath, to.getRoutingInfo(), null);
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

	private Vertex getFarthestVertex(Connected c, Direction d) {
		if (em.hasOuterCornerVertices(c)) {
			CornerVertices cv = em.getOuterCornerVertices(c);
			switch (d) {
			case UP:
			case LEFT:
				return cv.getBottomRight();
			case DOWN:
			case RIGHT:
			default:
				return cv.getTopLeft();
			}
		} else {
			return em.getPlanarizationVertex(c);
		}
	}

	@Override
	protected boolean canTravel(int pathVertex, Going endingDirection, boolean b) {
		return true;
	}
	
	/**
	 * Ensures that we are starting/terminating on a vertex on the right side of the 
	 * container we are leaving/arriving at.
	 */
	protected boolean onCorrectSideOfContainer(MultiCornerVertex v, boolean termination) {
		Direction dd = termination ? exitDirection : entryDirection;
		
		if (dd == null) {
			return true;
		}
		
		if (it!=CrossingType.STRICT) {
			return true;
		}
			
		switch (dd) {
		case UP:
			return termination ? MultiCornerVertex.isMax(v.getYOrdinal()) : MultiCornerVertex.isMin(v.getYOrdinal());
		case DOWN:
			return termination ? MultiCornerVertex.isMin(v.getYOrdinal()) : MultiCornerVertex.isMax(v.getYOrdinal());
		case LEFT:
			return termination ? MultiCornerVertex.isMax(v.getXOrdinal()) : MultiCornerVertex.isMin(v.getXOrdinal());
		case RIGHT:
			return termination ? MultiCornerVertex.isMin(v.getXOrdinal()) : MultiCornerVertex.isMax(v.getXOrdinal());		
		}
		
		throw new LogicException("Can't determine whether we can arrive/leave at this vertex");
	}

	private PlanarizationSide preferredSide;

	@Override
	protected int compareFavouredSide(PlanarizationSide a, PlanarizationSide b) {
		if (a==preferredSide) {
			return -1;
		} else {
			return 1;
		}
	}
	
	

}
