package org.kite9.diagram.visualization.compaction.align;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.display.CompleteDisplayer;


/**
 * At the moment, this passes through each aligner in turn, from top-to-bottom of the diagram. 
 */
public class AlignmentCompactionStep extends AbstractCompactionStep {

	Aligner[] aligners;

	@Nullable
	@Override
	public String getPrefix() {
		return "ALN ";
	}

	public AlignmentCompactionStep(CompleteDisplayer cd, Aligner...aligners) {
		super(cd);
		this.aligners = aligners;
	}

	@Override
	public void compact(Compaction c, Embedding e, Compactor rc) {
		if  (e.isTopEmbedding()) {
			alignContents(c.getHorizontalSegmentSlackOptimisation().getTheDiagram(), c);
		}
	}

	protected void alignContents(Container de, Compaction c) {
		List<DiagramElement> contents = de.getContents();
		
		for (Aligner a : aligners) {
			alignOnAxis(c, contents, a, true, de);
			alignOnAxis(c, contents, a, false, de);
		}
		
		for (DiagramElement de2 : contents) {
			if (de2 instanceof Container) {
				alignContents((Container) de2, c);
			}
		}
	}


	public void alignOnAxis(Compaction c, List<DiagramElement> contents, Aligner a, boolean horizontal, Container de) {
		Set<Rectangular> filtered = contents.stream()
			.filter(e -> (e instanceof Rectangular))
			.map(e -> (Rectangular) e)
			.filter(e -> a.willAlign(e, horizontal))
			.filter(e -> (e instanceof Connected))
			.collect(Collectors.toSet());
		
		if (filtered.size() > 0) {
			a.alignFor(de, filtered, c, horizontal);
		}
	}
}
