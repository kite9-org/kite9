/**
 * 
 */
package org.kite9.diagram.visualization.orthogonalization.flow.face;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.visualization.planarization.Face;


/**
 * A route through faces used in the {@link ConstraintGroupGenerator} algorithm
 * 
 * @author robmoffat
 *
 */
class Route {

	Face face;
	public Face getFace() {
		return face;
	}

	int in = -1;
	int out = -1;
	
	public int getIn() {
		return in;
	}
	
	public int getOut() {
		return out;
	}

	public Route getRest() {
		return rest;
	}

	Route rest;
	
	Route(Face f, int in, int out, Route rest) {
		super();
		this.face = f;
		this.rest = rest;
		this.in = in;
		this.out = out;
	}

	public int size() {
		if (rest == null) {
			return 1;
		} else {
			return 1 + rest.size();
		}
	}

	public String toString() {
		if (rest == null) {
			return face.getBoundary(out).toString()+"-"+face.getId()+"-"+face.getBoundary(in).toString();
		} else {
			return face.getBoundary(out).toString()+"-"+face.getId()+"-"+rest.toString();
		}
	}
	
	public boolean containsFace(Face f) {
		return this.face == f ||  ((rest!=null) && (rest.containsFace(f)));
	}
	
	public Edge getInEdge() {
		return face.getBoundary(in);
	}
	
	public Edge getOutEdge() {
		return face.getBoundary(out);
	}
	
}