package org.kite9.diagram.visualization.compaction.rect;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;
import org.kite9.diagram.visualization.orthogonalization.Dart;

public class RectOption implements Comparable<RectOption> {

		private final PrioritizingRectangularizer rectangularizer;
		VertexTurn vt1;
		VertexTurn vt2;
		VertexTurn vt3;
		VertexTurn vt4;
		VertexTurn vt5;
		Match m;
		int scoreJoin = 0;
		boolean isPriority = false;
		boolean rectSafe = false;
		boolean canBoxout = false;
		boolean willRect = false;
		double pushOut = 0;
		double length = 0;
		double availableMeets = 0;
		Compaction c;

		public RectOption(PrioritizingRectangularizer prioritizingRectangularizer, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, Compaction c) {
			super();
			this.rectangularizer = prioritizingRectangularizer;
			this.vt1 = vt1;
			this.vt2 = vt2;
			this.vt3 = vt3;
			this.vt4 = vt4;
			this.vt5 = vt5;
			this.m = m;
			this.c = c;
			this.length = getLink().getMinimumLength();
			// note - we need to store the initial values so that the sort is always correct
			this.isPriority = isPriority();
			this.canBoxout = canBoxout();
			this.availableMeets = calcAvailableMeets();
			this.rectSafe = isRectangularizationSafe();
			this.willRect = chooseRectangularization();
			this.scoreJoin = scoreJoin();
		}
		
		public VertexTurn getMeets() {
			return m==Match.A ? vt4 : vt2;
		}
		
		public VertexTurn getExtender() {
			return m==Match.A ? vt1 : vt5;
		}
		
		public VertexTurn getPar() {
			return m==Match.A ? vt2 : vt4;
		}
		
		public VertexTurn getLink() {
			return m==Match.A ? vt3 : vt3;
		}
		
		public VertexTurn getPost() {
			return m==Match.A ? vt5 : vt1;
		}

		public boolean isPriority() {
			VertexTurn x = getExtender();
			boolean out = m==Match.A ? x.isChangeEarlyEnd() : x.isChangeEarlyStart();
			return out;
		}
		
		public boolean canBoxout() {
			return false;
			
//			return getExtender().getSegment() == getPost().getSegment();			
		}
		
		public double calcAvailableMeets() {
			
			double minAdd;
			
			if (canBoxout()) {
				// no double-back dart
				minAdd = 0;
			} else {
				// need to include cost of double-back dart
				Direction d = getMeets().getDirection();
				minAdd = rectangularizer.getMinimumDistance(getPost().getStartsWith(), getExtender().getStartsWith(), d);
			}
	
			return getMeets().getMinimumLength() - minAdd;
		}
		
		public boolean isRectangularizationSafe() {
			pushOut = Math.max(0, getPar().getMinimumLength() - availableMeets);
			
			if ((getPar().isLengthKnown())) {
				if (pushOut <= 0) {
					return true;
				}
			}	
			
			return false;
		}
		
		/**
		 * Decide whether to rectangularize, or boxout.
		 */
		private boolean chooseRectangularization() {
			return (getMeets().getChangeCost()==Dart.EXTEND_IF_NEEDED) 
				|| (rectSafe)
				|| (isPriority) 
				|| (!canBoxout);
		}

		
		/**
		 * Lower scores are better
		 */
		public int scoreJoin() {
			if (rectSafe) {
				return 0;
			} else {
				int changeCost = getMeets().getChangeCost();
				if (willRect) {
					return changeCost;				
				} else {
					return Math.min(changeCost, 1);  // option of creating the box-out has a cost of 1.
				}
			}
		}
		
		@Override
		public int compareTo(RectOption o) {
			// check for priority change
			if (isPriority != o.isPriority) {
				return -((Boolean)isPriority).compareTo(o.isPriority);
			}
		
			// pick lowest cost arrangements first
			if (scoreJoin != o.scoreJoin) {
				return ((Integer) scoreJoin).compareTo(o.scoreJoin);
			}
			
			if (scoreJoin > 0) {
				if (o.pushOut != pushOut) {
					// use the one that wrecks length the least
					return ((Double)pushOut).compareTo(o.pushOut);
				}
				
			}

			// preserve link length if precious
			return -((Integer) getLink().getChangeCost()).compareTo(o.getLink().getChangeCost());
		}

		public String toString() {
			return "[RO: extender = " + getExtender() + " score = " + scoreJoin + " priority = " + isPriority + " rect_safe = "+rectSafe+" can_boxout? = "+canBoxout+" rect?= "+chooseRectangularization()+" push = "+pushOut+"/"+availableMeets + " length = "+length+"]";
		}
	}