package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.fg.AbsoluteArc;
import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.SimpleNode;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.visualization.orthogonalization.flow.EdgeVertex;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.orthogonalization.flow.OrthBuilder;
import org.kite9.diagram.visualization.orthogonalization.flow.vertex.ConstrainedVertexFlowOrthogonalizer;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.framework.logging.LogicException;

/**
 * Handles the creation of edges and faces in the flow graph.  
 * 
 * @author robmoffat
 *
 */
public class ConstrainedFaceFlowOrthogonalizer extends ConstrainedVertexFlowOrthogonalizer{

	public static final String FACE_SUBDIVISION_NODE = "fn";
	
	public ConstrainedFaceFlowOrthogonalizer(OrthBuilder<MappedFlowGraph> fb) {
		super(fb);
	}
	
	ConstraintGroup constraints;
	Map<Face, List<PortionNode>> facePortionMap = new HashMap<Face, List<PortionNode>>();
	Collection<SubdivisionNode> faceNodes = new LinkedList<SubdivisionNode>();

	@Override
	public MappedFlowGraph createOptimisedFlowGraph(Planarization pln) {
		
		ConstraintGroupGenerator sffr = new ConstraintGroupGenerator();
		constraints = sffr.getAllFloatingAndFixedConstraints(pln);
		// create portions and their nodes.
		for (Face f : pln.getFaces()) {
			if (faceRequiresFlowGraph(f)) {
				List<PortionNode> portions = createFacePortionNodes(f, constraints, pln);
				facePortionMap.put(f, portions);
			}
		}
		
		FaceMappedFlowGraph fg = (FaceMappedFlowGraph) super.createOptimisedFlowGraph(pln);
		fg.setFacePortionMap(facePortionMap);
		
		// nudge the graph so that face constraints are met.
		ConstraintNudger cn = new SequentialConstrainedFlowNudger(facePortionMap);
		cn.processNudges(fg, constraints, faceNodes);
		return fg;
	}
	
	protected Node createFaceSubdivisionNode(MappedFlowGraph fg, Face f) {
		SubdivisionNode fn = (SubdivisionNode) fg.getNodeFor(f);
		if (fn != null)
			return fn;

		int supply = f.isOuterFace() ? -4 : 4;

		fn = new SubdivisionNode("f[" + f.getId() + (f.isOuterFace() ? "x" : "") + "]", supply);
		fg.setNodeFor(f, fn);
		faceNodes.add(fn);
		return fn;
	}

	protected Node checkCreateEdgeNode(MappedFlowGraph fg, Edge e, Vertex near, String id) {
		EdgeVertex ev = createEdgeVertex(e, near);
		Node en = fg.getNodeFor(ev);
		if (en != null) {
			return en;
		}

		en = new SimpleNode("e[" + id + "]", 0, ev);
		en.setType(EDGE_NODE);
		fg.setNodeFor(ev, en);
		return en;
	}

	protected void createFlowGraphForEdge(MappedFlowGraph fg, Edge e, Node fn, int i) {
		PortionNode current = (PortionNode) fn;
		if (isConstrained(constraints, e)) {
			// when the edge is constrained by more than 2
			// portions, we need
			// to stop corners bleeding into portions they
			// are diagonally separated from
			// so we create multiple edge nodes
			if (current.containsVertexForEdge(e, e.getFrom())) {
				createPortionEdgeLink(e, current, e.getFrom(), fg, "-A", i);
			} else if (current.containsVertexForEdge(e, e.getTo())) {
				createPortionEdgeLink(e, current, e.getTo(), fg, "-B", i);
			} else {
				throw new LogicException("Portion should contain one end of the edge: " + current
						+ " " + e);
			}
		} else {
			createPortionEdgeLink(e, current, null, fg, "", i);
		}
	}
	

