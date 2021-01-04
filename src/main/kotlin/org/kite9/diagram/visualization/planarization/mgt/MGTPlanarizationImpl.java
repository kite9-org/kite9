package org.kite9.diagram.visualization.planarization.mgt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.BasicDimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarizationImpl;
import org.kite9.diagram.logging.LogicException;

public class MGTPlanarizationImpl extends RHDPlanarizationImpl implements MGTPlanarization {

	public MGTPlanarizationImpl(Diagram d, List<Vertex> vertexOrder, Collection<BiDirectional<Connected>> uninsertedConnections, Map<Container, List<Connected>> containerOrderingMap) {
		super(d, containerOrderingMap);
		this.vertexOrder = vertexOrder;
		this.unmodifiableVO = Collections.unmodifiableList(vertexOrder);
		createVertexIndexMap();
		aboveForwardLinks = new ArrayList<List<PlanarizationEdge>>(vertexOrder.size());
		aboveBackwardLinks = new ArrayList<List<PlanarizationEdge>>(vertexOrder.size());
		belowForwardLinks = new ArrayList<List<PlanarizationEdge>>(vertexOrder.size());
		belowBackwardLinks = new ArrayList<List<PlanarizationEdge>>(vertexOrder.size());
		for (int i = 0; i < vertexOrder.size(); i++) {
			LinkedList<PlanarizationEdge> abl = new LinkedList<>();
			LinkedList<PlanarizationEdge> bbl = new LinkedList<>();
			LinkedList<PlanarizationEdge> afl = new LinkedList<>();
			LinkedList<PlanarizationEdge> bfl = new LinkedList<>();
			aboveBackwardLinks.add(abl);
			aboveForwardLinks.add(afl);
			belowBackwardLinks.add(bbl);
			belowForwardLinks.add(bfl);
		}
		this.uninsertedConnections = uninsertedConnections;
	}

	@Override
	public String toString() {
		if (getFaces().size() == 0) {
			return getTextualRepresentation(null).toString();
		} else {
			return super.toString();
		}
	}

	List<Vertex> vertexOrder;
	List<Vertex> unmodifiableVO;

	private Set<PlanarizationEdge> aboveSet = new LinkedHashSet<PlanarizationEdge>();

	private Set<PlanarizationEdge> belowSet = new LinkedHashSet<PlanarizationEdge>();

	public Set<PlanarizationEdge> getAboveLineEdges() {
		return aboveSet;
	}

	public Set<PlanarizationEdge> getBelowLineEdges() {
		return belowSet;
	}

	public Collection<BiDirectional<Connected>> uninsertedConnections;

	private List<List<PlanarizationEdge>> aboveForwardLinks;
	private List<List<PlanarizationEdge>> aboveBackwardLinks;
	private List<List<PlanarizationEdge>> belowForwardLinks;
	private List<List<PlanarizationEdge>> belowBackwardLinks;

	public List<PlanarizationEdge> getAboveForwardLinks(Vertex v) {
		return aboveForwardLinks.get(getVertexIndex(v));
	}

	public List<PlanarizationEdge> getAboveBackwardLinks(Vertex v) {
		return aboveBackwardLinks.get(getVertexIndex(v));
	}

	public List<PlanarizationEdge> getBelowForwardLinks(Vertex v) {
		return belowForwardLinks.get(getVertexIndex(v));
	}

	public List<PlanarizationEdge> getBelowBackwardLinks(Vertex v) {
		return belowBackwardLinks.get(getVertexIndex(v));
	}

	public List<Vertex> getVertexOrder() {
		return unmodifiableVO;
	}

	public List<Edge> getAllEdges() {
		List<Edge> out = new ArrayList<Edge>();
		out.addAll(aboveSet);
		out.addAll(belowSet);
		return out;
	}

