package org.kite9.diagram.common.elements;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;

/**
 * Helper class for implementations of Vertex.  Ids are immutable on each vertex, and 
 * are set on the constructor only.
 */
public abstract class AbstractVertex implements Vertex {

	public String getID() {
		return id;
	}

	/**
	 * Defaults to false, but Arrows and Glyphs override to true
	 */
	public boolean hasDimension() {
		return false;
	}

	List<Edge> edges = new ArrayList<Edge>();
 
	private String id;
	
	public AbstractVertex(String id) {
		this.id = id;
	}

	public int compareTo(Vertex o) {
		return this.toString().compareTo(o.toString());
	}

	public int getEdgeCount() {
		return edges.size();
	}

	public Iterable<Edge> getEdges() {
		if (edges == null) {
			edges = new ArrayList<Edge>();
		}
		return edges;
	}

	public boolean isLinkedDirectlyTo(Vertex v) {
		for (Edge link : getEdges()) {
			if ((link.getFrom() == v) || (link.getTo() == v))
				return true;
		}
		return false;
	}

	public String toString() {
		return "[V:" + getID() + "]";
	}

	public void removeEdge(Edge e) {
		edges.remove(e);
	}
	
	public void addEdge(Edge e) {
		if (!edges.contains(e)) {
			edges.add(e);
		} 
	}
	
	protected Dimension2D position = new Dimension2D();
	
	public double getX() {
		return position.x();
	}
	
	public double getY() {
		return position.y();
	}

	public void setX(double x) {
		position = Dimension2D.setX(position,x);
	}

	public void setY(double y) {
		position = Dimension2D.setY(position, y);
	}

	public Dimension2D getPosition() {
		return position;
	}
	               
	public int hashCode() {
		return id.hashCode();
	}
	
	private RoutingInfo ri;

	@Override
	public RoutingInfo getRoutingInfo() {
		return ri;
	}

	@Override
	public void setRoutingInfo(RoutingInfo gi) {
		this.ri = gi;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return getOriginalUnderlying() == de;
	}

}
