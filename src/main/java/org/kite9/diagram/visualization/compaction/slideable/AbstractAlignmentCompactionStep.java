package org.kite9.diagram.visualization.compaction.slideable;

import java.util.List;

import org.kite9.diagram.model.CompactedRectangular;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

public abstract class AbstractAlignmentCompactionStep extends AbstractCompactionStep {

	public AbstractAlignmentCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Embedding e, Compactor rc) {
		if  (e.isTopEmbedding()) {
			alignRectangulars(c.getHorizontalSegmentSlackOptimisation().getTheDiagram(), c);
			alignConnections(c.getHorizontalSegments(), c);
			alignConnections(c.getVerticalSegments(), c);
		}
	}

	private void alignConnections(List<Segment> horizontalSegments, Compaction c) {
		horizontalSegments.stream()
			.filter(s -> s.getConnections().size() > 0)
			.forEach(s -> alignConnectionSegment(s, c));
	}

	protected abstract void alignConnectionSegment(Segment s,Compaction c);

	protected void alignRectangulars(Rectangular de, Compaction c) {
		alignRectangular(de, c);
		if (de instanceof Container) {
			((Container) de).getContents().stream()
			.filter(e -> hasSegments(e, c))
			.forEach(e -> alignRectangulars((Rectangular) e, c));
		}
		
	}

	private boolean hasSegments(DiagramElement e, Compaction c) {
		return (e instanceof CompactedRectangular); 
	}

	protected abstract void alignRectangular(Rectangular de, Compaction c);
}
