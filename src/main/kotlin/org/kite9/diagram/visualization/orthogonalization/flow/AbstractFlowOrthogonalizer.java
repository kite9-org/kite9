package org.kite9.diagram.visualization.orthogonalization.flow;

import java.util.Collection;
import java.util.TreeSet;

import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.FlowGraphSPP;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.RapidFlowGraphSSP;
import org.kite9.diagram.common.algorithms.fg.SimpleNode;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.logging.LogicException;

/**
 * This class builds a corner-flow network to model the current Planarization.
 * It is capable of handling any degree vertex, and also edge constraints where
 * an edge must be oriented in a certain direction within the diagram.
 * 
 * The basic approach is that faces, edges, vertices and face-vertice boundaries (helpers) are all nodes.
 * Faces and vertices have 4 corners to dispose of.  Helpers can sink 2.  Corners can be pushed over edge nodes.
 * Arcs link up the various parts.
 * 
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractFlowOrthogonalizer extends MappedFlowOrthogonalizer implements Logable {

	public AbstractFlowOrthogonalizer(VertexArranger va, EdgeConverter clc) {
		super(va, clc);
	}

	public static final String VERTEX_NODE = "vn";
	public static final String PORTION_NODE = "pn";
	public static final String HELPER_NODE = "hn";
	public static final String EDGE_NODE = "en";


	/**
	 * this is added to ensure that the algorithm doesn't track about all over
	 * the shop picking longer and longer routes
	 */
	public static final int TRACE = 1;

	/**
	 * This is the cost of inserting a corner on an edge
	 */
	public static final int CORNER = 100;

	int nodeNo = 0;

	protected Kite9Log log = new Kite9Log(this);

	private void createFlowGraph(Planarization pln, MappedFlowGraph fg) {
		log.send(log.go() ? null : "Planarization to convert:" + pln);
		log.send(log.go() ? null : pln.getFaces().toString());

		// create face, vertex, edge nodes and all of the arcs between them and
		// the portion nodes
		for (Face f : pln.getFaces()) {
			if (faceRequiresFlowGraph(f)) {
				createFlowGraphForFace(pln, fg, f);
			}
		}

		// output detail in log
		Collection<String> nodes = new TreeSet<String>();
		for (Node n : fg.getAllNodes()) {
			nodes.add(n.getID() + " has " + n.getArcs().size() + " arcs: " + n.getArcs());
		}
		log.send(log.go() ? null : "Node details: ", nodes);

	}
	
	public interface VertexHandler {
		
		public void processVertex(Edge in, Edge out, Vertex v, Node face);
		
	}
	

	protected abstract void createFlowGraphForVertex(MappedFlowGraph fg, Face f, Node p, Vertex v, Edge before, Edge after, Planarization pln);
	
	protected void createFlowGraphForFace(final Planarization pln, final MappedFlowGraph fg, final Face f) {
		createFaceNodes(fg, f, pln, new VertexHandler() {

			@Override
			public void processVertex(Edge in, Edge out, Vertex v, Node current) {
				createFlowGraphForVertex(fg, f, current, v, in, out, pln);
			}
		});
	}

	protected abstract void createFaceNodes(MappedFlowGraph fg, Face f, Planarization pln, VertexHandler vertexHandler);

	
	protected void addIfNotNull(MappedFlowGraph fg, Arc a) {
		if (a != null)
			fg.getAllArcs().add(a);
	}

	protected Node createHelperNode(MappedFlowGraph fg, Face f, Vertex v, Node vn, Edge before, Edge after) {
		// this is the number of corners the vertex can take from the face
		// between these two edges
		int supply = -2;

		FaceVertex ff = createFaceVertex(f, v, before, after);
		Node hn = new SimpleNode("h" + (nodeNo++) + "[" + v.getID() + "/" + f.getID() + "]", supply, ff);
		fg.setNodeFor(ff, hn);
		hn.setType(HELPER_NODE);
		return hn;
	}
	
	public static FaceVertex createFaceVertex(Face from, Vertex to, Edge before, Edge after) {
		FaceVertex ff = new FaceVertex();
		ff.face = from;
		ff.vertex = to;
		ff.prior = before;
		ff.after = after;
		if ((!before.meets(to)) || (!after.meets(to))) {
			throw new LogicException("Edges must meet the vertex in the face " + ff);
		}
		return ff;
	}
	
	public static EdgeVertex createEdgeVertex(Edge e, Vertex to) {
		EdgeVertex ff = new EdgeVertex();
		ff.edge = e;
		if (to != null)
			ff.vertex = to;
		return ff;
	}

	protected int weightCost(Edge e) {
		return CORNER * ((PlanarizationEdge) e).getBendCost() + TRACE;
	}

	public String getPrefix() {
		return "PLAN";
	}

	public boolean isLoggingEnabled() {
		return false;
	}

	public boolean faceRequiresFlowGraph(Face f) {
		return (f.edgeCount() > 0);
	}

	@Override
	public MappedFlowGraph createOptimisedFlowGraph(Planarization pln) {
		// repository for any generated constraints
		MappedFlowGraph fg = createFlowGraphObject(pln);

		// create constraints which will subdivide the faces
		createFlowGraph(pln, fg);
		maximiseFlow(fg);
		return fg;
	}

	/**
	 *  first stage - maximise the flows to get an approximate solution
	 */
	protected void maximiseFlow(MappedFlowGraph fg) {
		FlowGraphSPP<MappedFlowGraph> ssp = new RapidFlowGraphSSP<MappedFlowGraph>();
		ssp.displayFlowInformation(fg);
		ssp.maximiseFlow(fg);
		checkFlows(fg);
	}

	protected abstract MappedFlowGraph createFlowGraphObject(Planarization pln);

	public static void removeArcs(MappedFlowGraph fg, Node n) {
		for (Arc a : n.getArcs()) {

			Node otherEnd = a.getFrom() == n ? a.getTo() : a.getFrom();
			otherEnd.getArcs().remove(a);

			a.setFrom(null);
			a.setTo(null);

			fg.getAllArcs().remove(a);
		}
		
		n.getArcs().clear();
	}
	
	public static boolean isConstrained(Edge e) {
		return (e.getDrawDirection()!=null) && (!Tools.isUnderlyingContradicting(e));
	}
}
