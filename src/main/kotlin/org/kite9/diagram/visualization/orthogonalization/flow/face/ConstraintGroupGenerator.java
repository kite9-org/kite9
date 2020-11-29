package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;

/**
 * This class looks at the constraints set in the diagram and works out how to
 * join all of the constraints together so they are not separated by faces.
 * 
 * Note that since we now have unconnected graphs, there can be several
 * constraint groups (i.e. it is not possible to get from one constraint to
 * another via faces and over edges).
 * 
 * ConstraintGroups are a necessary evil when there are constrained edges within
 * a graph that are not on the same face - you need to know the route between
 * them in order to ensure that the direction between them is good.
 * 
 * There is a problem with constraint groups that it is possible to "box" yourself in, and 
 * create constraints that when applied first stop other ones from working.  
 * To avoid this, we depth-first search through the faces to find another constrained face, 
 * and make a route to it. Then, we use that as a hub to look for another face.
 * 
 * @author robmoffat
 * 
 */
public class ConstraintGroupGenerator implements Logable {

	private Kite9Log log = new Kite9Log(this);

	public ConstraintGroup getAllFloatingAndFixedConstraints(Planarization pln) {
		Set<Edge> constrainedEdges = gatherConstrainedEdges(pln);

		ConstraintGroup out = new ConstraintGroup(constrainedEdges, pln.getFaces().size());

		while (constrainedEdges.size() > 1) {
			Edge startEdge = constrainedEdges.iterator().next();
			constrainedEdges.remove(startEdge);
			Set<Face> visitedFaces = new UnorderedSet<Face>();
			Face firstFace = pln.getEdgeFaceMap().get(startEdge).get(0);
			visitFace(firstFace, firstFace.indexOf(startEdge).iterator().next(), visitedFaces, out, null, constrainedEdges, pln, 0, true);
		}

		log.send(log.go() ? null : "Constraint group: "+ out);
		return out;
	}

	/**
	 * Returns true if a constraint was found
	 */
	private boolean visitFace(Face face, int in, Set<Face> visitedFaces, ConstraintGroup theGroup, Route openRoute, Set<Edge> constrainedEdges, Planarization pln, int depth, boolean constraintFound) {

		log.send(pad(depth)+"Visiting Face: "+face.id+" start at "+in);
		
		visitedFaces.add(face);
		int start = in;

		// create all constraints for the current face
		for (int i = 0; i < face.edgeCount(); i++) {
			int out = (i + start) % face.edgeCount();
			Edge currentEdge = face.getBoundary(out);
			log.send(log.go() ? null : pad(depth+1) + "checking: "+currentEdge+ " at "+out);
			
			// check to see if we have completed a constraint
			if (constrainedEdges.contains(currentEdge)) {
				constrainedEdges.remove(currentEdge);
				openRoute = new Route(face, in, out, openRoute);
				log.send(log.go() ? null : pad(depth) + "Route added: "+openRoute);
				theGroup.addRoute(openRoute);
				openRoute = null;
				constraintFound = true;
				in = out;
			}
		}
		
		start = in;

		// ok, start moving off to other faces
		for (int i = 0; i < face.edgeCount(); i++) {
			int out = (i + start) % face.edgeCount();
			Edge currentEdge = face.getBoundary(out);
			// check for traverse face
			List<Face> meetingFaces = pln.getEdgeFaceMap().get(currentEdge);
			Face otherFace = meetingFaces.get(0) == face ? meetingFaces.get(1) : meetingFaces.get(0);
								
			if (!visitedFaces.contains(otherFace)) {
				// go to the new face
				Route toUse = (in != out) ? new Route(face, in, out, openRoute) : openRoute;
				log.send(pad(depth)+"Using route: "+toUse);
				boolean found = visitFace(otherFace, otherFace.indexOf(currentEdge).iterator().next(), visitedFaces, theGroup, toUse, constrainedEdges, pln, depth+1, out == in);
				if (found && !constraintFound) {
					// you can only visit one new face unless you find another constraint
					log.send(pad(depth)+"Finished Face: "+face.id);
					return found;
				}
			}
		}
		
		log.send(pad(depth)+"Finished Face: "+face.id);
		
		return constraintFound;
	}

	private String pad(int depth) {
		StringBuilder sb = new StringBuilder(depth);
		for (int i = 0; i < depth; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * Creates one constraint group from each constrained edge in the diagram
	 */
	private Set<Edge> gatherConstrainedEdges(Planarization pln) {
		Set<Edge> constrainedEdges = new DetHashSet<Edge>();
		for (Face f : pln.getFaces()) {
			for (Edge edge : f.edgeIterator()) {
				if (AbstractFlowOrthogonalizer.isConstrained(edge) && (!constrainedEdges.contains(edge))) {
					constrainedEdges.add(edge);
				}
			}
		}

		log.send(log.go() ? null : "initial constrained edges: " + listConstraints(constrainedEdges));

		return constrainedEdges;
	}

	private String listConstraints(Set<Edge> constrainedEdges) {
		StringBuilder sb = new StringBuilder();
		for (Edge edge : constrainedEdges) {
			sb.append(edge.toString());
			sb.append(" ");
			sb.append(edge.getDrawDirection());
			sb.append("(");
			sb.append(((PlanarizationEdge) edge).getDiagramElements());
			sb.append(")");
			sb.append(",");
		}
		return sb.toString();
	}

	public String getPrefix() {
		return "COGG";
	}

	public boolean isLoggingEnabled() {
		return true;
	}
}
