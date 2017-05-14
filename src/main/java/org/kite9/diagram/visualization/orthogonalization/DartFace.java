package org.kite9.diagram.visualization.orthogonalization;

import java.io.Serializable;
import java.util.List;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;


/**
 * Stores details of a face made up of darts.  The darts form a perimeter to the face.
 * This is now extended so that if a face contains subgraphs, these are also held as dart-faces
 * 
 * 
 * @author robmoffat
 *
 */
public class DartFace implements Serializable, Comparable<DartFace> {

	private static final long serialVersionUID = -4395910839686963521L;
    
    public static class DartDirection {
    	
    	public DartDirection(Dart d, Direction dir) {
			super();
			this.dart = d;
			this.dir = dir;
		}

		Dart dart;
    	public Dart getDart() {
			return dart;
		}

		public Direction getDirection() {
			return dir;
		}

		Direction dir;
    	
		public void setDirection(Direction dir) {
			this.dir = dir;
		}

		@Override
		public String toString() {
			return "DartDirection [d=" + dart + ", dir=" + dir + "]";
		}
    	
    }

	@Override
	public String toString() {
		return "DartFace: "+id+"-"+(outerFace ? "outer, inside "+containedBy.id : "inner") +": "+dartsInFace.toString();
	}
	
	public DartFace(int i, Rectangular partOf, boolean outerFace) {
		this.partOf = partOf;
		this.id = i;
		this.outerFace = outerFace;
		this.faceDepth = partOf != null ? partOf.getDepth() : -1;
	}
	
	private final int id;
	
	private final Rectangular partOf;
	
	public List<DartDirection> dartsInFace;
	
	public final boolean outerFace;
	
	private final int faceDepth;

	private DartFace containedBy;
	
	public DartFace getContainedBy() {
		return containedBy;
	}

	public void setContainedBy(DartFace containedBy) {
		this.containedBy = containedBy;
	}

	public Rectangular getPartOf() {
		return partOf;
	}

	public Vertex getStartVertex() {
		DartDirection d1 = dartsInFace.get(0);
		if (d1.dir == d1.dart.getDrawDirection()) {
			return d1.dart.getFrom();
		} else {
			return d1.dart.getTo();
		}
	}

	@Override
	public int compareTo(DartFace o) {
		return ((Integer)faceDepth).compareTo(o.faceDepth);
	}
	
}