	/**
	 * This does not output crossing edges or on line edges
	 */
	public TextualRepresentation getTextualRepresentation(Set<DiagramElement> highlight) {
		TextualRepresentation tr = new TextualRepresentation();

		// set up vertex positions in the textual rep.
		int voi = 0;
		int lastPos = 0;
		for (Vertex b : vertexOrder) {
			tr.getPositions().put(b, new BasicDimension2D(lastPos, 0));
			String name = b.getID() + "[" + (voi++) + "]";
			lastPos += name.length() + 2;
		}
		tr.setLength(lastPos);

		voi = 0;
		for (Vertex v : vertexOrder) {
			tr.outputString(0, (int) tr.getPositions().get(v).x(), v.getID() + "[" + (voi++) + "]");
		}

		Map<PlanarizationEdge, Integer> nestings = new HashMap<PlanarizationEdge, Integer>(aboveSet.size() * 2);
		for (PlanarizationEdge e : aboveSet) {
			Vertex from = e.getFrom();
			Vertex to = e.getTo();
			int fromi = (int) tr.getPositions().get(from).x();
			int toi = (int) tr.getPositions().get(to).x();
			if (fromi > toi) {
				int temp = toi;
				toi=fromi;
				fromi=temp;
			}
			int height = getNestings(e, aboveSet, nestings) + 1;
			boolean hl = isPartOf(highlight, e);
			tr.vLine(-1, fromi, -height, hl);
			tr.vLine(-1, toi, -height, hl);
			tr.hLine(-height - 1, fromi + 1, toi - 1, hl);
		}

		nestings = new HashMap<PlanarizationEdge, Integer>(belowSet.size() * 2);
		for (PlanarizationEdge e : belowSet) {
			Vertex from = e.getFrom();
			Vertex to = e.getTo();
			int fromi = (int) tr.getPositions().get(from).x();
			int toi = (int) tr.getPositions().get(to).x();
			if (fromi > toi) {
				int temp = toi;
				toi=fromi;
				fromi=temp;
			}
			int height = getNestings(e, belowSet, nestings) + 1;
			boolean hl = isPartOf(highlight, e);

			tr.vLine(1, fromi, height, hl);
			tr.vLine(1, toi, height, hl);
			tr.hLine(height + 1, fromi + 1, toi - 1, hl);
		}

		return tr;
	}

	private boolean isPartOf(Set<DiagramElement> highlight, Edge e) {
		if (highlight == null) {
			return false;
		}
		for (DiagramElement de : highlight) {
			if (e.isPartOf(de)) {
				return true;
			}
		}
		return false;
	}

	public int getNestings(PlanarizationEdge e, Set<PlanarizationEdge> aboveSet, Map<PlanarizationEdge, Integer> nestCache) {
		Integer out = nestCache.get(e);
		if (out != null) {
			return out;
		}

		int nestings = 0;
		int from = vertexOrder.indexOf(e.getFrom());
		int to = vertexOrder.indexOf(e.getTo());
		if (from > to) {
			int temp = to;
			to=from;
			from=temp;
		}

		for (PlanarizationEdge edge : aboveSet) {
			if (e != edge) {
				int fromi = vertexOrder.indexOf(edge.getFrom());
				int toi = vertexOrder.indexOf(edge.getTo());
				if (fromi > toi) {
					int temp = toi;
					toi=fromi;
					fromi=temp;
				}

				if (((fromi >= from) && (toi <= to)) && (!((fromi == from) && (toi == to)))) {
					nestings = Math.max(nestings, getNestings(edge, aboveSet, nestCache) + 1);
				}
			}
		}

		nestCache.put(e, nestings);

		return nestings;

	}

	public Collection<BiDirectional<Connected>> getUninsertedConnections() {
		return uninsertedConnections;
	}

	public void removeEdge(Edge cross) {
		boolean found = aboveSet.remove(cross) || belowSet.remove(cross);
		int fromvi = getVertexIndex(cross.getFrom());
		int tovi = getVertexIndex(cross.getTo());
		
		if ((fromvi == -1) || (tovi == -1)) {
			throw new LogicException();
		}
		
		// remove one end
		boolean found1 = 
		aboveForwardLinks.get(fromvi).remove(cross) ||
		belowForwardLinks.get(fromvi).remove(cross) ||
		aboveBackwardLinks.get(fromvi).remove(cross) ||
		belowBackwardLinks.get(fromvi).remove(cross);
		
		boolean found2 = 
				aboveForwardLinks.get(tovi).remove(cross) ||
				belowForwardLinks.get(tovi).remove(cross) ||
				aboveBackwardLinks.get(tovi).remove(cross) ||
				belowBackwardLinks.get(tovi).remove(cross);
		
		if (!found) { 
			throw new LogicException();
		}
	}

