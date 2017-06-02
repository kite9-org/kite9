package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.orthogonalization.vertex.AbstractVertexArranger;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.common.Kite9ProcessingException;

public class LabellingEdgeConverter extends SimpleEdgeConverter {

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
		} else if (e instanceof BorderEdge) {
			Direction d = e.getDrawDirectionFrom(end);
			
			if (d == Direction.RIGHT) {
				// we are on a left side vertex, with either top or bottom edge.
				DiagramElement de = (((BorderEdge) e).getElementForSide(Direction.DOWN));
				if (de instanceof Container) {
					l = findUnprocessedLabel((Container) de);
					labelSide = Direction.DOWN;
				}
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
	
	

	@Override
	public List<Dart> convertContainerEdge(DiagramElement de, Orthogonalization o, Vertex end1, Vertex end2, Direction edgeSide, Direction d) {
		Label l = null;
		if ((de instanceof Container) && (edgeSide == Direction.DOWN)) {
			l = findUnprocessedLabel((Container) de);
		}
		
		if (l == null) {
			return super.convertContainerEdge(de, o, end1, end2, edgeSide, d);
		} else {
			return convertContainerEdgeWithLabel(de, o, end1, end2, edgeSide, d, l);
		}
	}

	private List<Dart> convertContainerEdgeWithLabel(DiagramElement de, Orthogonalization o, Vertex end1, Vertex end2, Direction edgeSide, Direction d, Label l) {
		cc.convertDiagramElementToInnerFace(l, o);
		CornerVertices cv = em.getOuterCornerVertices(l);
		
		Vertex leftLabel = cv.getBottomLeft();
		Vertex rightLabel = cv.getBottomRight();
		
		if (d != Direction.LEFT) {
			throw new Kite9ProcessingException();
		}
		
		Dart d1 = o.createDart(end1, rightLabel, de, Direction.LEFT, Direction.DOWN);
		o.createDart(rightLabel, leftLabel, de, Direction.LEFT, Direction.DOWN);
		o.createDart(leftLabel, end2,de, Direction.LEFT, Direction.DOWN);
		
		List<Dart> out = new ArrayList<>();
		out.add(d1);
		Dart dart  = d1;
		Vertex to = dart.otherEnd(end1);
		
		do {
			dart = AbstractVertexArranger.getNextDartAntiClockwise(to, dart);
			d = dart.getDrawDirectionFrom(to);
			to = dart.otherEnd(to);
			out.add(dart);
		} while (to != end2);
		
		return out;
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

	
}
