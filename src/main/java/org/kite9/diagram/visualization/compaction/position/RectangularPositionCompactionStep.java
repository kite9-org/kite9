package org.kite9.diagram.visualization.compaction.position;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

public class RectangularPositionCompactionStep extends AbstractCompactionStep {

	public RectangularPositionCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		if (r instanceof Diagram) {
			compactInternal(c, r);
		}
	}

	private void compactInternal(Compaction c, Rectangular r) {
		OPair<Slideable<Segment>> y = c.getHorizontalSegmentSlackOptimisation().getSlideablesFor(r);
		OPair<Slideable<Segment>> x = c.getVerticalSegmentSlackOptimisation().getSlideablesFor(r);
		double xMin = x.getA().getMinimumPosition();
		double xMax = x.getB().getMinimumPosition();
		double yMin = y.getA().getMinimumPosition();
		double yMax = y.getB().getMinimumPosition();
		
		RectangleRenderingInformation rri = r.getRenderingInformation();
		rri.setPosition(new Dimension2D(xMin, yMin));
		rri.setSize(new Dimension2D(xMax - xMin, yMax - yMin));
		
		if (r instanceof Container) {
			for (DiagramElement de : ((Container) r).getContents()) {
				if ((de instanceof Label) || (de instanceof Connected)) {
					compactInternal(c, (Rectangular) de); 
				}
			}
		}
	}

	@Override
	public String getPrefix() {
		return "RPCS";
	}
	

}
