package org.kite9.diagram.visualization.compaction.rect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.model.position.Turn;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.logging.LogicException;

/**
 * Looks through the stack of items to rectangularize, and on finding
 * candidates, prioritises them in order of the ones that will distort the final
 * diagram least.
 * 
 * @author robmoffat
 * 
 */
public abstract class PrioritizingRectangularizer extends AbstractRectangularizer {

	public PrioritizingRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

	public enum Match {
		A, D
	};

	@Override
	protected void performFaceRectangularization(Compaction c, Map<DartFace, List<VertexTurn>> stacks) {
		PriorityQueue<RectOption> pq = new PriorityQueue<RectOption>(500);
		Set<VertexTurn> onStack = new UnorderedSet<VertexTurn>();

		createInitialRectOptions(c, stacks, pq, onStack);
		
		while (pq.size() > 0) {
//			log.send("Horizontal Segments:", c.getHorizontalSegmentSlackOptimisation().getAllSlideables());
//			log.send("Vertical Segments:", c.getVerticalSegmentSlackOptimisation().getAllSlideables());

			
			RectOption ro = pq.remove();
			List<VertexTurn> theStack = ro.getStack();
			Action action = checkRectOptionIsOk(onStack, ro, pq, c);
			switch (action) {
			case OK:
				performChange(c, pq, onStack, ro, theStack);
				break;
			case PUT_BACK:
				log.send(log.go() ? null : "Putting back: " + ro);
				ro.rescore();
				pq.add(ro);
				break;
			case DISCARD:
				log.send(log.go() ? null : "Discarding: " + ro);
				// do nothing
			} 			
		}
		
		createInitialRectOptions(c, stacks, pq, onStack);
		if (pq.size() > 0) {
			throw new LogicException("Should have completed rectangularization - throwing options away");
		}
	}

	private void createInitialRectOptions(Compaction c, Map<DartFace, List<VertexTurn>> stacks, PriorityQueue<RectOption> pq, Set<VertexTurn> onStack) {
		for (List<VertexTurn> theStack : stacks.values()) {
			for (int i = 0; i < theStack.size(); i++) {
				addNewRectOptions(c, theStack, pq, i);
				onStack.addAll(theStack);
			}
		}
	}

	protected void performChange(Compaction c, PriorityQueue<RectOption> pq, Set<VertexTurn> onStack, RectOption ro, List<VertexTurn> theStack) {
		// log.send(log.go() ? null : "Queue Currently: ",pq);
		log.send(log.go() ? null : "Change: " + ro);
		if (ro.getMatch() == Match.A) {
			performRectangularizationA(theStack, c, ro.getMeets(), ro.getLink(), ro.getPar(), ro.getExtender(), ((PrioritisedRectOption) ro).getTurnShape());
			onStack.remove(ro.getLink());
			onStack.remove(ro.getPar());
		} else {
			performRectangularizationD(theStack, c, ro.getExtender(), ro.getPar(), ro.getLink(), ro.getMeets(), ((PrioritisedRectOption) ro).getTurnShape());
			onStack.remove(ro.getLink());
			onStack.remove(ro.getPar());
		}

		int fromIndex = theStack.indexOf(ro.getVt1()) - 4;
		afterChange(c, pq, theStack, fromIndex);
	}

	protected void afterChange(Compaction c, PriorityQueue<RectOption> pq, List<VertexTurn> theStack, int fromIndex) {
		// find more matches
		for (int i = fromIndex; i <= fromIndex + 8; i++) {
			addNewRectOptions(c, theStack, pq, i);
		}
	}

	private void addNewRectOptions(Compaction c, List<VertexTurn> theStack,
			PriorityQueue<RectOption> pq, int i) {
		EnumSet<Match> m = findPattern(theStack, c, i);
		if (m != null) {
			for (Match match : m) {
				RectOption ro = createRectOption(theStack, i, match, c);
				pq.add(ro);
				log.send(log.go() ? null : "Added option: "+ro);
			}
		}
	}
	
	enum Action { DISCARD, PUT_BACK, OK};

	protected Action checkRectOptionIsOk(Set<VertexTurn> onStack, RectOption ro, PriorityQueue<RectOption> pq, Compaction c) {
		boolean allThere = onStack.contains(ro.getExtender()) && onStack.contains(ro.getMeets()) && onStack.contains(ro.getPar())
				&& onStack.contains(ro.getLink()) && onStack.contains(ro.getPost());
		if (!allThere) {
			log.send(log.go() ? null : "Discarding: " + ro);
			return Action.DISCARD;
		}
		
		if (((PrioritisedRectOption) ro).getType() != ((PrioritisedRectOption)ro).calculateType()) {
			return Action.PUT_BACK;
		}

		if ((ro.getScore() != ro.getInitialScore())) {
			// change it and throw it back in
			return Action.PUT_BACK;
		}
		
		if (pq.size()>0) {
			RectOption top = pq.peek();
			if (ro.compareTo(top) == 1) {
				return Action.PUT_BACK;
			}
		}
		
		EnumSet<Match> m =matchTurns(ro.getVt1(), ro.getVt2(), ro.getVt3(), ro.getVt4(), ro.getVt5());
		if (!m.contains(ro.getMatch())) {
			return Action.DISCARD;
		}
		
		return Action.OK;
		
	}

	/**
	 * Examines a particular rotation pattern on the stack and returns a
	 * RectOption for it if it can be rectangularized.
	 */
	protected EnumSet<Match> findPattern(List<VertexTurn> stack, Compaction c, int index) {
		if (stack.size() < 4)
			return null;

		// get top four items up to the index on the stack
		VertexTurn vt5 = getItemRotating(stack, index);
		VertexTurn vt4 = getItemRotating(stack, index - 1);
		VertexTurn vt3 = getItemRotating(stack, index - 2);
		VertexTurn vt2 = getItemRotating(stack, index - 3);
		VertexTurn vt1 = getItemRotating(stack, index - 4);
//		log.send(log.go() ? null : "Checking turns at index ending " + index);

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
	
	int rectOptionNo = 0;

	public RectOption createRectOption(List<VertexTurn> stack, int index, Match m, Compaction c) {
		VertexTurn vt5 = getItemRotating(stack, index);
		VertexTurn vt4 = getItemRotating(stack, index - 1);
		VertexTurn vt3 = getItemRotating(stack, index - 2);
		VertexTurn vt2 = getItemRotating(stack, index - 3);
		VertexTurn vt1 = getItemRotating(stack, index - 4);
		return new PrioritisedRectOption(rectOptionNo++, vt1, vt2, vt3, vt4, vt5, m, stack, this);
	}
}