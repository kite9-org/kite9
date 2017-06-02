package org.kite9.diagram.visualization.compaction.rect;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;

public class RectOption implements Comparable<RectOption> {

	private final PrioritizingRectangularizer rectangularizer;
	private final VertexTurn vt1;
	private final VertexTurn vt2;
	private final VertexTurn vt3;
	private final VertexTurn vt4;
	private final VertexTurn vt5;
	private final Match m;
	private final Compaction c;
	private int initialScore;

	public Match getMatch() {
		return m;
	}

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
		this.initialScore = getScore();
	}

	public VertexTurn getMeets() {
		return m == Match.A ? vt4 : vt2;
	}

	public VertexTurn getExtender() {
		return m == Match.A ? vt1 : vt5;
	}

	public VertexTurn getPar() {
		return m == Match.A ? vt2 : vt4;
	}

	public VertexTurn getLink() {
		return m == Match.A ? vt3 : vt3;
	}

	public VertexTurn getPost() {
		return m == Match.A ? vt5 : vt1;
	}

	public boolean isStillValid() {
		return true;
	}

	public int getScore() {
		return 0;
	}

	public int getInitialScore() {
		return initialScore;
	}

	public void rescore() {
		this.initialScore = getScore();
	}

	@Override
	public int compareTo(RectOption o) {
		return 0;
		// // check for priority change
		// if (isPriority != o.isPriority) {
		// return -((Boolean)isPriority).compareTo(o.isPriority);
		// }
		//
		// // pick lowest cost arrangements first
		// if (scoreJoin != o.scoreJoin) {
		// return ((Integer) scoreJoin).compareTo(o.scoreJoin);
		// }
		//
		// if (scoreJoin > 0) {
		// if (o.pushOut != pushOut) {
		// // use the one that wrecks length the least
		// return ((Double)pushOut).compareTo(o.pushOut);
		// }
		//
		// }
		//
		// // preserve link length if precious
		// return -((Integer)
		// getLink().getChangeCost()).compareTo(o.getLink().getChangeCost());
	}

	public String toString() {
		return "[RO: extender = " + getExtender() + "]"; // + " score = " +
															// scoreJoin + "
															// priority = " +
															// isPriority + "
															// rect_safe =
															// "+rectSafe+"
															// can_boxout? =
															// "+canBoxout+"
															// rect?=
															// "+chooseRectangularization()+"
															// push =
															// "+pushOut+"/"+availableMeets
															// + " length =
															// "+length+"]";
	}

	public VertexTurn getVt1() {
		return vt1;
	}

	public VertexTurn getVt2() {
		return vt2;
	}

	public VertexTurn getVt3() {
		return vt3;
	}

	public VertexTurn getVt4() {
		return vt4;
	}

	public VertexTurn getVt5() {
		return vt5;
	}

}