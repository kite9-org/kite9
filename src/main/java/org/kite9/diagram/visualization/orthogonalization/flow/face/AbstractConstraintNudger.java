package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.LinearArc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.StateStorage;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.logging.Table;

/**
 * Contains the basic code for testing nudges out, splitting the flow graph into halves, logging the results of a nudge and working out
 * how many corners are needed for a particular nudge.
 * @author robmoffat
 *
 */
public abstract class AbstractConstraintNudger implements Logable, ConstraintNudger {
	
	protected class NudgeChoice {

		MappedFlowGraph fg;

		protected NudgeChoice(MappedFlowGraph fg, Map<Object, Integer> stateBefore, int corners, NudgeItem ni,
				int constraintNumber, Collection<SubdivisionNode> subdivisions,
				ConstrainedSSP ssp) {
			super();
			this.fg = fg;
			this.stateBefore = stateBefore;
			this.corners = corners;
			this.ni = ni;
			this.constraintNumber = constraintNumber;
			this.subdivisions = subdivisions;
			this.ssp = ssp;
			this.note = constraintNumber + " nudge = " + ni.id + " corners: " + corners;
		}

		Map<Object, Integer> stateBefore;
		Map<Object, Integer> stateAfter;
		Integer cost;
		int corners;
		NudgeItem ni;
		int constraintNumber;
		Collection<SubdivisionNode> subdivisions;
		ConstrainedSSP ssp;
		String note;

		public int evaluate() {
			if (cost == null) {
				if (corners == 0) {
					stateAfter = stateBefore;
					cost = 0;
				} else {
					StateStorage.restoreState(fg, stateBefore);
					cost = introduceConstraints(fg, ni, constraintNumber, corners, note, ni.source, ni.sink, subdivisions,
							ssp);
					stateAfter = StateStorage.storeState(fg);
				}
			}

			return cost;
		}

		public void apply() {
			if (stateAfter == null) {
				evaluate();
			}

			StateStorage.restoreState(fg, stateAfter);
		}

		public String getNote() {
			return note;
		}

	}

	protected Kite9Log log = new Kite9Log(this);

	public static void createSourceArc(Node source, MappedFlowGraph fg, Node c) {
		LinearArc a = new LinearArc(AbstractFlowOrthogonalizer.TRACE, Integer.MAX_VALUE, 0, source, c, source
				.getId()
				+ "-" + c.getId());
		fg.getAllArcs().add(a);
	
		a = new LinearArc(AbstractFlowOrthogonalizer.TRACE, Integer.MAX_VALUE, 0, c, source, c.getId() + "-"
				+ source.getId());
		fg.getAllArcs().add(a);
	}

	protected Collection<Node> getTableLogNodes(MappedFlowGraph fg) {
		List<Node> out = new ArrayList<Node>();
		for (Node n : fg.getAllNodes()) {
			if (n instanceof PortionNode) {
				out.add(n);
			}
		}
		return out;
	}
	
	protected Collection<NudgeItem> createRouteList(ConstraintGroup constraintGroup, MappedFlowGraph fg) {
		Collection<NudgeItem> out = new TreeSet<NudgeItem>(new Comparator<NudgeItem>() {

			@Override
			public int compare(NudgeItem o1, NudgeItem o2) {
				if (o1.type!=o2.type) {
					return o1.type.compareTo(o2.type);
				} else {
					return ((Integer)o1.id).compareTo(o2.id);
				}
			}
		});
//		Collection<NudgeItem> out = new LinkedList<NudgeItem>();
		
		int id = 0;
		for (Route r : constraintGroup.getRequiredRoutes()) {
			try {
				NudgeItem ni = new NudgeItem(id++, r);
				ni.faceCount = r.size();
				ni.portionsClockwise = getPortionsForConstraint(fg, r);
				ni.portionsAntiClockwise = getOppositePortions(fg, ni.portionsClockwise, r);
				ni.calculateType();
				out.add(ni);
			} catch (Exception e) {
				throw new LogicException("Could not convert route to nudge item: "+r, e);
			}
		}
	
		return out;
	}


	protected NudgeItem getNextNudgeItem(Collection<NudgeItem> routes) {
		Iterator<NudgeItem> i = routes.iterator();
		NudgeItem out = i.next();
		i.remove();
		return out;
	}
	
