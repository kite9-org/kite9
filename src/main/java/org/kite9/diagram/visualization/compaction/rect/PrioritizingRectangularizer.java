package org.kite9.diagram.visualization.compaction.rect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Turn;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.framework.logging.LogicException;

/**
 * Looks through the stack of items to rectangularize, and on finding
 * candidates, prioritises them in order of the ones that will distort the final
 * diagram least.
 * 
 * @author robmoffat
 * 
 */
public class PrioritizingRectangularizer extends AbstractDartRectangularizer {

	public enum Match {
		A, D
	};

	public PrioritizingRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

	public class RectOption implements Comparable<RectOption> {

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

		public RectOption(VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, Compaction c) {
			super();
			this.vt1 = vt1;
			this.vt2 = vt2;
			this.vt3 = vt3;
			this.vt4 = vt4;
			this.vt5 = vt5;
			this.m = m;
			this.c = c;
			this.length = getLink().getUnderlying().getLength();
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
			boolean out = x.getUnderlying().isChangeEarly(m==Match.A ? x.endsWith : x.startsWith);
			return out;
		}
		
		public boolean canBoxout() {
			return getExtender().d == getPost().d;			
		}
		
		public double calcAvailableMeets() {
			Dart md = getMeets().getUnderlying();
			
			double minAdd;
			
			if (canBoxout()) {
				// no double-back dart
				minAdd = 0;
			} else {
				// need to include cost of double-back dart
				Direction d = getMeets().d;
				minAdd = getMinimumDistance(c, getPost().startsWith, getExtender().startsWith, d);
			}
	
			return md.getLength() - minAdd;
		}
		
		public boolean isRectangularizationSafe() {
			Dart pd = getPar().getUnderlying();
			pushOut = Math.max(0, pd.getLength() - availableMeets);
			
			if ((pd.isVertexLengthKnown())) {
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
			return (getMeets().getUnderlying().getChangeCost()==Dart.EXTEND_IF_NEEDED) 
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
				int changeCost = getMeets().getUnderlying().getChangeCost();
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
			return -((Integer) getLink().getUnderlying().getChangeCost()).compareTo(o.getLink().getUnderlying().getChangeCost());
		}

		public String toString() {
			return "[RO: extender = " + getExtender().getUnderlying() + " score = " + scoreJoin + " priority = " + isPriority + " rect_safe = "+rectSafe+" can_boxout? = "+canBoxout+" rect?= "+chooseRectangularization()+" push = "+pushOut+"/"+availableMeets + " length = "+length+"]";
		}
	}

	@Override
	protected void performFaceRectangularization(Compaction c, List<Dart> result, List<VertexTurn> theStack) {
		PriorityQueue<RectOption> pq = new PriorityQueue<RectOption>(theStack.size());
		HashSet<VertexTurn> onStack = new HashSet<VertexTurn>(theStack);
		for (int i = 0; i < theStack.size(); i++) {
			addNewRectOptions(c, result, theStack, pq, i);
		}
		


		while (pq.size() > 0) {
			RectOption ro = pq.remove();
			boolean ok = checkRectOptionIsOk(onStack, ro, pq);
			if (ok) {
				log.send(log.go() ? null : "Queue Currently: ",pq);
				log.send(log.go() ? null : "Change: " + ro);
				if (ro.m == Match.A) {
					
					if (ro.willRect) {
						performRectangularizationA(theStack, c, result, ro.getMeets(), ro.getLink(), ro.getPar(), ro.getExtender());
						onStack.remove(ro.getLink());
						onStack.remove(ro.getPar());
					} else {
						Vertex parFrom = ro.getPar().startsWith;
						Vertex meetsFrom = ro.getMeets().endsWith;
						VertexTurn newLink = performPopOut(c, result, ro.getMeets(), ro.getLink(), ro.getPar(), ro.getExtender(), parFrom, meetsFrom, theStack, Match.A);
						onStack.remove(ro.getLink());
						onStack.add(newLink);
						
					} 
					
				} else {
					if (ro.willRect) {
						performRectangularizationD(theStack, c, result, ro.getExtender(), ro.getPar(), ro.getLink(), ro.getMeets());
						onStack.remove(ro.getLink());
						onStack.remove(ro.getPar());
					} else { 
						Vertex parFrom = ro.getPar().endsWith;
						Vertex meetsFrom = ro.getMeets().startsWith;
						VertexTurn newLink = performPopOut(c, result, ro.getMeets(), ro.getLink(), ro.getPar(), ro.getExtender(), parFrom, meetsFrom, theStack, Match.D);
						onStack.remove(ro.getLink());
						onStack.add(newLink);
					}
				}

				int fromIndex = theStack.indexOf(ro.vt1)-4;

				// find more matches
				for (int i = fromIndex; i <= fromIndex + 8; i++) {
					addNewRectOptions(c, result, theStack, pq, i);
				}

			}
		}
	}