	protected boolean isConstrained(ConstraintGroup constraints, Edge e) {
		return constraints.isConstrained(e);
	}


	
	
	
	@Override
	protected void createFaceNodes(MappedFlowGraph fg, Face f, Planarization pln, VertexHandler vertexHandler) {
		Node fn = createFaceSubdivisionNode(fg, f);
		for (PortionNode current : facePortionMap.get(f)) {
			
			Arc faceArc = createFaceToPortionArc(fg, fn, current);
			current.setFaceArc(faceArc);

			int start = current.getEdgeStartPosition();
			int count = current.getEdgeEndPosition() - current.getEdgeStartPosition();
			if (count <= 0) {
				count += current.face.edgeCount();
			}
			if (start == -1) {
				start = 0;
			}

			int c = 0;
			while (c < count) {
				int i = (c + start) % current.face.edgeCount();
				Edge in = current.face.getBoundary(i);
				Edge out = current.face.getBoundary(i + 1);
				Vertex v = current.face.getCorner(i + 1);
				vertexHandler.processVertex(in, out, v, current);
				c++;
			}

			for (int i = 0; i <= count; i++) {
				Edge e = current.getEdge(i);
				if (!isConstrained(e)) {
					createFlowGraphForEdge(fg, e, current, i);
				}
			}
			
			fg.setNodeFor(null, current);
		}
	}
	
	/**
	 * Create arc from face node to portion node.  (A face is made up of several portions)
	 */
	protected Arc createFaceToPortionArc(MappedFlowGraph fg, Node fn, Node pn) {
		AbsoluteArc aa = new AbsoluteArc(TRACE, Integer.MAX_VALUE, fn, pn, fn.getId() + "-" + pn.getId());
		fg.getAllArcs().add(aa);
		return aa;
	}
	
	protected void createPortionEdgeLink(Edge e, PortionNode portion, Vertex end, MappedFlowGraph fg, String suffix, int i) {
		Node en = checkCreateEdgeNode(fg, e, end, e.toString()+suffix);
		List<Arc> arcs = createPortionEdgeArcs(portion, e, en);
		fg.getAllArcs().addAll(arcs);
	}
	

	protected List<Arc> createPortionEdgeArcs(Node fn, Edge e, Node en) {
		List<Arc> l = new LinkedList<Arc>();
		int weightCost = weightCost(e);
		Arc aa;
		aa = new AbsoluteArc(weightCost, Integer.MAX_VALUE, fn, en, fn.getId() + "-" + e.toString());
		log.send(log.go() ? null : "Edge Arc: "+e+" cost: "+weightCost+" (part of "+((PlanarizationEdge)e).getDiagramElements().keySet()+")");
		l.add(aa);
		return l;
	}

	
	
	private List<PortionNode> createFacePortionNodes(Face f, ConstraintGroup constraints, Planarization pln) {
		log.send(log.go() ? null : "Creating portions for "+(f.isOuterFace() ? "outer":"inner")+" face "+f.getId()+": "+f.cornerIterator());
		
		List<Integer> constrainedEdgesForFace = getConstraintsForFace(constraints, f);
		
		// first, divide up the faces into portions, bounded by constrained edges
		List<PortionNode> portions = createPortions(constrainedEdgesForFace, f);
		log.send("Portions for "+f.id, portions);
		return portions;
	}

	private List<Integer> getConstraintsForFace(ConstraintGroup constraints, Face f) {
		return constraints.getConstraintsRequiredForFace(f);
	}

	/**
	 * Subdivides the edges from a face or vertex into portions, bounded by constraining edges.
	 */
	private List<PortionNode> createPortions(List<Integer> constraintEdges, Face f) {
		
		if ((constraintEdges==null) || (constraintEdges.size() < 2)) {
			return Collections.singletonList(new PortionNode("p["+f.getId()+"p0"+(f.isOuterFace()?"x":"")+"]", 0, f, -1, -1));
		}
		
		List<PortionNode> portions = new ArrayList<PortionNode>(constraintEdges.size());
		for (int i = 0; i < constraintEdges.size(); i++) {
			int prev = i == 0 ? constraintEdges.get(constraintEdges.size()-1) : constraintEdges.get(i-1);
			int current = constraintEdges.get(i);
			PortionNode toAdd = new PortionNode("p["+f.getId()+"p"+(portions.size())+(f.isOuterFace()?"x":"")+"]", 0, f, prev, current);
			log.send("Created portion: "+toAdd+" starts at="+toAdd.getConstrainedEdgeStart()+" ends at="+toAdd.getConstrainedEdgeEnd()+" se="+toAdd.getEdgeStartPosition()+" ee="+toAdd.getEdgeEndPosition());
			portions.add(toAdd);
		}
		
		
		log.send(log.go() ? null : "Portion "+portions);
		
		return portions;
	}

	@Override
	protected MappedFlowGraph createFlowGraphObject(Planarization pln) {
		return new FaceMappedFlowGraph(pln);
	}
	
}


