package org.kite9.diagram.visualization.orthogonalization;

import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.common.elements.ArtificialElement;
import org.kite9.diagram.common.elements.edge.AbstractEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

/**
 * A Dart represents a horizontal or vertical extent of an edge or shape perimeter.  
 * 
 * Darts have a minimum length, which is respected by the compaction process.
 * 
 * Darts also have the 'fixedLength' identifier.  This is set for darts representing
 * the perimeter of vertices, as we would like rectangularization not to increase the
 * length of the dart.
 * 
 * Darts also have a orthogonal position preference direction.  This is used when compacting
 * to indicate whether the dart wants to be pushed orthogonally in a particular
 * direction.  
 * 
 * 
 * @author robmoffat
 *
 */
public class Dart extends AbstractEdge {

	private OrthogonalizationImpl o;
	
	@Override
	public String toString() {
		return "["+getFrom()+"-"+getTo()+"-"+drawDirection+"]";
	}

	/**
	 * Constructor is in Orthogonalization
	 */
	Dart(Vertex from, Vertex to, PlanarizationEdge partOf, Direction d, String label, OrthogonalizationImpl o) {
		super(from, to, d);
		if (partOf==null) {
			throw new IllegalArgumentException("Trying to create a dart with partOf not a diagram element or artificial element");
		}
				
		this.partOf = partOf;
		this.o = o;
		this.changeCost = Dart.VERTEX_DART_PRESERVE;
		this.setID(label);
		from.addEdge(this);
		to.addEdge(this);
	}
	
	private PlanarizationEdge partOf;
	
	public static final int EXTEND_IF_NEEDED = 0;
	public static final int CONNECTION_DART_FAN = 2;	
	public static final int VERTEX_DART_GROW = 3;
 	public static final int CONNECTION_DART = 4;	
	public static final int VERTEX_DART_PRESERVE = 5;

	public static final int CHANGE_EARLY_FROM = 64;
	public static final int CHANGE_EARLY_TO = 128;
	public static final int VERTEX_LENGTH_KNOWN = 256;
	
	public static final int COST_MASK = 63;
	
	private int changeCost = VERTEX_DART_PRESERVE;

	public int getChangeCost() {
		return changeCost & COST_MASK;
	}
	
	public boolean isChangeEarly(Vertex end) {
		if (end == from) {
			return (changeCost & CHANGE_EARLY_FROM) == CHANGE_EARLY_FROM;
		} else if (end == to) {
			return (changeCost & CHANGE_EARLY_TO) == CHANGE_EARLY_TO;
		} else {
			throw new LogicException("end passed in is not actually on the dart");
		}
	}
	
	public void setChangeCost(int changeCost, Vertex changeEarlyEnd) {
		this.changeCost = changeCost;
		if (changeEarlyEnd == from) {
			this.changeCost += CHANGE_EARLY_FROM;
		} else if (changeEarlyEnd == to) {
			this.changeCost += CHANGE_EARLY_TO;
		}
	}
	
	public void setChangeCostChangeEarlyBothEnds(int changeCost) {
		this.changeCost = changeCost + CHANGE_EARLY_FROM + CHANGE_EARLY_TO;
	}
		
	public boolean isDirected() {
		return false;
	}

	/**
	 * This is when the dart represents part of an edge or vertex.
	 */
	public PlanarizationEdge getUnderlying() {
		return partOf;
	}

	public int getBendCost() {
		return 0;
	}
	
	public int getCrossCost() {
		return 0;
	}
	
	public void setDrawDirection(Direction drawDirection) {
		this.drawDirection = drawDirection;
	}

	@Override
	public void setFrom(Vertex v) {
		if (o != null) {
			o.unlinkDartFromMap(this);
		}
		super.setFrom(v);
		if (o != null) {
			o.relinkDartInMap(this);
		}
	}

	@Override
	public void setTo(Vertex v) {
		if (o != null) {
			o.unlinkDartFromMap(this);
		}
		super.setTo(v);
		if (o != null) {
			o.relinkDartInMap(this);
		}
	}

	public void remove() {
		o.unlinkDartFromMap(this);
		from.removeEdge(this);
		to.removeEdge(this);
		o.allDarts.remove(this);
	}
	
	public Set<DiagramElement> getDiagramElements() {
		if (partOf instanceof PlanarizationEdge) {
			return ((PlanarizationEdge) partOf).getDiagramElements().keySet();
		} else if (partOf instanceof DiagramElement) {
			return Collections.singleton((DiagramElement) partOf);
		} else {
			throw new Kite9ProcessingException("Don't know underlying "+partOf);
		}
	}

	public DiagramElement getOriginalUnderlying() {
		if (partOf instanceof Edge) {
			return ((Edge) partOf).getOriginalUnderlying();
		} else if (partOf instanceof Vertex) {
			return ((Vertex) partOf).getOriginalUnderlying();
		}
		
		return (DiagramElement) partOf;
	}

	public void reverseDirection() {
		Vertex temp = from;
		from = to;
		to = temp;
		drawDirection = Direction.reverse(drawDirection);
	}

	RectangleRenderingInformation rri = new RectangleRenderingInformationImpl();
	
	public RenderingInformation getRenderingInformation() {
		return rri;
	}

	private Direction orthogonalPositionPreference = null;

	public Direction getOrthogonalPositionPreference() {
		return orthogonalPositionPreference;
	}

	public void setOrthogonalPositionPreference(Direction orthogonalPositionPreference) {
		this.orthogonalPositionPreference = orthogonalPositionPreference;
	}

	@Override
	public boolean isReversed() {
		return false;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		if (partOf instanceof ArtificialElement) {
			return ((ArtificialElement) partOf).isPartOf(de);
		} else {
			return false;
		}
	}
	
}