	protected void undivideNodes(Collection<SubdivisionNode> subdivisions, List<Pair<SubdivisionNode>> splits, int constraintNo) {
		for (Pair<SubdivisionNode> pair : splits) {
			SubdivisionNode a = pair.getA();
			SubdivisionNode b = pair.getB();
			log.send(log.go() ? null : "Undividing: " + a + " with " + b);
			a.merge(b);
			subdivisions.remove(b);
			log.send(log.go() ? null : "Merged node: " + a);
		}
		
		String aName = "("+constraintNo+"A)";
		String bName = "("+constraintNo+"B)";
		
		
		for (SubdivisionNode sn : subdivisions) {
			String subdivision = sn.getSubdivision();
			if (subdivision.endsWith(aName)) {
				sn.setSubdivision(subdivision.substring(0, subdivision.length() - aName.length()));
			} else if (subdivision.endsWith(bName)) {
				sn.setSubdivision(subdivision.substring(0, subdivision.length() - bName.length()));
			}
		}
	}

	/**
	 * Where a subdivision node meets a portion in clockwise and anti-clockwise,
	 * split the subdivision node so that this is no longer the case
	 */
	protected void subdivideNodes(Collection<SubdivisionNode> subdivisions, List<PortionNode> portionsClockwise, List<PortionNode> portionsAntiClockwise, List<Pair<SubdivisionNode>> splits, int constraintNo, MappedFlowGraph fg) {
		Collection<SubdivisionNode> in = new ArrayList<SubdivisionNode>(subdivisions);
	
		for (SubdivisionNode sn : in) {
			boolean meetsClock = sn.meets(portionsClockwise);
			boolean meetsAnti = sn.meets(portionsAntiClockwise);
			String subdivision = sn.getSubdivision();
			
			if ((meetsClock) && (meetsAnti)) {
				SubdivisionNode bPart = sn.split(portionsClockwise, portionsAntiClockwise, constraintNo);
				Pair<SubdivisionNode> newPair = new Pair<SubdivisionNode>(sn, bPart);
				splits.add(newPair);
				subdivisions.add(bPart);				
				sn.setSubdivision(subdivision+"("+constraintNo+"A)");
				bPart.setSubdivision(subdivision+"("+constraintNo+"B)");
			} else if (meetsAnti) {
				sn.setSubdivision(subdivision+"("+constraintNo+"B)");
			} else if (meetsClock) {
				sn.setSubdivision(subdivision+"("+constraintNo+"A)");
			}
		}
	}

	protected int introduceConstraints(MappedFlowGraph fg, NudgeItem ni, int constraintNumber, int corners, String note,
			Node source, Node sink, Collection<SubdivisionNode> subs, ConstrainedSSP ssp) {
			
				// this will contain constraints for clockwise and anticlockwise
				// portions respectively
				try {
					int cost = 0;
					if (corners != 0) {
						addSourceAndSink(ni.portionsClockwise, source, ni.portionsAntiClockwise, sink, fg, subs);
						
						getReachable(source, sink);
						log.send(log.go() ? null : "Nudge Number: " + note);
						initializePortionSupplies(source, sink, corners);
						cost = ssp.maximiseFlow(fg);
						removeSourceAndSink(fg, source, sink);
					}
					return cost;
				} catch (LogicException e) {
					removeSourceAndSink(fg, source, sink);
					return Integer.MAX_VALUE;
				}
			}

	protected void checkFlowGraphIntegrity(MappedFlowGraph fg, Node source, Node sink) {
		for (Node n : fg.getAllNodes()) {
			if ((n.getSupply() != -n.getFlow()) && (n != source) && (n != sink)) {
				throw new LogicException("Flow graph in inconsistent state! " + n);
			}
		}
	}

	protected Map<String, String> displaySubdivisions(Collection<SubdivisionNode> subs) {
		Map<String, String> out = new HashMap<String, String>();
		for (SubdivisionNode subdivisionNode : subs) {
			String sns = out.get(subdivisionNode.getSubdivision());
			if (sns == null) {
				sns = "\n";
			}
			
			sns += "\t\t"+subdivisionNode.getId()+" --- ";
			Set<Node> to = new DetHashSet<Node>();
			for (Arc a : subdivisionNode.getArcs()) {
				Node otherEnd = a.otherEnd(subdivisionNode);
				to.add(otherEnd);
			}
			
			sns += to.toString()+"\n";
			
			out.put(subdivisionNode.getSubdivision(), sns);
		}
	
		return out;
	}

	private void removeSourceAndSink(MappedFlowGraph fg, Node source, Node sink) {
		AbstractFlowOrthogonalizer.removeArcs(fg, source);
		AbstractFlowOrthogonalizer.removeArcs(fg, sink);
		fg.getAllNodes().remove(source);
		fg.getAllNodes().remove(sink);
	}