	private Map<Vertex, Integer> vertexIndex = null;

	private void createVertexIndexMap() {
		vertexIndex = new HashMap<Vertex, Integer>();
		int i = 0;
		for (Vertex vertex : vertexOrder) {
			vertexIndex.put(vertex, i);
			i++;
		}
	}

	public void addVertexToOrder(int i, Vertex insert) {
		vertexOrder.add(i + 1, insert);
		aboveBackwardLinks.add(i + 1, new LinkedList<PlanarizationEdge>());
		aboveForwardLinks.add(i + 1, new LinkedList<PlanarizationEdge>());
		belowBackwardLinks.add(i + 1, new LinkedList<PlanarizationEdge>());
		belowForwardLinks.add(i + 1, new LinkedList<PlanarizationEdge>());
		createVertexIndexMap();
	}

	public int getVertexIndex(Vertex v) {
		Integer i = vertexIndex.get(v);
		if (i == null) {
			return -1;
		} else {
			return i;
		}
	}

	public void removeVertexFromOrder(Vertex v) {
		vertexOrder.remove(v);
		createVertexIndexMap();
	}

	public boolean isAdjacency(Edge edge) {
		int v1 = getVertexIndex(edge.getFrom());
		int v2 = getVertexIndex(edge.getTo());
		return (Math.abs(v1 - v2) <= 1);
	}

	public boolean crosses(Edge edge, boolean above) {
		Vertex v1 = edge.getFrom();
		Vertex v2 = edge.getTo();
		int i1 = getVertexIndex(v1);
		int i2 = getVertexIndex(v2);
		return crosses(i1, i2, above);
	}

	public boolean crosses(float i1, float i2, boolean above) {
		for (Edge edge2 : above ? aboveSet : belowSet) {
			if (crosses(i1, i2, getVertexIndex(edge2.getFrom()), getVertexIndex(edge2.getTo()))) {
				return true;
			}
		}
		return false;
	}

	protected boolean crosses(float i1, float i2, float j1, float j2) {
		if ((i1 == j1) || (i1 == j2) || (i2 == j1) || (i2 == j2))
			return false;

		if (within(i1, j1, j2) != within(i2, j1, j2)) {
			return true;
		}

		return false;
	}

	protected boolean within(float i1, float j1, float j2) {
		float ja = Math.min(j1, j2);
		float jb = Math.max(j1, j2);
		return ((i1 > ja) && (i1 < jb));
	}

	public void addEdge(PlanarizationEdge edge, boolean above, PlanarizationEdge outsideOf) {
		int fromi = getVertexIndex(edge.getFrom());
		int toi = getVertexIndex(edge.getTo());
		if (fromi > toi) {
			int temp = toi;
			toi=fromi;
			fromi=temp;
		}

		if ((outsideOf != null) && (!outsideOf.meets(edge.getFrom()) || !outsideOf.meets(edge.getTo()))) {
			outsideOf = null;
		}

		if (above) {
			aboveSet.add(edge);
			orderedInsert(aboveForwardLinks.get(fromi), edge, outsideOf);
			orderedInsert(aboveBackwardLinks.get(toi), edge, outsideOf);
		} else {
			belowSet.add(edge);
			orderedInsert(belowForwardLinks.get(fromi), edge, outsideOf);
			orderedInsert(belowBackwardLinks.get(toi), edge, outsideOf);
		}
		boolean contradicting = Tools.isUnderlyingContradicting(edge);
		checkEdgeOrdering(edge.getFrom(), edge.getDrawDirectionFrom(edge.getFrom()), contradicting);
		checkEdgeOrdering(edge.getTo(), edge.getDrawDirectionFrom(edge.getTo()), contradicting);

		checkOrderingAround(edge.getFrom());
		checkOrderingAround(edge.getTo());
	}

