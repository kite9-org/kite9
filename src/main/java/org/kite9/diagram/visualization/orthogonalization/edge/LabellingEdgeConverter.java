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
import org.kite9.diagram.model.style.LabelPlacement;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.logging.LogicException;

public class LabellingEdgeConverter extends SimpleEdgeConverter {

	private ElementMapper em;
	
	public LabellingEdgeConverter(ContentsConverter cc, ElementMapper em) {
		super(cc);
		this.em = em;
	}

	@Override
	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fan) {
		System.err.println("Border edge "+e);
		Label l = null;
		Direction labelSide = null;

		if (e instanceof ConnectionEdge) {
			
			ConnectionEdge ce = (ConnectionEdge) e;
			boolean fromEnd = planVertex.isPartOf(ce.getFromConnected());
			boolean toEnd = planVertex.isPartOf(ce.getToConnected());
			
			
			if (fromEnd) {
				if (planVertex.getDiagramElements().contains(ce.getFromConnected())) {
					// we have the actual end then
					l = ce.getOriginalUnderlying().getFromLabel();
				} 
			} else if (toEnd) {
				if (planVertex.getDiagramElements().contains(ce.getToConnected())) {
					// we have the actual end then
					l = ce.getOriginalUnderlying().getToLabel();
				} 
			} else {
				// middle bit of an edge
				l = null;
			}
		} else if (e instanceof BorderEdge) {
			labelSide = Direction.rotateAntiClockwise(incident);
			DiagramElement de = ((BorderEdge) e).getElementForSide(labelSide);
			if (de instanceof Container) {
				l = findUnprocessedLabel((Container) de, labelSide);
			}
		}

		
		if (l != null) {
			LabelPlacement lp = l.getLabelPlacement();
			labelSide = labelSide != null ? labelSide : lp.connectionLabelPlacementDirection(incident);
			return convertWithLabel(e, o, incident, labelSide, externalVertex, sideVertex, l);
		} else {
			return super.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, planVertex, fan);
		}
				
	}
	
	private Label findUnprocessedLabel(Container c, Direction side) {
		for (DiagramElement de : c.getContents()) {
			if (de instanceof Label) {
				if (((Label)de).getLabelPlacement().containerLabelPlacement(side)) {
					if (!em.hasOuterCornerVertices(de)) {
						return (Label) de;
					}
				}
			}
		}
		
		return null;
	}

	private IncidentDart convertWithLabel(PlanarizationEdge e, Orthogonalization o, Direction incident, Direction labelJoinConnectionSide, Vertex externalVertex, Vertex sideVertex, Label l) {
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
			throw new LogicException();
		}
		
		Map<DiagramElement, Direction> map = createMap(e);
		
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
			throw new LogicException();
		}
	}

}
