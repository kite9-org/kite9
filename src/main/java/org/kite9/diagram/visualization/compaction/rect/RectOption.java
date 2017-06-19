package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;

public class RectOption implements Comparable<RectOption> {

	private final VertexTurn vt1;
	private final VertexTurn vt2;
	private final VertexTurn vt3;
	private final VertexTurn vt4;
	private final VertexTurn vt5;
	private final Match m;
	private int initialScore;
	private final int i;
	private final List<VertexTurn> fromStack;

	public Match getMatch() {
		return m;
	}

	public RectOption(int i, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, List<VertexTurn> fromStack) {
		super();
		this.vt1 = vt1;
		this.vt2 = vt2;
		this.vt3 = vt3;
		this.vt4 = vt4;
		this.vt5 = vt5;
		this.m = m;
		this.initialScore = getScore();
		this.i = i;
		this.fromStack = fromStack;
	}
	
	public List<VertexTurn> getStack() {
		return fromStack;
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
		int out = Integer.compare(this.initialScore, o.initialScore);
		if (out != 0) {
			return out;
		} else {
			return Integer.compare(this.i, o.i);
		}
	}

	public String toString() {
		return "[RO: ("+initialScore+") extender = " + getExtender().getSegment() + "]"; 
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
	
	/**
	 * This basically says whether we can rectangularize without causing meets to grow IF par is shorter 
	 * or the same length as meets.
	 */
	public boolean isSizingSafe() {
		return getPost().getDirection() == getExtender().getDirection();
		
	}

}