	private void addNewRectOptions(Compaction c, List<Dart> result, List<VertexTurn> theStack,
			PriorityQueue<RectOption> pq, int i) {
		EnumSet<Match> m = findPattern(theStack, c, result, i);
		if (m != null) {
			for (Match match : m) {
				RectOption ro = createRectOption(theStack, i, match, c);
				pq.add(ro);
				log.send(log.go() ? null : "Added option: "+ro);
			}
		}
	}

	private boolean checkRectOptionIsOk(HashSet<VertexTurn> onStack, RectOption ro, PriorityQueue<RectOption> pq) {
		boolean allThere = onStack.contains(ro.getExtender()) && onStack.contains(ro.getMeets()) && onStack.contains(ro.getPar())
				&& onStack.contains(ro.getLink()) && onStack.contains(ro.getPost());
		if (!allThere) {
			log.send(log.go() ? null : "Discarding: " + ro);
			return false;
		}
		
		if ((ro.scoreJoin != ro.scoreJoin()) || (ro.isPriority != ro.isPriority)) {
			// change it and throw it back in
			log.send(log.go() ? null : "Putting back: " + ro);
			ro.scoreJoin = ro.scoreJoin();
			pq.add(ro);
			return false;
		}
		
		if (pq.size()>0) {
			RectOption top = pq.peek();
			if (ro.compareTo(top) == 1) {
				ro.scoreJoin = ro.scoreJoin();
				log.send(log.go() ? null : "Putting back: " + ro);
				pq.add(ro);
				return false;
			}
		}
		
		EnumSet<Match> m =matchTurns(ro.vt1, ro.vt2, ro.vt3, ro.vt4, ro.vt5);
		if (!m.contains(ro.m)) {
			log.send(log.go() ? null : "Discarding: " + ro);
			return false;
		}
		
		if (!ro.willRect) {
			// we're supposed to be doing a box-out
			if (!ro.canBoxout()) {
				return false;
			}
		}
		
		return true;
		
	}

	/**
	 * Examines a particular rotation pattern on the stack and returns a
	 * RectOption for it if it can be rectangularized.
	 */
	protected EnumSet<Match> findPattern(List<VertexTurn> stack, Compaction c, List<Dart> out, int index) {
		if (stack.size() < 4)
			return null;

		// get top four items up to the index on the stack
		VertexTurn vt5 = getItemRotating(stack, index);
		VertexTurn vt4 = getItemRotating(stack, index - 1);
		VertexTurn vt3 = getItemRotating(stack, index - 2);
		VertexTurn vt2 = getItemRotating(stack, index - 3);
		VertexTurn vt1 = getItemRotating(stack, index - 4);
		log.send(log.go() ? null : "Checking turns at index ending " + index);

		return matchTurns(vt1, vt2, vt3, vt4, vt5);
	}

	private EnumSet<Match> matchTurns(VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5) {
		EnumSet<Match> out = EnumSet.noneOf(Match.class);
		List<Turn> turns = new ArrayList<Turn>();
		turns.add(getTurn(vt1, vt2));
		turns.add(getTurn(vt2, vt3));
		turns.add(getTurn(vt3, vt4));
		turns.add(getTurn(vt4, vt5));

		if (turns.toString().contains("STRAIGHT")) {
			throw new LogicException("You cannot connect two disparate segments with a straight line");
		} else if (turns.toString().contains("BACK")) {
			throw new LogicException("You cannot connect two disparate segments with a back line");
		}

		if (turnMatch(turns.get(0), turns.get(1), turns.get(2), patternA)) {
			out.add(Match.A);
		} 
		
		if (turnMatch(turns.get(1), turns.get(2), turns.get(3), patternD)) {
			out.add(Match.D);
		} 
		
		return out;
	}
	
	public boolean turnMatch(Turn t1, Turn t2, Turn t3, List<Turn> turns) {
		return turns.get(0).equals(t1) && turns.get(1).equals(t2) && turns.get(2).equals(t3);
	}

	public RectOption createRectOption(List<VertexTurn> stack, int index, Match m, Compaction c) {
		VertexTurn vt5 = getItemRotating(stack, index);
		VertexTurn vt4 = getItemRotating(stack, index - 1);
		VertexTurn vt3 = getItemRotating(stack, index - 2);
		VertexTurn vt2 = getItemRotating(stack, index - 3);
		VertexTurn vt1 = getItemRotating(stack, index - 4);
		return new RectOption(vt1, vt2, vt3, vt4, vt5, m, c);
	}
}
