package org.kite9.diagram.common.algorithms.fg;

import org.kite9.diagram.logging.LogicException;

abstract class AbstractArc implements Arc {

	protected int flow = 0;
	protected Node from;
	protected Node to;
	protected String label;

	public AbstractArc() {
		super();
	}

	public int getFlow() {
		return flow;
	}

	public int getFlowFrom(Node n) {
		if (n==from) {
			return flow;
		} else if (n==to) {
			return -flow;
		} else {
			throw new LogicException("Node not joined to arc");
		}
	}

	public void pushFlow(int flow) {
		this.flow += flow;
		from.pushFlow(-flow);
		to.pushFlow(flow);
	}

	public void setFlow(int flow) {
		this.flow = flow;
	}

	public Node getFrom() {
		return from;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

	public Node getTo() {
		return to;
	}

	public void setTo(Node to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return label;
	}

	public Node otherEnd(Node n) {
		if (from==n) {
			return to;
		}
		if (to==n)
			return from;
		return null;
	}

	@Override
	public int hashCode() {
		return label.hashCode();
	}

	private boolean blocked = false;
	
	@Override
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	@Override
	public boolean isBlocked() {
		return blocked;
	}
	
	@Override
	public String getID() {
		return label;
	}

}