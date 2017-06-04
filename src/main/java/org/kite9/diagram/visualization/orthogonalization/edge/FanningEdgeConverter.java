package org.kite9.diagram.visualization.orthogonalization.edge;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;

public class FanningEdgeConverter extends LabellingEdgeConverter {

	public FanningEdgeConverter(ContentsConverter cc, ElementMapper em) {
		super(cc, em);
	}

	@Override
	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fan) {
		// TODO Auto-generated method stub
		return super.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, planVertex,  fan);
	}

	
}
