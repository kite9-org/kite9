package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

public abstract class AbstractSizingCompactionStep extends AbstractCompactionStep {

	public AbstractSizingCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Embedding e, Compactor rc) {
		if (e.isTopEmbedding()) {
			size(c, true);
			size(c, false);
		}
	}

	private void size(Compaction c, boolean horizontal) {
		c.getHorizontalSegments().stream()
			.flatMap(s -> s.getUnderlyingInfo().stream())
			.map(ui -> ui.getDiagramElement())
			.filter(de -> de instanceof Rectangular)
			.map(de -> (Rectangular) de)
			.filter(r -> filter(r))
			.distinct()
			.sorted((a, b) -> compare(a,b,c, horizontal))
			.forEach(r -> performSizing(r, c, horizontal));
	}

	public abstract boolean filter(Rectangular r);

	public abstract int compare(Rectangular a, Rectangular b, Compaction c, boolean horizontal);

	public abstract void performSizing(Rectangular r, Compaction c, boolean horizontal);
	
}