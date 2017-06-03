package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.visualization.orthogonalization.Dart;

/**
 * Holds vertices and darts for one side of the vertex being converted
 */
public class Side {

	List<Vertex> vertices = new ArrayList<Vertex>();

	LinkedHashSet<Dart> newEdgeDarts = new LinkedHashSet<Dart>();

	public void addVertex(Vertex vsv) {
		vertices.add(vsv);
	}

	public LinkedHashSet<Dart>  getDarts() {
		return newEdgeDarts;
	}
}