package org.kite9.diagram.visualization.compaction.rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;

/**
 * This makes sure that compaction proceeds bottom-up through the diagram.
 * 
 * @author robmoffat
 *
 */
public class HierarchicalCompactionStep implements CompactionStep {

	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		if (r instanceof Container) {
			for (DiagramElement de : ((Container) r).getContents()) {
				if ((de instanceof Connected) || (de instanceof Label)) {
					System.out.println("Compacting: "+de);
					rc.compact((Rectangular) de, c);
				}
			}
		}
	}
	
	/**
	 * This labels pairs of attr in the diagram with the alignment they should have,
	 * and assigns a unique priority to the attr in the diagram, so that those with lowest
	 * priority number receive preference on size.
	 */
	public void orderDiagramElementSizes(SegmentSlackOptimisation opt) {
		opt.updatePositionalOrdering();
		List<OPair<Slideable>> toDo = new ArrayList<>(opt.getRectangularSlideablePairs());
		
		Collections.sort(toDo, new Comparator<OPair<Slideable>>() {

			@Override
			public int compare(OPair<Slideable> o1, OPair<Slideable> o2) {
				int distO1 = getDist1(o1);
				int distO2 = getDist1(o2);
				return ((Integer)distO1).compareTo(distO2);
			}

			private int getDist1(OPair<Slideable> o1) {
				Slideable a = o1.getA();
				Slideable b = o1.getB();
				if ((a == null) || (b == null)) {
					return Integer.MAX_VALUE;
				}
				
				return Math.abs(a.getPositionalOrder() - b.getPositionalOrder());
			}
			
		});
		
		setSizes(opt, toDo, DiagramElementSizing.MINIMIZE);
		setSizes(opt, toDo, DiagramElementSizing.MAXIMIZE);
	}

	private void setSizes(SegmentSlackOptimisation opt, List<OPair<Slideable>> toDo, DiagramElementSizing minimize) {
		// TODO Auto-generated method stub
		
	}

}