	/**
	 * Sets up clockwise portions as sources and anticlockwise as sinks. All
	 * other portions get set to zero.
	 */
	private void addSourceAndSink(List<PortionNode> clock, Node source, List<PortionNode> anti, Node sink, MappedFlowGraph fg, Collection<SubdivisionNode> subs) {
	
		for (SubdivisionNode subdivisionNode : subs) {
			boolean done = false;
	
			if (subdivisionNode.meets(anti)) {
				createSourceArc(sink, fg, subdivisionNode);
				done = true;
			}
	
			if (subdivisionNode.meets(clock)) {
				if (done) {
					throw new LogicException("Should not meet clock and anti!");
				} else {
					createSourceArc(source, fg, subdivisionNode);
				}
			}
		}
		
		fg.getAllNodes().add(source);
		fg.getAllNodes().add(sink);
	}

	private void initializePortionSupplies(Node source, Node sink, int corners) {
		source.setFlow(0);
		source.setSupply(corners);
		sink.setFlow(0);
		sink.setSupply(-corners);
	}

	Map<Face, List<PortionNode>> facePortionMap;
	
	public AbstractConstraintNudger(Map<Face, List<PortionNode>> facePortionMap) {
		super();
		this.facePortionMap = facePortionMap;
	}

	protected void logSizes(Collection<Node> logNodes, Table nudges, String note, String bestCost, String worstCost) {
		int[] portionSizes = new int[logNodes.size()];
		int i = 0;
		for (Node p : logNodes) {
			portionSizes[i++] = countPortionCorners(p);
		}
	
		nudges.addRow("", portionSizes, note, bestCost, worstCost);
		// log.send(log.go() ? null : "Nudges: ",nudges);
	}

	protected void unlogSizes(Table nudges) {
		nudges.removeLastRow();
	}

	protected int calculateCornersRequired(NudgeItem ni, boolean bestDirection, boolean logs) {
	
		Face startFace = ni.getFirstFace();
		int startEdge = (ni.portionsClockwise.get(0)).getEdgeStartPosition();
	
		Face endFace = ni.getLastFace();
		int endEdge = (ni.portionsClockwise.get(ni.portionsClockwise.size() - 1)).getEdgeEndPosition();
	
		Direction firstEdge = getClockwiseDirection(startEdge, startFace);
		Direction lastEdge = getClockwiseDirection(endEdge, endFace);
		int cornersClockwise = countRequiredCorners(firstEdge, lastEdge, true);
	
		// add on face-crossing costs
		int faceCost = (ni.faceCount - 1) * 2;
		cornersClockwise += faceCost;
	
		// work out what the corner count is
		int actualClockwise = countActualCorners(ni.portionsClockwise);
	
		// work out how we require it to change to meet the constraint
		int clockChange = cornerChange(cornersClockwise, actualClockwise, bestDirection);
		int antiChange = -clockChange;
	
		if (logs) {
			log.send(log.go() ? null : ni.id + " starts " + startFace.getId() + "/" + startEdge + "/" + firstEdge + " ends "
					+ endFace.getId() + "/" + endEdge + "/" + lastEdge + ", requires portions: " + ni.portionsClockwise
					+ " corners " + clockChange + " ( currently : " + actualClockwise + ", needed: " + cornersClockwise
					+ " )");
			log.send(log.go() ? null : "Opposite route requires portions: " + ni.portionsAntiClockwise + " corners " + antiChange);
		}
	
		return clockChange;
	}

	/**
	 * Works out what the number of corners should be, given the actual.
	 */
	private int cornerChange(int requiredMod, int actualClockwise, boolean bestDirection) {
		int actualMod = (actualClockwise + 4) % 4;
		requiredMod = (requiredMod + 4) % 4;
	
		int c1, c2;
	
		if (requiredMod == actualMod) {
			return 0;
		}
	
		if (requiredMod < actualMod) {
			c1 = requiredMod - actualMod;
			c2 = requiredMod + 4 - actualMod;
		} else {
			c1 = requiredMod - 4 - actualMod;
			c2 = requiredMod - actualMod;
		}
	
		if (bestDirection) {
			return Math.abs(c1) < Math.abs(c2) ? c1 : c2;
		} else {
			return Math.abs(c1) >= Math.abs(c2) ? c1 : c2;
		}
	
	}

	public int countPortionCorners(Node n) {
		int count = 0;
		for (Arc a : n.getArcs()) {
			Node otherEnd = a.otherEnd(n);
			if (!(otherEnd.getType() == ConstrainedFaceFlowOrthogonalizer.FACE_SUBDIVISION_NODE)) {
				int flow = a.getFlowFrom(n);
				count += flow;
				//log.send(log.go() ? null : "Flow on "+a+" is "+flow);
			}
		}
	
		//log.send(log.go() ? null : "Total Flow on "+n+" = "+count);
		return count;
	}

