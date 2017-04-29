package org.kite9.diagram.visualization.compaction.segment;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;


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
	
	
	public Slideable<Segment> getSlideable() {
		return slideable;
	}

	public void setSlideable(Slideable<Segment> slideable) {
		this.slideable = slideable;
	}
	
	public Set<UnderlyingInfo> getUnderlyingInfo() {
		if (underlyings == null) {
			underlyings = getDartsInSegment().stream().flatMap(o -> convertUnderlyingToUnderlyingInfo(o))
			.filter(a -> a.de != null)
			.collect(Collectors.toSet());
		}
		
		return underlyings;
	}
	
	private Stream<UnderlyingInfo> convertUnderlyingToUnderlyingInfo(Dart d) {
		Object o = d.getUnderlying();
		if (o instanceof Edge) {
			if (o instanceof BiDirectionalPlanarizationEdge) {
				return Stream.of(new UnderlyingInfo(((BiDirectionalPlanarizationEdge)o).getOriginalUnderlying(), Side.NEITHER));
			} else if (o instanceof BorderEdge) {
				BorderEdge borderEdge = (BorderEdge) o;
				Map<DiagramElement, Direction> diagramElements = borderEdge.getDiagramElements();
				Iterator<DiagramElement> it = diagramElements.keySet().iterator();
				
				if (diagramElements.size() == 2) {
					DiagramElement first = it.next();
					DiagramElement second = it.next();
					
					if (first == null) {
						return Stream.of(toUnderlyingInfo(diagramElements, second));
					} else if (second == null) {
						return Stream.of(toUnderlyingInfo(diagramElements, first));
					} else if (first.getParent() == second) {
						return Stream.of(toUnderlyingInfo(diagramElements, first));
					} else if (second.getParent() == first) {
						return Stream.of(toUnderlyingInfo(diagramElements, second));
					} else {
						return Stream.of(toUnderlyingInfo(diagramElements, first), toUnderlyingInfo(diagramElements, second));
					}
					
				} else if (diagramElements.size() == 1) {
					DiagramElement de = it.next();
					return Stream.of(toUnderlyingInfo(diagramElements, de));
				} else {
					throw new LogicException("Border Edge with wrong number of diagram elements: "+o);
				}
			} 
		} 
		
		throw new Kite9ProcessingException("Don't know what this is: "+o);
	}

	private UnderlyingInfo toUnderlyingInfo(Map<DiagramElement, Direction> diagramElements, DiagramElement de) {
		return new UnderlyingInfo(de, 
				getSideFromDirection(diagramElements.get(de)));
	}
	
	private Side getSideFromDirection(Direction d) {
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

	public DiagramElement getSingleUnderlying() {
		if ((getUnderlyingInfo().size() > 1) || (getUnderlyingInfo().size() == 0)) {
			return null;
		} else {
			return getUnderlyingInfo().iterator().next().getDiagramElement();
		}
	}
}