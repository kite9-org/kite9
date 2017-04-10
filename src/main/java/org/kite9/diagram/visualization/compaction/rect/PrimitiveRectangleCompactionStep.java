package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.framework.common.Kite9ProcessingException;

public class PrimitiveRectangleCompactionStep extends AbstractCompactionStep {

	public PrimitiveRectangleCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		if (r.getSizing() == DiagramElementSizing.FIXED) {
			List<DartFace> internalFaces = c.getDartFacesForRectangular(r)
					.stream().filter(f -> !f.outerFace).collect(Collectors.toList());
			
			if (internalFaces.size() != 1) {
				throw new Kite9ProcessingException("Was expecting a single internal face for fixed element");
			}
			
			
			OPair<Slideable> lr = c.getXSlackOptimisation().getSlideablesFor(r);
			OPair<Slideable> ud = c.getYSlackOptimisation().getSlideablesFor(r);
			if ((lr != null) && (ud != null)) {
				// sometimes, we might not display everything (e.g. labels)
				CostedDimension cd = displayer.size(r, CostedDimension.UNBOUNDED);
				c.getXSlackOptimisation().ensureMinimumDistance(lr.getA(), lr.getB(), (int) cd.getWidth());
				c.getYSlackOptimisation().ensureMinimumDistance(ud.getA(), ud.getB(), (int) cd.getHeight());
				log.send("Setting initial size for "+r+" face = "+internalFaces.get(0).getUnderlying().id);
			}
		}
	}

	@Override
	public String getPrefix() {
		return "PECS";
	}

}