	private int countActualCorners(List<PortionNode> portionsClockwise) {
		int count = 0;
		for (PortionNode variable : portionsClockwise) {
			count += countPortionCorners(variable);
		}
	
		return count;
	}

	private List<PortionNode> getPortionsForConstraint(MappedFlowGraph fg, Route r) {
		List<PortionNode> portionsInvolved = new ArrayList<PortionNode>();
	
		while (r != null) {
			int in = r.getIn();
			int out = r.getOut();
			Face f = r.getFace();
			List<PortionNode> p = getMatchingPortions(facePortionMap.get(f), out, in, f.isOuterFace());
			portionsInvolved.addAll(p);
			r = r.getRest();
		}
		return portionsInvolved;
	}

	private List<PortionNode> getMatchingPortions(List<PortionNode> list, int start, int end, boolean outerFace) {
		List<PortionNode> out = new ArrayList<PortionNode>(list.size());
		
		for (int i = 0; i < list.size(); i++) {
			PortionNode portionNode = list.get(i);
			if (portionNode.getEdgeStartPosition() == start) {
				// found starting point
				for (int j = 0; j < list.size(); j++) {
					PortionNode toAdd = list.get((i+j) % list.size());
					out.add(toAdd);
					if (toAdd.getEdgeEndPosition() == end) {
						return out;
					}
				}
			}	
		}
		
		throw new LogicException("Could not find portion between "+start+" and "+end);
	}

	/**
	 * Returns the portions not included in portionsClockwise from the
	 * implicated faces. This is then used as the anti-clockwise route
	 */
	private List<PortionNode> getOppositePortions(MappedFlowGraph fg, List<PortionNode> portionsClockwise, Route r) {
		List<PortionNode> portionsInvolved = new ArrayList<PortionNode>();
		while (r != null) {
			Face f = r.getFace();
			r = r.getRest();
			portionsInvolved.addAll(facePortionMap.get(f));
		}

		portionsInvolved.removeAll(portionsClockwise);
		return portionsInvolved;
	}

	/**
	 * Returns the direction of an edge wrt a face.
	 * 
	 * @param constrainedEdge
	 * @param f
	 * @return
	 */
	private Direction getClockwiseDirection(int index, Face f) {
		Edge constrainedEdge = f.getBoundary(index);
		Vertex fromVertex = f.getCorner(index);
		Direction out = constrainedEdge.getDrawDirectionFrom(fromVertex);
		if (out==null) {
			throw new LogicException("Was expecting a constrained edge: "+constrainedEdge);
		}
		return out;
	}

	/**
	 * Counts number of corners in a clockwise direction needed to get from da
	 * to db.
	 */
	private int countRequiredCorners(Direction da, Direction db, boolean clockwise) {
		int corners = 0;
		while (da != db) {
			corners++;
			if (clockwise)
				da = Direction.rotateClockwise(da);
			else
				da = Direction.rotateAntiClockwise(da);
		}
		return corners;
	}

	public String getPrefix() {
		return "NUDG";
	}

	public boolean isLoggingEnabled() {
		return false;
	}
	
	public void getReachable(Node source, Node sink) {
		Set<Node> foundSource = new DetHashSet<Node>();
		Set<Node> foundSink = new DetHashSet<Node>();
		reach(source, foundSource, true);
		reach(sink, foundSink, false);
		Set<Node> common = new DetHashSet<Node>(foundSink);
		common.retainAll(foundSource);
		
		log.send("Total Nodes.  source="+foundSource.size()+" sink="+foundSink.size()+" coincindent="+common.size(), sort(new ArrayList<Node>(common)));
		log.send("Source Reachable: ",sort(new ArrayList<Node>(foundSource)));
		log.send("Sink Reachable: ",sort(new ArrayList<Node>(foundSink)));
		
		
	}

	private List<Node> sort(List<Node> out) {
		Collections.sort(out, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return o1.toString().compareTo(o2.toString());
			}
			
			
		});
		
		return out;
	}

	private void reach(Node node, Set<Node> found, boolean pushing) {
		if (!found.contains(node)) {
			found.add(node);
			for (Arc a : node.getArcs()) {
				boolean reversed = a.getFrom() != node;
				boolean capacity = a.hasCapacity(reversed ==pushing);
				if (capacity) {
					Node otherEnd = a.getFrom() == node ? a.getTo() : a.getFrom();
					reach(otherEnd, found, pushing);
				}
			}
		}
	}

}