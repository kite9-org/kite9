package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.visualization.planarization.Face;

public class ConstraintGroup {

	private final Set<Edge> fixedConstraints;

	public ConstraintGroup(Set<Edge> fixedConstraints, int faces) {
		super();
		this.fixedConstraints = new DetHashSet<Edge>(fixedConstraints);
		this.faceCount = faces;
	}

	private Set<Edge> floatingConstraints = new DetHashSet<Edge>();

	private List<Route> requiredRoutes = new ArrayList<Route>();
	
	private int faceCount;

	public List<Route> getRequiredRoutes() {
		return requiredRoutes;
	}

	private Map<Face, List<Integer>> constrainedFaces = null;

	/**
	 * This is used in portion creation. It looks at which routes use this
	 * face, and asks for just the constraints within those routes on that
	 * face. This is to avoid breaking the face into too many unnecessary
	 * portions.
	 */
	public List<Integer> getConstraintsRequiredForFace(Face f) {
		if (constrainedFaces == null) {
			constrainedFaces = new HashMap<Face, List<Integer>>(faceCount * 2);
			for (Route r : requiredRoutes) {
				while (r!=null) {
					Face ff = r.face;
					List<Integer> constraintsForFace = constrainedFaces.get(ff);
					if (constraintsForFace == null) {
						constraintsForFace = new LinkedList<Integer>();
						constrainedFaces.put(ff, constraintsForFace);
					}
					
					if (!constraintsForFace.contains(r.in)) {
						constraintsForFace.add(r.in);
					}
					
					if (!constraintsForFace.contains(r.out)) {
						constraintsForFace.add(r.out);
					}
					
					Edge e1 = r.getInEdge();
					if (!fixedConstraints.contains(e1)) {
						floatingConstraints.add(e1);
					}
					
					Edge e2 = r.getOutEdge();
					if (!fixedConstraints.contains(e2)) {
						floatingConstraints.add(e2);
					}
					
					r = r.rest;
				}
			}
			
			for (Entry<Face, List<Integer>> ent : constrainedFaces.entrySet()) {
				Collections.sort(ent.getValue());
			}
		}
		
		return constrainedFaces.get(f);
	}

	public boolean isConstrained(Edge e) {
		return fixedConstraints.contains(e) || floatingConstraints.contains(e);
	}

	public String toString() {
		return fixedConstraints.toString();
	}
	
	public void addRoute(Route r) {
		requiredRoutes.add(r);
	}
}