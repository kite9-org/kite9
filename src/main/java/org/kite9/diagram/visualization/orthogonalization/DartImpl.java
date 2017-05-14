package org.kite9.diagram.visualization.orthogonalization;

import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.AbstractEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.logging.LogicException;

/**
 * A Dart represents a horizontal or vertical extent of an edge or shape perimeter.  
 *  * 
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
class DartImpl extends AbstractEdge implements Dart {

	private OrthogonalizationImpl o;
	
	@Override
	public String toString() {
		return "["+getFrom()+"-"+getTo()+"-"+drawDirection+"]";
	}

	/**
	 * Constructor is in Orthogonalization
	 */
	DartImpl(Vertex from, Vertex to, Map<DiagramElement, Direction> partOf, Direction d, String label, OrthogonalizationImpl o) {
		super(from, to, d);
				
		if (partOf != null) {
			this.underlyings.putAll(partOf);
		}
		this.o = o;
		this.changeCost = DartImpl.VERTEX_DART_PRESERVE;
		this.setID(label);
		from.addEdge(this);
		to.addEdge(this);
	}
	
	Map<DiagramElement, Direction> underlyings = new HashMap<>();

	public static final int CHANGE_EARLY_FROM = 64;
	public static final int CHANGE_EARLY_TO = 128;
	public static final int VERTEX_LENGTH_KNOWN = 256;
	
	public static final int COST_MASK = 63;
	
	private int changeCost = VERTEX_DART_PRESERVE;

	@Override
	public int getChangeCost() {
		return changeCost & COST_MASK;
	}
	
	@Override
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
		
	@Override
	public boolean isDirected() {
		return false;
	}

	@Override
	public int getBendCost() {
		return 0;
	}
	
	@Override
	public int getCrossCost() {
		return 0;
	}
	
	@Override
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
	
	@Override
	public Map<DiagramElement, Direction> getDiagramElements() {
		return underlyings;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return underlyings.containsKey(de);
	}
	
	Direction orthPreference;

	@Override
	@Deprecated  // figure out what to do with this.
	public void setOrthogonalPositionPreference(Direction d) {
		this.orthPreference = d;
	}
	
}
