package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.List;

import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.ssp.AbstractSSP;
import org.kite9.diagram.common.algorithms.ssp.PathLocation;
import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.mgt.ContainerBorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing;
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarizationBuilder;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This contains the EdgePath class structure, which is a way of traversing through an MGT planarization.
 * 
 * It contains the logic for traversing the MGT and working out the best route to insert a given edge.
 * 
 * This uses an A* algorithm (generalizing SSP) for choosing the route an edge will take through the planarization. This
 * means that potentially, the edge can flip sides from outsideEdge to below on the planarization as often as it likes to
 * achieve the shortest path.
 * 
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractRouteFinder extends AbstractSSP<AbstractRouteFinder.LocatedEdgePath> implements Logable {

	@Override
	public String getPrefix() {
		return "GTRF";
	}

	@Override
	public boolean isLoggingEnabled() {
		return false;
	}

	public static final Double TOLERANCE = RHDPlanarizationBuilder.CONTAINER_VERTEX_SIZE / 10000;

	protected Kite9Log log = new Kite9Log(this);
	
	private int pathCount = 0;
	
	enum Going { FORWARDS, BACKWARDS};
	
	enum TransverseAxis { HORIZONTAL, VERTICAL };
	
	private static Routing getRouting(Going going, Place p) {
		if (p==null) {
			// start or end routing.
			return null;
		}
		
		switch (going) {
		case FORWARDS:
			switch (p) {
			case ABOVE:
				return Routing.OVER_FORWARDS;
			case BELOW:
				return Routing.UNDER_FORWARDS;
			}
		case BACKWARDS:
			switch (p) {
			case ABOVE:
				return Routing.OVER_BACKWARDS;
			case BELOW:
				return Routing.UNDER_BACKWARDS;
			}
		}
		
		throw new LogicException("Don't have a routing");
	}
	
	public class Costing implements Comparable<Costing> {
		
		double legalEdgeCrossCost;
		int totalEdgeCrossings;
		int totalPlanarizationCrossings;
		int illegalEdgeCrossings;

		double minimumTotalDistance;
		double minimumExpensiveAxisDistance;
		double minimumBoundedAxisDistance;
		
		
		public Costing() {
			super();
		}
		
		public Costing(Costing c) {
			super();
			this.legalEdgeCrossCost = c.legalEdgeCrossCost;
			this.totalEdgeCrossings = c.totalEdgeCrossings;
			this.totalPlanarizationCrossings = c.totalPlanarizationCrossings;
			this.minimumTotalDistance = c.minimumTotalDistance;
			this.minimumExpensiveAxisDistance = c.minimumExpensiveAxisDistance;
			this.minimumBoundedAxisDistance = c.minimumBoundedAxisDistance;
			this.illegalEdgeCrossings = c.illegalEdgeCrossings;
		}

		public int compareTo(Costing o) {
			// expensive (i.e. more important than crossings)
			if (!equalWithinTolerance(this.minimumExpensiveAxisDistance, o.minimumExpensiveAxisDistance)) {
				return ((Double) this.minimumExpensiveAxisDistance).compareTo(o.minimumExpensiveAxisDistance);
			}
			
			if (this.illegalEdgeCrossings != o.illegalEdgeCrossings) {
				return ((Integer) this.illegalEdgeCrossings).compareTo(o.illegalEdgeCrossings);
			}
			
			// route with minimum amount of crossing
			if (!equalWithinTolerance(this.legalEdgeCrossCost, o.legalEdgeCrossCost)) {
				return ((Double) this.legalEdgeCrossCost).compareTo(o.legalEdgeCrossCost);
			}

			// minimum distance
			if (!equalWithinTolerance(this.minimumTotalDistance, o.minimumTotalDistance)) {
				return ((Double) this.minimumTotalDistance).compareTo(o.minimumTotalDistance);
			}

			// some other things worth routing for
			if (this.totalEdgeCrossings != o.totalEdgeCrossings) {
				return ((Integer) this.totalEdgeCrossings).compareTo(o.totalEdgeCrossings);
			}
			
			if (this.totalPlanarizationCrossings != o.totalPlanarizationCrossings) {
				return ((Integer) this.totalPlanarizationCrossings).compareTo(o.totalPlanarizationCrossings);
			}
			
			// adding this gives us some consistency
			return 0;
		}
		
		private boolean equalWithinTolerance(double a, double b) {
			return Math.abs(a - b) < TOLERANCE;
		}

		@Override
		public String toString() {
			return "COST[el=" + legalEdgeCrossCost+
					" et=" + totalEdgeCrossings + 
					" pc=" + totalPlanarizationCrossings +
					" mtd=" + minimumTotalDistance + 
					" mbd=" + minimumBoundedAxisDistance +
					" med=" + minimumExpensiveAxisDistance+ "]";
		}

		
	}
	public static enum PlanarizationSide { ENDING_ABOVE, ENDING_BELOW }
	
	public LineRoutingInfo move(LineRoutingInfo current, int from, int to, Going g, Place pl, boolean includeLocation) {
		while ((Math.abs(from - to) > 1) && (pl != null)){
			from += (from < to) ? 1 : -1;
			current = move(current, from, g, pl);
		}
		if (includeLocation) {
			return move(current, to, g, pl);
		} else {
			return current;
		}
	}

	private LineRoutingInfo move(LineRoutingInfo current, int to, Going g, Place pl) {
		Vertex tov = p.getVertexOrder().get(to);
		// System.out.println("-- moving to "+tov+" going "+g+" place "+pl);
		RoutingInfo past = tov.getRoutingInfo();
		if (past != null) {
			Routing moveType = getRouting(g, pl);
			return getRouteHandler().move(current, past, moveType);
		} else {
			return current;
		}
	}
	
	/**
	 * A part of a path that does something, could be crossing a edge, the planarization, arriving somewhere.
	 */
	public abstract class EdgePath {
		
		int pathNumber = pathCount++;
		Costing costing;
		Going going;
		PlanarizationSide side;
		int pathParts;
		
		/**
		 * Which side of the planarization the path is travelling on.
		 */
		public PlanarizationSide getSide() {
			return side;
		}

		EdgePath prev;
		
		public Going getGoing() {
			return going;
		}
		
		public Costing getCosting() {
			return costing;
		}

		public abstract LineRoutingInfo getTrail();
		
		public abstract int getTrailEndVertex();
		
		public EdgePath(Going g, PlanarizationSide s, EdgePath prev) {
			this.going = g;
			this.side = s;
			this.costing = prev == null ? new Costing() : new Costing(prev.getCosting());
			if (prev != null) {
				while (prev instanceof SimpleEdgePath) {
					prev = prev.prev;
				}
				
				this.prev = prev;
			}
			
			this.pathParts = prev == null ? 0 : this.prev.pathParts + 1;
		}
		
		protected abstract void initTrail(EdgePath prev2);

		public abstract void append(StringBuilder sb);
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(150);
			EdgePath c = this;
			while (c != null) {
				c.append(sb);
				c = c.prev;
			}

			sb.append("("+pathNumber+", $="+costing +
					" g="+going+
					" pos=" + getTrail());
			sb.append(")");
			return sb.toString();
		}
		
		public abstract boolean sameCrossing(EdgePath other);
		
		/**
		 * Returns the container we are currently inside
		 */
		public abstract Container insideContainer();
	
	}
		
	/**
	 * Located edge paths have a location set.   These are added to the queue for the dijkstra
	 * routine, but aren't really of interest to the edge inserter.
	 */
	public abstract class LocatedEdgePath extends EdgePath implements PathLocation<LocatedEdgePath> {
		
		Location l;
		LineRoutingInfo trail;

		@Override
		public boolean sameCrossing(EdgePath other) {
			return false;
		}

		boolean active = true;

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public void setActive(boolean a) {
			this.active = a;
		}

		@Override
		public Object getLocation() {
			return l;
		}

		@Override
		public int compareTo(LocatedEdgePath o) {
			int out = costing.compareTo(o.getCosting());
			
			if ((out == 0)
					&& (this.pathParts != o.pathParts)) {
				return ((Integer) pathParts).compareTo(o.pathParts);
			}
			
			if ((out == 0) && (this.side != o.side)) {
				return compareFavouredSide(this.side, o.side);
			}
			
			return out;
		}

		@Override
		public LineRoutingInfo getTrail() {
			return trail;
		}

		@Override
		public int getTrailEndVertex() {
			return l.vertex;
		}

		public LocatedEdgePath(Location l, Going g, PlanarizationSide side, EdgePath prev) {
			super(g, side, prev);
			this.going = g;
			this.l = l;
			initTrail(prev);
			calculateRemainingCost();
		}
		

		/**
		 * Because the distance-so-far calculation does not represent the full cost of getting to a node, we add on the
		 * minimum remaining cost to get to the final node. Otherwise, SSP will not work correctly as we are not
		 * considering the actual cost to the node at each step.
		 * 
		 * By calculating cost to the final node, we are actually performing an A* calculation, and the SSP system will
		 * follow the path with the lowest minimum total cost, which will be better.
		 */
		protected void calculateRemainingCost() {
			// System.out.println("--remaining cost");
			LineRoutingInfo remaining = getRouteHandler().move(this.trail, endZone, null);
			//log.send("current pos: "+this.trail+" to: "+ remaining+ " cost: "+remaining.getRunningCost());
			calculateDistanceCosts(remaining);
		}

		protected void calculateDistanceCosts(LineRoutingInfo remaining) {
			costing.minimumTotalDistance = remaining.getRunningCost();
			
			if (expensive==Axis.VERTICAL) {
				costing.minimumExpensiveAxisDistance = remaining.getVerticalRunningCost();
			} else if (expensive == Axis.HORIZONTAL) {
				costing.minimumExpensiveAxisDistance = remaining.getHorizontalRunningCost();
			}
			
			if (bounded==Axis.VERTICAL) {
				costing.minimumBoundedAxisDistance = remaining.getVerticalRunningCost();
			} else if (bounded == Axis.HORIZONTAL) {
				costing.minimumBoundedAxisDistance = remaining.getHorizontalRunningCost();
			}
		}
	}

	public abstract class EdgeCrossPath extends EdgePath {

		Edge crossing;
		int trailEndVertex;
		LineRoutingInfo trail;
		Container inContainer;

		public EdgeCrossPath(Edge crossing, EdgePath prev, Going g) {
			super(g, prev.getSide(), prev);
			this.crossing = crossing;
			costing.legalEdgeCrossCost += ((PlanarizationEdge) crossing).getCrossCost();
			costing.totalEdgeCrossings ++;
			
			if (illegalEdgeCross == Axis.HORIZONTAL) {
				if ((crossing.getDrawDirection()==Direction.LEFT) || (crossing.getDrawDirection()==Direction.RIGHT)) {
					costing.illegalEdgeCrossings ++;
				}
			} else if (illegalEdgeCross == Axis.VERTICAL) {
				if ((crossing.getDrawDirection()==Direction.UP) || (crossing.getDrawDirection()==Direction.DOWN)) {
					costing.illegalEdgeCrossings ++;
				}
			}
			
			initTrail(prev);
			
			if (crossing instanceof ContainerBorderEdge) {
				Container inside = prev.insideContainer();
				DiagramElement crossingContainer = crossing.getOriginalUnderlying();
				if (crossingContainer == inside) {
					// we are leaving the container
					inContainer = ((Contained)inside).getContainer();
				} else {
					// we are entering a new container
					inContainer = (Container) crossingContainer;
				}
			} else {
				inContainer = prev.insideContainer();
			}
			
		}

		@Override
		public Container insideContainer() {
			return inContainer;
		}

		public Edge getCrossing() {
			return crossing;
		}

		@Override
		protected void initTrail(EdgePath prev2) {
			this.trailEndVertex = prev2.getTrailEndVertex();
			this.trail = prev2.getTrail();
		}

		@Override
		public boolean sameCrossing(EdgePath other) {
			if (other instanceof EdgeCrossPath) {
				return this.getCrossing() == ((EdgeCrossPath)other).getCrossing();
			} else {
				return false;
			}
		}

		@Override
		public int getTrailEndVertex() {
			return trailEndVertex;
		}

		@Override
		public LineRoutingInfo getTrail() {
			return trail;
		}

	}
	
	

	public class EndCrossEdgePath extends EdgeCrossPath {

		public EndCrossEdgePath(Edge e, EdgePath prev, Going g) {
			super(e, prev, g);
		}

		@Override
		public void append(StringBuilder sb) {
			sb.append("-");
			sb.append("ece(");
			sb.append(crossing);
			sb.append(")");
		}

	}

	public class FinishPath extends TerminalPath {

		public FinishPath(int vertex, Vertex v, EdgePath prev, Going g, Edge outsideEdge) {
			super(new Location(null, vertex, v), g, prev.getSide(), prev, outsideEdge);
		}

		@Override
		public void append(StringBuilder sb) {
			sb.append("finish(");
			sb.append(l);
			sb.append(",");
			sb.append(side);
			sb.append(")");
		}

		/**
		 * Instead of calculating the rest of the distance to the endZone, we now need to 
		 * calculate the actual distance to the vertex of choice that we are attaching to.
		 */
		@Override
		protected void calculateRemainingCost() {
			calculateDistanceCosts(trail);
		}

		@Override
		public void initTrail(EdgePath prev) {
			this.trail = move(prev.getTrail(), prev.getTrailEndVertex(), l.vertex, getGoing(), null, true);
		}

		@Override
		public Container insideContainer() {
			return prev.insideContainer();
		}
		
	}

	public class PlanarizationCrossPath extends EdgePath {

		int after;
		
		Vertex crossingPoint;
		LineRoutingInfo trail;
		boolean switchback;
		
	
		public Vertex getCrossingPoint() {
			return crossingPoint;
		}

		public void setCrossingPoint(Vertex crossingPoint) {
			this.crossingPoint = crossingPoint;
		}
		
		public boolean sameCrossing(EdgePath other) {
			if (other instanceof PlanarizationCrossPath) {
				return this.after == ((PlanarizationCrossPath)other).after;
			} else {
				return false;
			}
		}

		private PlanarizationCrossPath(int after, EdgePath prev, Going g, boolean switchback) {
			super(g, prev.side == PlanarizationSide.ENDING_ABOVE ? PlanarizationSide.ENDING_BELOW : PlanarizationSide.ENDING_ABOVE, prev);
			this.beforeV = p.getVertexOrder().get(after);
			this.afterV = p.getVertexOrder().get(after+1);			
			this.costing.totalPlanarizationCrossings++;
			this.after = after;
			initTrail(prev);
			this.switchback = switchback;
		}

		public void initTrail(EdgePath prev) {
			this.trail = move(prev.getTrail(), 
					prev.getTrailEndVertex(), 
					getTrailEndVertex(), 
					getGoing(), 
					getSide() == PlanarizationSide.ENDING_ABOVE ? Place.BELOW : Place.ABOVE, 
					true);
		}

		Vertex afterV;
		
		public Vertex getAfterV() {
			return afterV;
		}

		public Vertex getBeforeV() {
			return beforeV;
		}

		Vertex beforeV;

		@Override
		public void append(StringBuilder sb) {
			sb.append("-");
			sb.append("cross(");
			if (crossingPoint == null) {
			sb.append(after+","+(after+1));
			} else {
				sb.append(crossingPoint);
			}
			if (switchback) {
				sb.append(",SB");
			}
			sb.append(")");
		}

		@Override
		public LineRoutingInfo getTrail() {
			return trail;
		}

		@Override
		public int getTrailEndVertex() {
			if (going == Going.FORWARDS) {
				return after;
			} else {
				return after+1;
			}
		}

		@Override
		public Container insideContainer() {
			return prev.insideContainer();
		}


	}

	public class SimpleEdgePath extends LocatedEdgePath {

		public SimpleEdgePath(int newVertex, boolean above, Vertex vertex, EdgePath prev, Going g) {
			super(new Location(above ? Place.ABOVE : Place.BELOW, newVertex, vertex), 
					g, 
					prev.getSide(),
					prev);
			
		}

		@Override
		public void append(StringBuilder sb) {
			sb.append("-sep(");
			sb.append(l);
			sb.append(",");
			sb.append(side);
			sb.append(")");
		}

		@Override
		public void initTrail(EdgePath prev) {
			this.trail = move(prev.getTrail(), prev.getTrailEndVertex(), l.vertex, getGoing(), l.p, true);
		}

		@Override
		public Container insideContainer() {
			return prev.insideContainer();
		}
	}

	public class StartCrossEdgePath extends EdgeCrossPath {

		public StartCrossEdgePath(Edge e, EdgePath prev, Going g) {
			super(e, prev, g);
		}

		@Override
		public void append(StringBuilder sb) {
			sb.append("-");
			sb.append("sce(");
			sb.append(crossing);
			sb.append(")");
		}
	}
	
	public abstract class TerminalPath extends LocatedEdgePath {
		
		Edge outsideEdge;

		public TerminalPath(Location l, Going g, PlanarizationSide side, EdgePath prev, Edge outsideEdge) {
			super(l, g, side, prev);
			this.outsideEdge = outsideEdge;
		}
		
		/**
		 * Edge that this path terminates outside of
		 */
		public Edge getOutsideEdge() {
			return outsideEdge;
		}
		
	}

	public class StartPath extends TerminalPath {

		public StartPath(int vertex, Vertex v, Place p, PlanarizationSide side, RoutingInfo position, Going going, Edge outsideEdge) {
			super(new Location(p, vertex, v), going, side, null, outsideEdge);
		}

		@Override
		public void append(StringBuilder sb) {
			sb.append("-start(");
			sb.append(l);
			sb.append(",");
			sb.append(side);
			sb.append(")");
		}

		@Override
		public void initTrail(EdgePath unused) {
			this.trail = getRouteHandler().move(null, l.v.getRoutingInfo(), null);
		}

		@Override
		public Container insideContainer() {
			Vertex v = l.v;
			DiagramElement de = v.getOriginalUnderlying();
			if (de instanceof Contained) {
				return ((Contained)de).getContainer();
			}
			
			throw new LogicException("Was expecting something to have a container");
		}

	}

	static enum Place { ABOVE, BELOW };
	
	/**
	 * This class holds the location of the ssp node, which can be either outsideEdge or below or arriving at any given vertex.
	 */
	public static class Location {

		public Place p;
		private int vertex;
		private Vertex v;

		@Override
		public String toString() {
			return "LOC[p=" + p + ", vertex=" + vertex + ",v="+v+"]";
		}

		private Location(Place p, int vertex, Vertex v) {
			super();
			this.p = p;
			this.vertex = vertex;
			this.v = v;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((p == null) ? 0 : p.hashCode());
			result = prime * result + vertex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Location other = (Location) obj;
			if (p != other.p)
				return false;
			if (vertex != other.vertex)
				return false;
			return true;
		}

		public Vertex getVertex() {
			return v;
		}
	}
	
	enum Axis { HORIZONTAL, VERTICAL }

	public AbstractRouteFinder(MGTPlanarization p, RoutableReader rh, RoutingInfo endZone, Axis expensive, Axis bounded, Edge e) {
		super();
		this.p = p;
		this.rh = rh;
		this.endZone = endZone;
		this.expensive = expensive;
		this.bounded = bounded;
		this.e = e;
	}

	protected MGTPlanarization p;
	protected RoutableReader rh;
	protected RoutingInfo endZone;
	protected Axis expensive, bounded, illegalEdgeCross;
	protected Edge e;
	protected Direction entryDirection;


	public RoutableReader getRouteHandler() {
		return rh;
	}

	public void addToQueue(State<LocatedEdgePath> pq, LocatedEdgePath ep) {
		if ((ep != null)) {
			if (canAddToQueue(ep) && pq.add(ep)) {
				log.send(log.go() ? null : "Adding: " + ep);
			} else {
				log.send(log.go() ? null : "Not Adding: " + ep);				
			}
		}
	}

	/**
	 * Allows us to exclude a path based on some criteria after it has been generated.
	 */
	protected abstract boolean canAddToQueue(LocatedEdgePath ep);

	/**
	 * Takes an EdgePath, and crosses some edges to move it either outsideEdge or below a the pathVertex, which turns it into a proper LocatedEdgePath.
	 */
	private LocatedEdgePath escape(Edge outsideOf, EdgePath forwardIn, List<Edge> inside, List<Edge> outside, boolean pathAbove, int pathVertex, Going startingDirection, Going endingDirection) {
		if (forwardIn==null) {
			return null;
		}
		
		if (!canTravel(pathVertex, endingDirection, forwardIn.side == PlanarizationSide.ENDING_ABOVE)) {
			return null;
		}
	
		EdgePath forward = forwardIn;
		
		// have to move to the bottom of the inside group
		int currentlyOutside = inside.indexOf(outsideOf);
		if (currentlyOutside > -1) {
			for (int i = currentlyOutside; i >= 0; i--) {
				Edge edge = inside.get(i);
				Integer toi = meetsDestination(edge, startingDirection); 
				if (toi != null) {
					Vertex toV = p.getVertexOrder().get(toi);
					if (canRouteToVertex(toV, edge, forward.getSide()==PlanarizationSide.ENDING_ABOVE, startingDirection, true)) {
						return new FinishPath(toi, toV, forward, startingDirection, outsideOf);
					}
				}
				
				if (!canCross(edge, forward, !pathAbove)) {
					return null;
				}
				forward = new EndCrossEdgePath(edge, forward, startingDirection);
				outsideOf = edge;
			}
		}
		
		// cross the planarization
		if ((pathAbove && forward.getSide() == PlanarizationSide.ENDING_BELOW) || 
			((!pathAbove) && forward.getSide() == PlanarizationSide.ENDING_ABOVE)) {
			int after = endingDirection == Going.FORWARDS ? pathVertex - 1 : pathVertex;
			if (canSwitchSides(after)) {
				forward = new PlanarizationCrossPath(after, forward, startingDirection, startingDirection!=endingDirection);
			} else {
				return null;
			}
		}

		// now have to work outwards on the outside group
		currentlyOutside = outsideOf == null ? -1 : outside.indexOf(outsideOf);

		for (int i = currentlyOutside + 1; i < outside.size(); i++) {
			Edge edge = outside.get(i);
			
			
			if (!canCross(edge, forward, !pathAbove)) {
				return null;
			}
			forward = new EndCrossEdgePath(edge, forward, endingDirection);
			
			Integer finish = meetsDestination(edge, endingDirection);
			if (finish!=null) {
				Vertex toV = p.getVertexOrder().get(finish);
				if (canRouteToVertex(toV, edge, forward.getSide()==PlanarizationSide.ENDING_ABOVE, endingDirection, true)) {
					return new FinishPath(finish, toV, forward, endingDirection, edge);
				}
			}
		

		}

		return new SimpleEdgePath(pathVertex, pathAbove, p.getVertexOrder().get(pathVertex), forward, endingDirection);
	}

	/**
	 * Prevents the path wending through vertices that are on top of each other
	 */
	protected boolean canSwitchSides(int after) {
		Vertex beforeV = p.getVertexOrder().get(after);
		Vertex afterV = p.getVertexOrder().get(after+1);
		RoutingInfo bri = beforeV.getRoutingInfo();
		RoutingInfo ari = afterV.getRoutingInfo();
		if ((bri != null) && (ari != null) && (rh.overlaps(bri, ari))) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Determines whether the route can cross a given edge.
	 */
	protected abstract boolean canCross(Edge edge, EdgePath forward, boolean above);

	/**
	 * Determines whether the route can continue above/below an existing vertex.
	 */
	protected abstract boolean canTravel(int pathVertex, Going endingDirection, boolean b);

	protected RoutingInfo getPosition(Vertex v) {
		RoutingInfo out = v.getRoutingInfo();
		if (out == null) {
			DiagramElement und = v.getOriginalUnderlying();
			out = rh.getPlacedPosition(und);
		}
		
		return out;
	}

	private Integer meetsDestination(Edge edge, Going g) {
		if (g==Going.FORWARDS) {
			Vertex to = edge.getTo();
			int toi = p.getVertexIndex(to);
			if (isTerminationVertex(toi)) {
				return toi;
			} 
		} else if (g==Going.BACKWARDS){
			Vertex from = edge.getFrom();
			int fromi = p.getVertexIndex(from);
			if (isTerminationVertex(fromi)) {
				return fromi;
			}
		}
		
		return null;
	}

	@Override
	protected void generateSuccessivePaths(LocatedEdgePath r, State<LocatedEdgePath> pq) {
		log.send(log.go() ? null : "Extending path: "+r);
		Vertex from = p.getVertexOrder().get(r.l.vertex);
		generatePaths(r, getLinkSet(r.getGoing(), from, r.getSide()), pq, from, r.getGoing(), r.getSide());
		boolean backOk = (r.l.vertex > 0) && (r.l.vertex < p.getVertexOrder().size()-1) && (entryDirection==null);
		if (backOk) {
			generateSwitchbackPaths(pq, r, from, r.getGoing());
		}
	}

	private List<Edge> getLinkSet(Going g, Vertex from, PlanarizationSide s) {
		if (s == PlanarizationSide.ENDING_ABOVE) {
			return g == Going.FORWARDS ? p.getAboveForwardLinks(from) : p.getAboveBackwardLinks(from);
		} else if (s==PlanarizationSide.ENDING_BELOW) {
			return g == Going.FORWARDS ? p.getBelowForwardLinks(from) : p.getBelowBackwardLinks(from);
		} else {
			throw new LogicException("Was expecting a side"+s);
		}
	}

	/**
	 * This does a handbrake turn around a given vertex, leaving you facing back in the other direction, on the other side of the planarization line
	 * Obviously this doesn't get used for directed edges, as they can't turn.
	 */
	private void generateSwitchbackPaths(State<LocatedEdgePath> pq, LocatedEdgePath r, Vertex from,  Going toStartWith) {
		List<Edge> insideLinks;
		List<Edge> outsideLinks;
		Edge first = null;
		Going endingUp = toStartWith == Going.FORWARDS ? Going.BACKWARDS : Going.FORWARDS;
		boolean endingAbove = r.l.p==Place.ABOVE;
		if (endingAbove) {
			insideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_ABOVE);
			outsideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_BELOW);
		} else {
			insideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_BELOW);
			outsideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_ABOVE);
		}
		
		if (insideLinks.size() > 0) {
			first = insideLinks.get(insideLinks.size()-1);
		}
		
		//if (insideLinks.size() + outsideLinks.size()==0) {
		//if (getPosition(from)!=null) {
			LocatedEdgePath path = escape(first, r, insideLinks, outsideLinks, !endingAbove, r.l.vertex, toStartWith, endingUp);
			if (path!= null) {
				log.send("Switchback:");
				addToQueue(pq,path);		
			}
