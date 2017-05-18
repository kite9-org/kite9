package org.kite9.diagram.visualization.orthogonalization;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.common.Kite9ProcessingException;


/**
 * Stores details of a face made up of darts.  The darts form a perimeter to the face.
 * This is now extended so that if a face contains subgraphs, these are also held as dart-faces
 * 
 * 
 * @author robmoffat
 *
 */
public class DartFace implements Serializable {

	private static final long serialVersionUID = -4395910839686963521L;
    
    public static class DartDirection {
    	
    	public DartDirection(Dart d, Direction dir) {
			super();
			this.dart = d;
			this.dir = dir;
		}

		final Dart dart;
		final Direction dir;
    	
		public Dart getDart() {
			return dart;
		}

		public Direction getDirection() {
			return dir;
		}

		@Override
		public String toString() {
			return "DartDirection [d=" + dart + ", dir=" + dir + "]";
		}
    	
    }

	@Override
	public String toString() {
		String containedByStr = containedBy == null ? "-" : ""+containedBy.id;
		return "DartFace: "+id+"-"+(outerFace ? "outer, inside "+containedByStr : "inner") +": "+dartsInFace.toString();
	}
	
	public DartFace(int i, boolean outerFace, List<DartDirection> dartsInFace) {
		this.id = i;
		this.outerFace = outerFace;
		this.dartsInFace = dartsInFace;
	}
	
	private final int id;
	
	public int getId() {
		return id;
	}

	private final List<DartDirection> dartsInFace;
	
	public final boolean outerFace;
	
	private DartFace containedBy;
	private Set<DartFace> containing = new HashSet<>();
	
	public DartFace getContainedBy() {
		return containedBy;
	}

	public void setContainedBy(DartFace containedBy) {
		if (!outerFace) {
			throw new Kite9ProcessingException();
		}
		
		if (this.containedBy != null) {
			throw new Kite9ProcessingException();
		}
		
		if (containedBy != null) {
			this.containedBy = containedBy;
			this.containedBy.containing.add(this);
		}
	}

	public Vertex getStartVertex() {
		DartDirection d1 = dartsInFace.get(0);
		if (d1.dir == d1.dart.getDrawDirection()) {
			return d1.dart.getFrom();
		} else {
			return d1.dart.getTo();
		}
	}

	public Set<DartFace> getContainedFaces() {
		return containing;
	}

	public List<DartDirection> getDartsInFace() {
		return dartsInFace;
	}
	
}