	private void checkEdgeOrdering(Vertex from, Direction direction, boolean contradicting) {
		// first, fix the vertex ordering
		VertexEdgeOrdering eo = (VertexEdgeOrdering) getEdgeOrderings().get(from);
		if (eo==null) {
			eo = new MGTVertexEdgeOrdering(this, from);
			getEdgeOrderings().put(from, eo);
			eo.addEdgeDirection(direction, contradicting);
		} else {
			eo.addEdgeDirection(direction, contradicting);
			eo.changed();
		}
	}

	private void checkOrderingAround(Vertex from) { 
		List<Edge> byQuad = new ArrayList<Edge>(); 
		List<PlanarizationEdge> af = getAboveForwardLinks(from);
		List<PlanarizationEdge> bf = getBelowForwardLinks(from);
		List<PlanarizationEdge> bb = getBelowBackwardLinks(from);
		List<PlanarizationEdge> ab = getAboveBackwardLinks(from);
		List<Edge> c1 = new ArrayList<Edge>(af);
		Collections.reverse(c1);
		byQuad.addAll(c1);
		byQuad.addAll(bf);
		c1 = new ArrayList<Edge>(bb);
		Collections.reverse(c1);
		byQuad.addAll(c1);
		byQuad.addAll(ab);

		List<PlanarizationEdge> cmp = getEdgeOrderings().get(from).getEdgesAsList();
		int i = byQuad.indexOf(cmp.get(0));
		Collections.rotate(byQuad, -i);

		if (!cmp.equals(byQuad)) {
			throw new LogicException("Collections not same: \n" + cmp + "\n" + byQuad);
		}

	}

	public PlanarizationEdge getFirstEdgeAfterPlanarizationLine(Vertex from, boolean forward, boolean above) {
		PlanarizationEdge outsideOf;
		boolean clockwise = !(above == forward);
		Quadrant q = getQuadrantFor(above, forward);
		List<PlanarizationEdge> edgeSet = getEdgeSetByQuadrant(q, from);
		while (edgeSet.size() == 0) {
			int ord = (q.ordinal() + (clockwise ? -1 : 1) + 4) % 4;
			q = Quadrant.values()[ord];
			edgeSet = getEdgeSetByQuadrant(q, from);
		}

		outsideOf = q.clockwise == clockwise ? edgeSet.get(0) : edgeSet.get(edgeSet.size() - 1);
		return outsideOf;
	}

	private Quadrant getQuadrantFor(boolean above, boolean forward) {
		if (above) {
			return forward ? Quadrant.ABOVE_FORWARD : Quadrant.ABOVE_BACKWARD;
		} else {
			return forward ? Quadrant.BELOW_FORWARD : Quadrant.BELOW_BACKWARD;
		}
	}

	enum Quadrant {
		ABOVE_FORWARD(false), ABOVE_BACKWARD(true), BELOW_BACKWARD(false), BELOW_FORWARD(true);

		private boolean clockwise;

		private Quadrant(boolean clockwise) {
			this.clockwise = clockwise;
		}
	}

	public List<PlanarizationEdge> getEdgeSetByQuadrant(Quadrant quadrant, Vertex v) {
		switch (quadrant) {
		case ABOVE_BACKWARD:
			return getAboveBackwardLinks(v);
		case ABOVE_FORWARD:
			return getAboveForwardLinks(v);
		case BELOW_BACKWARD:
			return getBelowBackwardLinks(v);
		case BELOW_FORWARD:
		default:
			return getBelowForwardLinks(v);
		}
	}

	private void orderedInsert(List<PlanarizationEdge> abl, PlanarizationEdge e, PlanarizationEdge outsideOf) {
		int o1d = edgeSpan(e);
		ListIterator<PlanarizationEdge> li = abl.listIterator();
		while (li.hasNext()) {
			PlanarizationEdge n = li.next();
			int o2d = edgeSpan(n);
			if ((o2d > o1d) || ((o2d == o1d) && (outsideOf == null))) {
				li.previous();
				li.add(e);
				return;
			}
			if ((o2d == o1d) && (outsideOf == n)) {
				li.add(e);
				return;
			}
		}

		li.add(e);
	}

	private int edgeSpan(Edge e) {
		return Math.abs(getVertexIndex(e.getFrom()) - getVertexIndex(e.getTo()));
	}
}
