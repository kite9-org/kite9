package org.kite9.diagram.visualization.orthogonalization.flow.balanced;

import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.model.Terminator;

public class TerminatorPair extends Pair<Terminator>{

	public TerminatorPair(Terminator a, Terminator b) {
		super(a, b);
	}

	@Override
	protected boolean elementEquals(Object a2, Object a3) {
		return ((Terminator)a2).styleMatches((Terminator)a3);
	}

	
}
