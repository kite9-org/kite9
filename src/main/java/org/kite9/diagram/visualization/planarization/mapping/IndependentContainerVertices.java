package org.kite9.diagram.visualization.planarization.mapping;

import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.HIGHEST_ORD;
import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.LOWEST_ORD;

import java.util.HashMap;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;

public class IndependentContainerVertices extends AbstractContainerVertices {
	
	public IndependentContainerVertices(Container c) {
		super(c, LOWEST_ORD, HIGHEST_ORD, LOWEST_ORD, HIGHEST_ORD, new HashMap<>());
		
		ContainerVertex tl = createVertex(LOWEST_ORD, LOWEST_ORD);
		ContainerVertex tr = createVertex(HIGHEST_ORD, LOWEST_ORD);
		ContainerVertex br = createVertex(HIGHEST_ORD, HIGHEST_ORD);
		ContainerVertex bl = createVertex(LOWEST_ORD, HIGHEST_ORD);
		
		tl.addAnchor(HPos.LEFT, VPos.UP, c);
		tr.addAnchor(HPos.RIGHT, VPos.UP, c);
		bl.addAnchor(HPos.LEFT, VPos.DOWN, c);
		br.addAnchor(HPos.RIGHT, VPos.DOWN, c);
	}

}