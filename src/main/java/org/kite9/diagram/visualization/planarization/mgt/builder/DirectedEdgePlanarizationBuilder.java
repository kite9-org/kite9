package org.kite9.diagram.visualization.planarization.mgt.builder;

import java.util.Iterator;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.router.CrossingType;
import org.kite9.diagram.visualization.planarization.mgt.router.EdgeRouter;
import org.kite9.diagram.visualization.planarization.mgt.router.GeographyType;
import org.kite9.diagram.visualization.planarization.mgt.router.MGTEdgeRouter;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.framework.logging.LogicException;

/**
 * This supplies extra logic to insert edges into the planarization. These are
 * done in the order they are held in the planarization's uninserted edges list.
 * 
 * Those that cannot be inserted (having a given direction) are inserted again
 * with their direction relaxed.
 * 
 * @author robmoffat
 * 
 */
public abstract class DirectedEdgePlanarizationBuilder extends
		MGTPlanarizationBuilder {

	public DirectedEdgePlanarizationBuilder(ElementMapper em, GridPositioner gp) {
		super(em, gp);
	}
	
	private EdgeRouter er;

	protected EdgeRouter getEdgeRouter() {
		if (er == null) {
			er = new MGTEdgeRouter(getRoutableReader(), em);
		}
		return er;
	}


	enum EdgePhase {
		SINGLE_DIRECTION, SINGLE_DIRECTION_CONTRADICTORS, FORWARDS_DIRECTIONS,  RELAXED_DIRECTIONS
	};

	public final void processConnections(MGTPlanarization p) {
		log.send("Plan: "+p.toString());
		log.send("Adding Connections: ", p.getUninsertedConnections());

		int count = processCorrectDirectedConnections(p);
		count = processMarkedContradictingConnections(p, count);
		count = processForwardDirectionConnections(p, count);
		count = processRelaxedDirectionConnections(p, count);

		int left = p.getUninsertedConnections().size();
		if (left > 0) {
			log.error("Failed to add " + left + " connections:");
			for (BiDirectional<Connected> e : p.getUninsertedConnections()) {
				log.error(e.toString());
				EdgeMapping redundant = p.getEdgeMappings().get(e);
				if (redundant != null) {
					for (Edge edge : redundant.getEdges()) {
						((PlanarizationEdge) edge).remove();
					}
				}
			}
		} else {
			log.send("Added " + count + " connections");
		}
	}

	protected int processRelaxedDirectionConnections(MGTPlanarization p, int count) {
		return addAllItems(p, EdgePhase.RELAXED_DIRECTIONS, count);
	}

	protected int processMarkedContradictingConnections(MGTPlanarization p, int count) {
		return addAllItems(p, EdgePhase.SINGLE_DIRECTION_CONTRADICTORS, count);
	}
	
	protected int processForwardDirectionConnections(MGTPlanarization p, int count) {
		return addAllItems(p, EdgePhase.FORWARDS_DIRECTIONS, count);
	}

	protected int processCorrectDirectedConnections(MGTPlanarization p) {
		return addAllItems(p, EdgePhase.SINGLE_DIRECTION, 0);
	}

	private int addAllItems(MGTPlanarization p, EdgePhase ep, int runningCount) {
		int count = runningCount;
		int max = p.getUninsertedConnections().size()+runningCount; //10;
		for (Iterator<BiDirectional<Connected>> iterator = p
				.getUninsertedConnections().iterator(); iterator.hasNext();) {
			BiDirectional<Connected> c = iterator.next();
			boolean done = false;

			if (count < max) {
				done = handleInsertionPhase(p, ep, c);
			}

			if (done) {
				iterator.remove();
				count++;
			}

		}

		return count;
	}

	private boolean handleInsertionPhase(MGTPlanarization p, EdgePhase ep, BiDirectional<Connected> c) {
		PlanarizationEdge e = getEdgeForConnection(c, p);
		
		boolean done = false;

		boolean contradicting = (c instanceof Connection) && (Tools.isConnectionContradicting((Connection)c));
		boolean directed = c.getDrawDirection()!=null;
		boolean removable = GroupPhase.isRemoveableLink(c);
		
		switch (ep) {
		case SINGLE_DIRECTION:
			if ((!contradicting) && (directed)) {
				done = er.addPlanarizationEdge(p, e, e.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
			}

			break;
		case SINGLE_DIRECTION_CONTRADICTORS:
			if (contradicting && directed) {
				// have a go at getting the connections in, on the off chance they will fit.
				done = er.addPlanarizationEdge(p, e, e.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
				
				if (done) {
					Tools.setUnderlyingContradiction(e, false);
				}
			}
			break;
		case FORWARDS_DIRECTIONS:
			if ((!contradicting) && (!directed)) {
				// stops edges wrapping round containers with layout - go in a straight line
				Container comm = getCommonContainer(c.getFrom(), c.getTo());
				Direction d = null;
				if ((comm.getLayout()!=null) && (comm.getLayout() != Layout.GRID)) {
					d = getInsertionDirection(comm.getLayout(), e.getFrom(), e.getTo());
					done = er.addPlanarizationEdge(p, e, d, CrossingType.NOT_BACKWARDS, GeographyType.RELAXED);
				}
			}

			break;
		case RELAXED_DIRECTIONS:
			if (e.getDrawDirection()!=null) {
				Tools.setUnderlyingContradiction(e, true);
			}
			
			if (!removable) {
				done = er.addPlanarizationEdge(p, e, null, CrossingType.UNDIRECTED, GeographyType.RELAXED);
			}
			
			break;
		}
		
		if (!done) {
			p.getEdgeMappings().remove(c);
		}
		return done;
	}
	
	private Direction getInsertionDirection(Layout layoutDirection, Vertex from, Vertex to) {
		switch (layoutDirection) {
		case LEFT:
		case RIGHT:
		case HORIZONTAL:
			return from.getRoutingInfo().centerX() < to.getRoutingInfo().centerX() ? Direction.RIGHT : Direction.LEFT;
		case UP:
		case DOWN:
		case VERTICAL:
			return from.getRoutingInfo().centerY() < to.getRoutingInfo().centerY() ? Direction.DOWN : Direction.UP;
		}
		
		throw new LogicException("Couldn't determine direction to insert in");
	}

	protected abstract PlanarizationEdge getEdgeForConnection(BiDirectional<Connected> c, MGTPlanarization p);
}
