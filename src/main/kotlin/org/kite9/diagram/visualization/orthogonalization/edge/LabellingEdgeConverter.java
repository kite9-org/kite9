package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.LabelPlacement;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.orthogonalization.vertex.ContainerContentsArranger;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.logging.LogicException;

public class LabellingEdgeConverter extends SimpleEdgeConverter {

	private ElementMapper em;
	
	public LabellingEdgeConverter(ContentsConverter cc, ElementMapper em) {
		super(cc);
		this.em = em;
	}

	@Override
	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fan) {
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
	
	private boolean hasLabels(Container con, Direction side) {
		return con.getContents().stream()
			.filter(c -> c instanceof Label)
			.filter(c -> ((Label)c).getLabelPlacement().containerLabelPlacement(side))
			.count() > 0;
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


	/**
	 * Adds labels to container edges, if the container has a label. We add the
	 * label by splitting the first dart we find.
	 */
	public void addLabelsToContainerDart(Orthogonalization o, List<Dart> darts, Vertex from, Vertex to, Direction going) {
		boolean hasLabels = false;
		for (Dart dart : darts) {
			for (DiagramElement de : dart.getDiagramElements().keySet()) {
				if (de instanceof Container) {
					Direction sideDirection = dart.getDiagramElements().get(de);
					hasLabels = hasLabels || hasLabels((Container) de, sideDirection);

					// clockwise direction around container
					Direction d = Direction.rotateClockwise(sideDirection);
					Vertex end1 = dart.getDrawDirection() == d ? dart.getFrom() : dart.getTo();

					// greedily collect all possible labels
					Map<Label, CornerVertices> toProcess = new LinkedHashMap<>();
					Label l = findUnprocessedLabel((Container) de, sideDirection);
					while (l != null) {
						toProcess.put(l, em.getOuterCornerVertices(l));
						l = findUnprocessedLabel((Container) de, sideDirection);
					}
					
					// split the darts up
					for(Entry<Label, CornerVertices> e : toProcess.entrySet()) {
						Vertex[] waypoints = rotateWaypointsCorrectly(e.getValue(), d);

						Pair<Dart> p1 = o.splitDart(dart, waypoints[0]);
						Dart p1keep = p1.getA().meets(end1) ? p1.getA() : p1.getB();
						Dart p1change = p1.getA().meets(end1) ? p1.getB() : p1.getA();
						
						o.splitDart(p1change, waypoints[3]);
						cc.convertDiagramElementToInnerFace(e.getKey(), o);

						dart = p1keep;
					}
				}
			}
		}
		
		if (hasLabels) {
			darts.clear();
			ContainerContentsArranger.populateInnerFaceDarts(darts, from, to, going);
		}
	}

	/**
	 * Waypoints is ordered if d is left (i.e. the label is at the bottom) However,
	 * it could be in any direction.
	 */
	private Vertex[] rotateWaypointsCorrectly(CornerVertices cv, Direction d) {
		List<Vertex> wp = Arrays.asList(cv.getBottomRight(), cv.getTopRight(), cv.getTopLeft(), cv.getBottomLeft());
		while (d != Direction.LEFT) {
			Collections.rotate(wp, -1);
			d = Direction.rotateClockwise(d);
		}

		return (Vertex[]) wp.toArray(new Vertex[wp.size()]);
	}

	@Override
	public List<Dart> buildDartsBetweenVertices(Map<DiagramElement, Direction> underlyings, Orthogonalization o, Vertex end1,
			Vertex end2, Direction d) {
		List<Dart> s = super.buildDartsBetweenVertices(underlyings, o, end1, end2, d);
		addLabelsToContainerDart(o, s, end1, end2, d);
		return s;		
	}

}
