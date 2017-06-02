package org.kite9.diagram.visualization.compaction.rect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.model.position.Turn;
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
public class PrioritizingRectangularizer extends AbstractRectangularizer {

	public PrioritizingRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

	public enum Match {
		A, D
	};

	@Override
	protected void performFaceRectangularization(Compaction c, List<Dart> result, List<VertexTurn> theStack) {
		PriorityQueue<RectOption> pq = new PriorityQueue<RectOption>(theStack.size());
		Set<VertexTurn> onStack = new UnorderedSet<VertexTurn>(theStack);
		for (int i = 0; i < theStack.size(); i++) {
			addNewRectOptions(c, result, theStack, pq, i);
		}
		

 
		while (pq.size() > 0) {
			RectOption ro = pq.remove();
			boolean ok = checkRectOptionIsOk(onStack, ro, pq);
			if (ok) { 
				log.send(log.go() ? null : "Queue Currently: ",pq);
				log.send(log.go() ? null : "Change: " + ro);
				if (ro.getMatch() == Match.A) {
					performRectangularizationA(theStack, c, result, ro.getMeets(), ro.getLink(), ro.getPar(), ro.getExtender());
					onStack.remove(ro.getLink());
					onStack.remove(ro.getPar());
				} else {
					performRectangularizationD(theStack, c, result, ro.getExtender(), ro.getPar(), ro.getLink(), ro.getMeets());
					onStack.remove(ro.getLink());
					onStack.remove(ro.getPar());
				}

				int fromIndex = theStack.indexOf(ro.getVt1())-4;

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

	private boolean checkRectOptionIsOk(Set<VertexTurn> onStack, RectOption ro, PriorityQueue<RectOption> pq) {
		boolean allThere = onStack.contains(ro.getExtender()) && onStack.contains(ro.getMeets()) && onStack.contains(ro.getPar())
				&& onStack.contains(ro.getLink()) && onStack.contains(ro.getPost());
		if (!allThere) {
			log.send(log.go() ? null : "Discarding: " + ro);
			return false;
		}
		
		if ((ro.getScore() != ro.getInitialScore())) {
			// change it and throw it back in
			log.send(log.go() ? null : "Putting back: " + ro);
			ro.rescore();
			pq.add(ro);
			return false;
		}
		
		if (pq.size()>0) {
			RectOption top = pq.peek();
			if (ro.compareTo(top) == 1) {
				ro.rescore();
				log.send(log.go() ? null : "Putting back: " + ro);
				pq.add(ro);
				return false;
			}
		}
		
		EnumSet<Match> m =matchTurns(ro.getVt1(), ro.getVt2(), ro.getVt3(), ro.getVt4(), ro.getVt5());
		if (!m.contains(ro.getMatch())) {
			log.send(log.go() ? null : "Discarding: " + ro);
			return false;
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
		return new RectOption(this, vt1, vt2, vt3, vt4, vt5, m, c);
	}
}
