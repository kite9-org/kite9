package org.kite9.diagram.visualization.orthogonalization;

import java.io.Serializable;
import java.util.List;

import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Face;


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
		return dartsInFace.toString();
	}
	
	public DartFace(Face f, boolean outerFace) {
		this.f = f;
		this.outerFace = outerFace;
		this.faceDepth = f instanceof Face ? getFaceDepth((Face) f) : Integer.MAX_VALUE;
	}

	private int getFaceDepth(Face f2) {
		if (f2.getContainedBy() == null) {
			return 0;
		} else {
			return 1 + getFaceDepth(f2.getContainedBy());
		}
	}

	private Face f;
	
	public List<DartDirection> dartsInFace;
	
	public boolean outerFace;
	
	private int faceDepth;

	
	public Face getUnderlying() {
		return f;
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
