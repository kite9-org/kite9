package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException;
import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.style.BorderTraversal;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mapping.ConnectionEdge;
import org.kite9.diagram.visualization.planarization.mapping.CornerVertices;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.ordering.PerimeterEdgeOrdering;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.serialization.CSSConstants;
import org.kite9.framework.serialization.EnumValue;

public class ConnectionEdgeRouteFinder extends AbstractRouteFinder {

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
	protected boolean canCrossConnectionEdge(Edge a, boolean goingDown) {
		if (a.getDrawDirection()==null) {
			return true;
		}
		
		// check that the crossing will be orthogonal
		Direction crossDirection = Direction.values()[(a.getDrawDirection().ordinal() + (goingDown ? -1 : 1) + 4) % 4];
		boolean ok = (allowedCrossingDirections.contains(crossDirection));
		
		if (ok) {
			if (entryDirection != null) {
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
		Container insideContainer = ep.insideContainer();
		boolean leaving = (crossing.getOriginalUnderlying() == insideContainer);

		BorderTraversal traversalRule = getTraversalRule(crossing);
		if (traversalRule == BorderTraversal.NONE) {
			return false;
		} else if (traversalRule == BorderTraversal.LEAVING) {
			if (!leaving) {
				return false;
			}
		}
		
		if (entryDirection==null) {
			return true;
		}
		
		DiagramElement c = crossing.getOriginalUnderlying();

		// check that the container is positioned somewhere that intersects with the edge direction
		if (!checkContainerPathIntersection(ep, c, entryDirection)) {
			log.send(e+" can't cross into "+c);
			return false;
		}
	
		PerimeterEdgeOrdering containerOrdering = (PerimeterEdgeOrdering) p.getEdgeOrderings().get(c);
		
		if (containerOrdering == null) {
			return true;
		}
		
		if (containerOrdering.getEdgeDirections() != VertexEdgeOrdering.MUTLIPLE_DIRECTIONS) {
			return true;
		}
		
		Edge leaverBefore = containerOrdering.getLeaverBeforeBorder(crossing);
	
		if (leaverBefore.getDrawDirection()==null) {
			Iterator<Edge> containerIterator = containerOrdering.getIterator(false, leaverBefore, leaverBefore, true);
			containerIterator.next();
			leaverBefore = containerIterator.next();
		}
		
		Direction incidentDirection = leaving ? entryDirection : Direction.reverse(entryDirection);
		boolean out = containerOrdering.canInsert(leaverBefore, incidentDirection, true, log);
		//System.out.println("Can cross: "+crossing+" between "+crossing.getFrom()+" "+crossing.getTo()+" leaving="+leaving+" result="+out+" direction="+incidentDirection+" currentlyInside="+insideContainer);
		return out;
	}

	private BorderTraversal getTraversalRule(BorderEdge crossing) {
		return crossing.getBorderTraversal();
	}

	private boolean checkContainerPathIntersection(EdgePath ep, DiagramElement c,
			Direction ed) {
		RoutingInfo cri = rh.getPlacedPosition(c);
		return rh.isInPlane(cri, startZone, (entryDirection == Direction.RIGHT) || (entryDirection == Direction.LEFT));
		//return true;
	}

	protected boolean canRouteToVertex(Vertex to, Edge edge, boolean pathAbove, Going g, boolean arriving) {
			if ((getPosition(to) == null) || (to.getOriginalUnderlying() == null)) {
				return false;
			}
			
			if (entryDirection==null) {
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
			boolean forwards = g == Going.FORWARDS;
			boolean clockwise = (forwards == pathAbove);
			Direction dd;
			if (!arriving) {
				clockwise = !clockwise;
				dd = entryDirection;
			} else {
				dd = Direction.reverse(entryDirection);
			}
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
		if (e2.getOriginalUnderlying() instanceof DiagramXMLElement) {
			// you can't leave the top container
			return false;
		}
	
		if ((e2.getOriginalUnderlying() instanceof Container) && (e.getOriginalUnderlying() instanceof Container)) {
			// one container can't cross another
			return false;
		}
	
		if (e2 instanceof BorderEdge) {
			return canCrossBorderEdge((BorderEdge) e2, ep);
		} else {
			// regular connection edge
			return canCrossConnectionEdge(e2, goingDown);
		}
	}
	
	public ConnectionEdgeRouteFinder(MGTPlanarization p, RoutableReader rh, Edge ci, ElementMapper em, Direction edgeDir, CrossingType it, GeographyType gt) {
		super(p, rh, getEndZone(rh, ci), getExpensiveAxis(ci, gt), getBoundedAxis(ci, gt), ci);
		this.startZone = getRoutingInfo(rh, ci, ci.getFrom());
		this.completeZone = getCompleteZone(ci);
		
		this.maximumBoundedAxisDistance = getMaximumBoundedAxisDistance(this.bounded);
		this.allowedCrossingDirections = getCrossingDirections(edgeDir, it);
		this.illegalEdgeCross = getIllegalEdgeCrossAxis(edgeDir, it);
		this.entryDirection = getEntryDirection(edgeDir, it);
		this.em = em;
		this.it = it;
		this.gt = gt;
				
		RoutingInfo ocStart = getRoutingInfoForOuterContainer(rh, ci.getFrom()), ocEnd = getRoutingInfoForOuterContainer(rh, ci.getTo());
		if (rh.isWithin(ocStart, ocEnd) || rh.isWithin(ocEnd, ocStart)) {
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

	private static RoutingInfo getEndZone(RoutableReader rh, Edge ci) {
		return getRoutingInfo(rh, ci, ci.getTo());
	}
	
	private static DiagramElement getCorrectUnderlying(Edge ci, Vertex v) {
		DiagramElement und = ci.getOriginalUnderlying();
		DiagramElement fromUnd = (DiagramElement) ((BiDirectional<?>) und).getFrom();
		DiagramElement toUnd = (DiagramElement) ((BiDirectional<?>) und).getTo();
		if (v.isPartOf(fromUnd)) {
			return fromUnd;					
		} else if (v.isPartOf(toUnd)) {
			return toUnd;					
		}

		throw new Kite9ProcessingException("Couldn't get underlying for "+v);
	}
	
	private static void checkContainerNotWithinGrid(Container c) {
		Container parent = c.getContainer();
		if ((parent != null) && (parent.getLayout() == Layout.GRID)) {
			throw new EdgeRoutingException("Edge can't be routed as it can't come from a container embedded in a grid: "+c);
		}				
	}
	
	private static RoutingInfo getRoutingInfoForOuterContainer(RoutableReader rh, Vertex v) {
		if (v instanceof MultiCornerVertex) {
			DiagramElement und = v.getOriginalUnderlying();
			return rh.getPlacedPosition(und);					
		} 
		
		return v.getRoutingInfo();
	}
 	
	private static RoutingInfo getRoutingInfo(RoutableReader rh, Edge ci, Vertex v) {
		if (v instanceof MultiCornerVertex) {
			DiagramElement und = getCorrectUnderlying(ci, v);
			return rh.getPlacedPosition(und);					
		} 
		
		return v.getRoutingInfo();
	}

	private RoutingInfo getCompleteZone(Edge ci) {
		RoutingInfo from = getRoutingInfo(rh, ci, ci.getFrom());
		RoutingInfo to = getRoutingInfo(rh, ci, ci.getTo());
		return rh.increaseBounds(from, to);
	}

	private Direction getEntryDirection(Direction edgeDir, CrossingType it) {
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
	
	@Override
	protected void createInitialPaths(State<LocatedEdgePath> pq) {
		Vertex from = e.getFrom();
		
		if (from instanceof MultiCornerVertex) {
			Container c = (Container) getCorrectUnderlying(e, from);
			checkContainerNotWithinGrid(c);
			CornerVertices cvs = em.getCornerVertices(c);
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
					if (!(con instanceof Container)) {
						Vertex vcon = em.getVertex((Connected) con);
						createInitialPathsFrom(pq, vcon);
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
		DiagramElement originalUnderlying = e.getTo().getOriginalUnderlying();
		Vertex candidate = p.getVertexOrder().get(v);
		
		if (candidate instanceof MultiCornerVertex) {
			
			DiagramElement und = candidate.getOriginalUnderlying();
			if (candidate instanceof MultiCornerVertex) {
				// return true if this is a container vertex for the container we're trying to get to
				if ((und == originalUnderlying) && (onCorrectSideOfContainer((MultiCornerVertex) candidate, true))) {
					return true;
				}
			}
			
			boolean out = false;
			if (allowConnectionsToContainerContents()) {
				// return false if this element is not in the correct container.
				if ((und != null) && (und.getContainer() != originalUnderlying)) {
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
	
	/**
	 * Ensures that we are starting/terminating on a vertex on the right side of the 
	 * container we are leaving/arriving at.
	 */
	private boolean onCorrectSideOfContainer(MultiCornerVertex v, boolean termination) {
		if (entryDirection == null) {
			return true;
		}
		
		if (it!=CrossingType.STRICT) {
			return true;
		}
			
		switch (entryDirection) {
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
