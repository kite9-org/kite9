package org.kite9.diagram.common.algorithms.fg;

import java.util.List;

import org.kite9.diagram.common.algorithms.ssp.PathLocation;

/**
 * @author robmoffat
 * 
 */
public class Path implements PathLocation<Path> {

	Node endNode;
	Path nextPathItem;

	public Path getNextPathItem() {
		return nextPathItem;
	}

	Arc route;
	boolean reversed;
	int cost = 0;
	int length = 0;

	public Path(Node n) {
		this.endNode = n;
		route = null;
	}

	/**
	 * Extends an existing path to create a new path
	 */
	public Path(Path p, Arc a, boolean reversed) {
		this.endNode = a.otherEnd(p.endNode);
		this.nextPathItem = p;
		this.route = a;
		this.reversed = reversed;
		this.cost += p.cost + (reversed ? a.getIncrementalCost(-1) : a.getIncrementalCost(+1));
		this.length = p.length + 1;
	}

	/**
	 * Used to say whether this route has joined up with one of the start nodes.
	 */
	public boolean meets(List<Node> destination) {
		return destination.contains(getStartNode());
	}

	public Node getStartNode() {
		if (nextPathItem != null) {
			return nextPathItem.getStartNode();
		} else {
			return endNode;
		}
	}

	public boolean contains(Arc a) {
		if (this.route == a) {
			return true;
		} else if (this.nextPathItem == null) {
			return false;
		} else {
			return nextPathItem.contains(a);
		}
	}

	public boolean contains(Node a) {
		if (this.endNode == a) {
			return true;
		} else if (this.nextPathItem == null) {
			return false;
		} else {
			return nextPathItem.contains(a);
		}
	}

	@Override
	public String toString() {
		return (route == null) ? "" : (reversed ? "!" : "") + route.toString()
				+ (nextPathItem == null ? "" : "/" + nextPathItem.toString())+":"+getCost();
	}

	public int getCost() {
		return cost;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void pushFlow(int flow) {
		if (route != null) {
			route.pushFlow((reversed ? -1 : 1) * flow);
			nextPathItem.pushFlow(flow);
		}
	}

	public int compareTo(Path o) {
		if (this.cost > o.cost) {
			return 1;
		} else if (this.cost < o.cost) {
			return -1;
		} else if (this.length > o.length) {
			return 1;
		} else if (this.length < o.length) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public Object getLocation() {
		return endNode;
		// return null;
	}
	
	private boolean active = true;
	
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean a) {
		this.active = a;
	}

}
