package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.logging.LogicException;

public class LabellingEdgeConverter extends SimpleEdgeConverter implements EdgeConverter {

	private ElementMapper em;
	
	public LabellingEdgeConverter(ContentsConverter cc, ElementMapper em) {
		super(cc);
		this.em = em;
	}

	/**
	 * Adds labels to container edges, if the container has a label.
	 */
	@Override
	public void convertContainerEdge(Map<DiagramElement, Direction> underlyings, Orthogonalization o, Vertex end1, Vertex end2, Direction d, Side s) {
		Direction sideDirection = Direction.rotateAntiClockwise(d);
	
		DiagramElement de = getDiagramElementInside(underlyings, sideDirection);

		if (de instanceof Container) {
			// only label non-grid elements.  This is because grid edges can be shared between containers, and we don't know how to 
			// figure out label positioning in this case yet.
			Container parentContainer = ((Container) de).getContainer();
			Label l = findUnprocessedLabel((Container) de, sideDirection);
			while (l != null) { 
				if ((parentContainer != null) && (parentContainer.getLayout() != Layout.GRID)) {
					CornerVertices cv = em.getOuterCornerVertices(l);
					cc.convertDiagramElementToInnerFace(l, o);
					Vertex[] waypoints = rotateWaypointsCorrectly(cv, d);
					
					Dart d1 = o.createDart(end1, waypoints[0], underlyings, d);
					Dart d2 = o.createDart(waypoints[0], waypoints[1], Collections.emptyMap(), Direction.rotateClockwise(d));
					Dart d3 = o.createDart(waypoints[1], waypoints[2], Collections.emptyMap(), d);
					Dart d4 = o.createDart(waypoints[2], waypoints[3], Collections.emptyMap(), Direction.rotateAntiClockwise(d));
					s.newEdgeDarts.add(d1);
					s.newEdgeDarts.add(d2);
					s.newEdgeDarts.add(d3);
					s.newEdgeDarts.add(d4);
					end1=waypoints[3];
					l = findUnprocessedLabel((Container) de, sideDirection); 
				} else {
					l.getRenderingInformation().setRendered(false);
				}
			} 
		}
		
		super.convertContainerEdge(underlyings, o, end1, end2, d, s);
	}

	/**
	 * Waypoints is ordered if d is left (i.e. the label is at the bottom)
	 * However, it could be in any direction.
	 */
	private Vertex[] rotateWaypointsCorrectly(CornerVertices cv, Direction d) {
		List<Vertex> wp = Arrays.asList(cv.getBottomRight(), cv.getTopRight(), cv.getTopLeft(), cv.getBottomLeft());
		while (d != Direction.LEFT) {
			Collections.rotate(wp, -1);
			d = Direction.rotateClockwise(d);
		}
		
		return (Vertex[]) wp.toArray(new Vertex[wp.size()]);
	}

	protected DiagramElement getDiagramElementInside(Map<DiagramElement, Direction> underlyings, Direction sideDirection) {
		return underlyings.entrySet().stream()
			.filter(e -> e.getValue() == sideDirection)
			.map(e -> e.getKey())
			.findFirst()
			.orElse(null);
	}
	
	@Override
	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fan) {
		Direction labelSide = (incident == Direction.UP) || (incident == Direction.DOWN) ? Direction.LEFT : Direction.UP;
		Label l = null;

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
			DiagramElement de = ((BorderEdge) e).getElementForSide(Direction.DOWN);
			if (de instanceof Container) {
				l = findUnprocessedLabel((Container) de, Direction.DOWN);
				labelSide = Direction.DOWN;
			}
		}
		
		if (l != null) {
			return convertWithLabel(e, o, incident, labelSide, externalVertex, sideVertex, l);
		} else {
			return super.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, planVertex, fan);
		}
				
	}

	private Label findUnprocessedLabel(Container c, Direction d) {
		for (DiagramElement de : c.getContents()) {
			if ((de instanceof Label) && (((Label)de).getLabelPlacement().matches(d))) {
				if (!em.hasOuterCornerVertices(de)) {
					return (Label) de;
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
