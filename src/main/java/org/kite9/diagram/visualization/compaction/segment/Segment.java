package org.kite9.diagram.visualization.compaction.segment;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.framework.common.Kite9ProcessingException;


/**
 * A segment is a set of vertices that must have the same horizontal or vertical position.
 * 
 * 
 * @author robmoffat
 *
 */
public class Segment implements Comparable<Segment> {
	
	private PositionAction dimension;
	private int i;
	private boolean positioned = false;
	private double position;
	private Slideable<Segment> slideable;
	private Set<UnderlyingInfo> underlyings;
	private Set<Rectangular> rectangulars;
	private Set<Connection> connections;
	
	public Slideable<Segment> getSlideable() {
		return slideable;
	}

	public void setSlideable(Slideable<Segment> slideable) {
		this.slideable = slideable;
	}
	
	public Set<UnderlyingInfo> getUnderlyingInfo() {
		if (underlyings == null) {
			underlyings = getDartsInSegment().stream().flatMap(o -> convertUnderlyingToUnderlyingInfo(o))
			.filter(a -> a.getDiagramElement() != null)
			.collect(Collectors.toSet());
		}
		
		return underlyings;
	}
	
	public DiagramElement getUnderlyingWithSide(Side s) {
		return getUnderlyingInfo().stream().filter(ui -> ui.getSide() == s).map(ui -> ui.getDiagramElement()).findFirst().orElse(null);
	}
	
	public boolean hasUnderlying(DiagramElement de) {
		return underlyings.stream()
				.map(u -> u.getDiagramElement())
				.filter(a -> a == de)
				.count() > 0;
	}
	
	private Stream<UnderlyingInfo> convertUnderlyingToUnderlyingInfo(Dart d) {
		Map<DiagramElement, Direction> diagramElements = d.getDiagramElements();
		return diagramElements.keySet().stream().map(de -> toUnderlyingInfo(de, diagramElements.get(de)));
	}

	private UnderlyingInfo toUnderlyingInfo(DiagramElement de, Direction d) {
		return new UnderlyingInfo(de, 
				getSideFromDirection(de, d));
	}
	
	private Side getSideFromDirection(DiagramElement de, Direction d) {
		if (de instanceof BiDirectional) {
			return Side.NEITHER;
		} else if (de instanceof Rectangular) {
			switch (d) {
			case DOWN:
			case RIGHT:
				return Side.END;
			case UP:
			case LEFT: 
				return Side.START;
			default:
				return Side.NEITHER;
			}
		} else {
			throw new Kite9ProcessingException();
		}
	}

	public Segment(PositionAction dimension, int i) {
		this.dimension = dimension;
		this.i = i;
	}
	
	public void addToSegment(Vertex v) {
		verticesInSegment.add(v);
		underlyings = null;
	}

	public String getIdentifier() {
		return dimension+" ("+i+" "+getUnderlyingInfo()+" )";
	}
	
	@Override
	public String toString() {
		 return getIdentifier() + " pos: "+position+"  "+verticesInSegment.toString();
	}

	public void setPosition(double d) {
		for (Vertex v : getVerticesInSegment()) {
			dimension.set(v, d);
		}
	
		position = d;
	}
	
	private Set<Vertex> verticesInSegment = new LinkedHashSet<Vertex>();

	public Set<Vertex> getVerticesInSegment() {
		return verticesInSegment;
	}
	
	public boolean connects(Vertex a, Vertex b) {
		return (inSegment(a) && inSegment(b));
	}

	private boolean inSegment(Vertex b) {
		return verticesInSegment.contains(b);
	}

	public boolean isPositioned() {
		return positioned;
	}

	public void setPositioned(boolean positioned) {
		this.positioned = positioned;
	}

	public double getPosition() {
		return position;
	}

	public PositionAction getDimension() {
		return dimension;
	}

	/**
	 * De-facto ordering for segments.
	 */
	public int compareTo(Segment o) {
		Double pos = this.position;
		return pos.compareTo(o.position);
		
		// TODO: need to incorporate dependencies here too, when values are equal
	}
	
	/**
	 * This is a utility method, used to set the positions of the darts for the diagram
	 */
	public Collection<Dart> getDartsInSegment() {
		Collection<Dart> darts = new DetHashSet<Dart>();
		for (Vertex v : verticesInSegment) {
			for (Edge e : v.getEdges()) {
				if (e instanceof Dart) { 
					if (dimension==PositionAction.YAction) {
						if ((e.getDrawDirection()==Direction.LEFT) || (e.getDrawDirection()==Direction.RIGHT)) {
							darts.add((Dart)e);
						}
					} else if (dimension==PositionAction.XAction) {
						if ((e.getDrawDirection()==Direction.UP) || (e.getDrawDirection()==Direction.DOWN)) {
							darts.add((Dart)e);
						}
					}
				}
			}
		}
		
		return darts;
	}

	public int getNumber() {
		return i;
	}
	

	public Set<Rectangular> getRectangulars() {
		if (rectangulars==null) {
			rectangulars = underlyings.stream()
					.map(ui -> ui.getDiagramElement())
					.filter(de -> de instanceof Rectangular)
					.map(de -> (Rectangular) de).collect(Collectors.toSet());
		} 
		
		return rectangulars;
	}
	
	public Set<Connection> getConnections() {
		if (connections==null) {
			connections = underlyings.stream()
					.map(ui -> ui.getDiagramElement())
					.filter(de -> de instanceof Connection)
					.map(de -> (Connection) de).collect(Collectors.toSet());
		} 
		
		return connections;
	}
}