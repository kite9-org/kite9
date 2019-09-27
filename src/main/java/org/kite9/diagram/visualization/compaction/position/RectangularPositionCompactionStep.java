package org.kite9.diagram.visualization.compaction.position;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.framework.logging.LogicException;

public class RectangularPositionCompactionStep extends AbstractCompactionStep {

	public RectangularPositionCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Embedding r, Compactor rc) {
		if (r.isTopEmbedding()) {
			compactInternal(c);
		}
	}

	private void compactInternal(Compaction c) {
		Set<Rectangular> done = new HashSet<>();
		
		for (Segment s : c.getHorizontalSegments()) {
			for (UnderlyingInfo ui : s.getUnderlyingInfo()) {
				DiagramElement de = ui.getDiagramElement();
				if ((de instanceof Rectangular) &&(!done.contains(de))) {
					Rectangular r = (Rectangular) de;
					done.add(r);
					setRectanularRenderingInformation(r, c);
				}
			}
		}
	}
		 
	private void setRectanularRenderingInformation(Rectangular r, Compaction c) {
		OPair<Slideable<Segment>> y = c.getHorizontalSegmentSlackOptimisation().getSlideablesFor(r);
		OPair<Slideable<Segment>> x = c.getVerticalSegmentSlackOptimisation().getSlideablesFor(r);
		if ((x != null) && (y != null)) {
			double xMin = x.getA().getMinimumPosition();
			double xMax = x.getB().getMinimumPosition();
			double yMin = y.getA().getMinimumPosition();
			double yMax = y.getB().getMinimumPosition();
			
			RectangleRenderingInformation rri = r.getRenderingInformation();
			rri.setPosition(new Dimension2D(xMin, yMin));
			Dimension2D size = new Dimension2D(xMax - xMin, yMax - yMin);
			
			if ((size.getWidth() < 0) || (size.getHeight() < 0)) {
				throw new LogicException("Slideable issue");
			}
			rri.setSize(size);
		}
	}

	@Override
	public String getPrefix() {
		return "RPCS";
	}
	

}