//		} else {
//			return;
//		}
	}

	@Override
	protected boolean pathComplete(LocatedEdgePath r) {
		return r instanceof FinishPath;
	}
	

	private EdgePath createStartPath(EdgePath path, Vertex from, int fromi, Going g, PlanarizationSide side, Edge outsideOf) {
		if (path != null) {
			return path;
		}
		
		if (canRouteToVertex(from, outsideOf, side==PlanarizationSide.ENDING_ABOVE, g, false)) {
			return new StartPath(fromi, from, null, side, getPosition(from), g, outsideOf);
		} else {
			return null;
		}
	}

	protected abstract boolean canRouteToVertex(Vertex from, Edge outsideOf, boolean above, Going g, boolean arriving);

	protected void generatePaths(final LocatedEdgePath r, List<Edge> list, State<LocatedEdgePath> pq,  Vertex from, Going g, PlanarizationSide side) {
		EdgePath current = r;
		int s = p.getVertexIndex(from);
		for (int i = list.size() - 1; i >= 0; i--) {
			Edge edge = list.get(i);
			Vertex to = edge.otherEnd(from);
			int e = p.getVertexIndex(to);

			if ((isTerminationVertex(e))) {
				if (canRouteToVertex(to, edge, side == PlanarizationSide.ENDING_ABOVE, g, true) && (canTravel(e, g, side==PlanarizationSide.ENDING_ABOVE))) {
					addToQueue(pq, new FinishPath(e, to, createStartPath(current, from, s, g, side, edge), g, edge));
				}
			} 
			
			addToQueue(pq,
						escape(edge, createStartPath(current, from, s, g, side, edge), getCorrectEdgeSet(s, e, false, to),
								getCorrectEdgeSet(s, e, true, to), true, e, g, g));
			addToQueue(pq,
						escape(edge, createStartPath(current, from, s, g, side, edge), getCorrectEdgeSet(s, e, true, to),
								getCorrectEdgeSet(s, e, false, to), false, e, g, g));
		
			if (r!=null) {
				if (!canCross(edge, current, r.l.p == Place.ABOVE)) {
					return;
				} else {
					current = new StartCrossEdgePath(edge, current, g);
				}
			}

		}

		int nextItem = s + (g == Going.FORWARDS ? 1 : -1);

		if ((nextItem < p.getVertexOrder().size() && (nextItem >= 0))) {
			// generate the inside route
			Vertex vnext = p.getVertexOrder().get(nextItem);
			current = createStartPath(current, from, s, g, side, null);
			
			if (current == null) {
				return;
			}
			
			// above paths
			EdgePath using = current;
			if (side == PlanarizationSide.ENDING_BELOW) {
				using = new PlanarizationCrossPath(nextItem + (g == Going.FORWARDS ? -1 : 0) , current, g, false);
			}
			addToQueue(
					pq,
					escape(null, using, getCorrectEdgeSet(s, nextItem, false, vnext),
							getCorrectEdgeSet(s, nextItem, true, vnext), true, nextItem, g, g));
		

			// below paths
			using = current;
			if (side == PlanarizationSide.ENDING_ABOVE)  {
				using = new PlanarizationCrossPath(nextItem + (g==Going.FORWARDS ? -1 : 0), current, g, false);
			}
			addToQueue(
					pq,
					escape(null, using, getCorrectEdgeSet(s, nextItem, true, vnext),
							getCorrectEdgeSet(s, nextItem, false, vnext), false, nextItem,
							g, g));
			
			if (isTerminationVertex(nextItem)) {
				// generate some finish paths if we are in the right area
				if (canRouteToVertex(vnext, null, true, g, true)) {
					addToQueue(pq, new FinishPath(nextItem, p.getVertexOrder().get(nextItem),  current, g, null));
				}
			}
		}
	}


	
	/**
	 * Returns true if the vertex is somewhere where the route can end.
	 */
	protected abstract boolean isTerminationVertex(int v);

	private List<Edge> getCorrectEdgeSet(int start_pos, int ev_pos, boolean above, Vertex ev) {
		return getCorrectEdgeSet(start_pos, ev_pos, above, ev, p);
	}
	
	
	public static List<Edge> getCorrectEdgeSet(int start_pos, int ev_pos, boolean above, Vertex ev, MGTPlanarization p) {
		return getCorrectEdgeSet(start_pos < ev_pos ? Going.FORWARDS : Going.BACKWARDS, above, ev, p);
	}
	
	public static List<Edge> getCorrectEdgeSet(Going g, boolean above, Vertex ev, MGTPlanarization p) {
		if (g==Going.FORWARDS) {
			if (above) {
				return p.getAboveBackwardLinks(ev);
			} else {
				return p.getBelowBackwardLinks(ev);
			}
		} else {
			if (above) {
				return p.getAboveForwardLinks(ev);
			} else {
				return p.getBelowForwardLinks(ev);
			}
		}
	}
	
	protected int compareFavouredSide(PlanarizationSide side, PlanarizationSide side2) {
		return 0;
	}

}
