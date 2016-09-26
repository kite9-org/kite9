package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;

import org.kite9.diagram.adl.Container;

/**
 * Adds vertices to the parent container vertices, and operates in a narrower range.
 * 
 * @author robmoffat
 *
 */
public class SubwindowContainerVertices extends AbstractContainerVertices {
	
	private ContainerVertices parent;
	
	private int minx, miny, maxx, maxy;

	public SubwindowContainerVertices(Container c, ContainerVertices parentCV) {
	
		this.parent = parentCV;
		while (parentCV instanceof SubwindowContainerVertices) {
			parentCV = parentCV.
		}
	}

	@Override
	public ArrayList<ContainerVertex> getPerimeterVertices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ContainerVertex> getVerticesInXOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContainerVertex createVertex(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

}
