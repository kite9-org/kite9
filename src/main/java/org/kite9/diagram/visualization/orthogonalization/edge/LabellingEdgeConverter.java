package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Map;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.framework.common.Kite9ProcessingException;

public class LabellingEdgeConverter extends SimpleEdgeConverter implements ContainerLabelConverter {

	private ElementMapper em;
	
	public LabellingEdgeConverter(ContentsConverter cc, ElementMapper em) {
		super(cc);
		this.em = em;
	}

	@Override
	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex end, Vertex sideVertex) {
		Direction labelSide = (incident == Direction.UP) || (incident == Direction.DOWN) ? Direction.LEFT : Direction.UP;
		Label l = null;

		if (e instanceof ConnectionEdge) {
			
			ConnectionEdge ce = (ConnectionEdge) e;
			boolean fromEnd = ce.getFrom() == end;
			boolean toEnd = ce.getTo() == end;
			
			
			if (fromEnd) {
				if (end.getDiagramElements().contains(ce.getFromConnected())) {
					// we have the actual end then
					l = ce.getOriginalUnderlying().getFromLabel();
				} 
			} else if (toEnd) {
				if (end.getDiagramElements().contains(ce.getToConnected())) {
					// we have the actual end then
					l = ce.getOriginalUnderlying().getToLabel();
				} 
			} else {
				throw new Kite9ProcessingException();
			}
		} 
		
		if (l != null) {
			return convertWithLabel(e, o, incident, labelSide, end, sideVertex, l);
		} else {
			return super.convertPlanarizationEdge(e, o, incident, end, sideVertex);
		}
				
	}

	private Label findUnprocessedLabel(Container c) {
		for (DiagramElement de : c.getContents()) {
			if (de instanceof Label) {
				if (!em.hasOuterCornerVertices(de)) {
					return (Label) de;
				}
			}
		}
		
		return null;
	}

	private DartFace convertContainerLabel(DiagramElement de, Orthogonalization o, Label l) {
		cc.convertDiagramElementToInnerFace(l, o);
		CornerVertices cv = em.getOuterCornerVertices(l);
		Vertex topLeft = cv.getTopLeft();
		DartFace outer = cc.convertGridToOuterFace(o, topLeft, l);
		return outer;
	}
	
	

	private IncidentDart convertWithLabel(PlanarizationEdge e, Orthogonalization o, Direction incident, Direction labelJoinConnectionSide, Vertex end, Vertex sideVertex, Label l) {
		Direction side = Direction.reverse(incident);
		cc.convertDiagramElementToInnerFace(l, o);
		CornerVertices cv = em.getOuterCornerVertices(l);
		
		Vertex sideToLabel;
		Vertex labelToExternal;
		
		switch (labelJoinConnectionSide) {
		case UP:
			sideToLabel = incident == Direction.LEFT ? cv.getTopLeft() : cv.getTopRight();
			labelToExternal = incident == Direction.LEFT ? cv.getTopRight() : cv.getTopLeft();
			break;
		case DOWN:
			sideToLabel = incident == Direction.LEFT ? cv.getBottomLeft() : cv.getBottomRight();
			labelToExternal = incident == Direction.LEFT ? cv.getBottomRight() : cv.getBottomLeft();
			break;
			
		case LEFT:
			sideToLabel = incident == Direction.UP ? cv.getTopLeft() : cv.getBottomLeft();
			labelToExternal = incident == Direction.UP ? cv.getBottomLeft() : cv.getTopLeft();
			break;
		case RIGHT:
			sideToLabel = incident == Direction.UP ? cv.getTopRight() : cv.getBottomRight();
			labelToExternal = incident == Direction.UP ? cv.getBottomRight() : cv.getTopRight();
			break;
		default:
			throw new Kite9ProcessingException();
		}
		
		Map<DiagramElement, Direction> map = createMap(e);
		
		ExternalVertex externalVertex = createExternalvertex(e, end);
		o.createDart(sideVertex, sideToLabel, map, side);
		o.createDart(sideToLabel, labelToExternal, map, side);
		o.createDart(labelToExternal, externalVertex, map, side);
		
		if (e instanceof ConnectionEdge) {
			handleLabelContainment(((ConnectionEdge) e).getOriginalUnderlying(), l);
		}
		
		return new IncidentDart(externalVertex, sideVertex, side, e);
	}

	/**
	 * This method alters the DiagramElements' containment hierarchy, so that Compaction works correctly.
	 * We shouldn't be altering that at all.
	 */
	@Deprecated
	private void handleLabelContainment(Connection c, Label l) {
		if (c.getFromLabel() == l) {
			Container cc = c.getFrom().getContainer();
			cc.getContents().add(l);
		} else if (c.getToLabel() == l) {
			Container cc = c.getTo().getContainer();
			cc.getContents().add(l);
		} else {
			throw new Kite9ProcessingException();
		}
	}

	@Override
	public void handleContainerLabels(DartFace innerFace, DiagramElement partOf, Orthogonalization o) {
		if (!(partOf instanceof Container)) {
			return;
		}
		for (DartDirection dd : innerFace.getDartsInFace()) {
			if (dd.getDart().getDiagramElements().get(partOf) == Direction.DOWN) {
				// found bottom edge of container
				Label l2 = findUnprocessedLabel((Container) partOf);
				if (l2 != null) {
					DartFace df = convertContainerLabel(partOf, o, l2);
					df.setContainedBy(innerFace);
				}
				return;
			}
		}
	}
	
}
