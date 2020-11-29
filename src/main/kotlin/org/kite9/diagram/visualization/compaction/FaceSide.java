package org.kite9.diagram.visualization.compaction;

import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.visualization.compaction.segment.Segment;

public class FaceSide {
	
	private final Slideable<Segment> main;
	
	private final Set<Slideable<Segment>> all;

	public FaceSide(Slideable<Segment> main, Set<Slideable<Segment>> others) {
		super();
		this.main = main;
		this.all = others;
	}

	public Slideable<Segment> getMain() {
		return main;
	}

	public Set<Slideable<Segment>> getAll() {
		return all;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((main == null) ? 0 : main.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FaceSide other = (FaceSide) obj;
		if (main == null) {
			if (other.main != null)
				return false;
		} else if (!main.equals(other.main))
			return false;
		return true;
	}
	
}