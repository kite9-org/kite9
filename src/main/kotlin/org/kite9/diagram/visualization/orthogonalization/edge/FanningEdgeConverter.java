package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Arrays;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.FanVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.planarization.Tools;

public class FanningEdgeConverter extends LabellingEdgeConverter {

	public FanningEdgeConverter(ContentsConverter cc, ElementMapper em) {
		super(cc, em);
	}

	private int counter;

	@Override
	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fan) {
		if (fan != null) {

			// disregard for straight edges
			if ((e.getDrawDirection() == null) || (Tools.isUnderlyingContradicting(e))) {
				
				Connected c = incident == e.getDrawDirection() ? 
						((BiDirectionalPlanarizationEdge) e).getToConnected() :
							((BiDirectionalPlanarizationEdge) e).getFromConnected();

				Vertex fanOuter = new FanVertex(planVertex.getID() + "-fo-" + counter, false, Arrays.asList(incident, Direction.reverse(fan)));
				Vertex fanInner = new FanVertex(planVertex.getID() + "-fi-" + counter, true, Arrays.asList(incident, fan));
				counter++;

				Map<DiagramElement, Direction> map = createMap(e);
				o.createDart(externalVertex, fanOuter, map, incident);
				o.createDart(fanOuter, fanInner, map, Direction.reverse(fan));

				IncidentDart out = super.convertPlanarizationEdge(e, o, incident, fanInner, sideVertex, planVertex, fan);
				out.setExternal(externalVertex);
				return out;
			}
		}

		return super.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, planVertex, fan);
	}

}
