package org.kite9.diagram.visualization.compaction.rect;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

/**
 * This makes sure that compaction proceeds bottom-up through the diagram.
 * 
 * @author robmoffat
 *
 */
public class HierarchicalCompactionStep extends AbstractCompactionStep {

	public HierarchicalCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		if (r instanceof Container) {
			for (DiagramElement de : ((Container) r).getContents()) {
				if ((de instanceof Connected) || (de instanceof Label)) {
					log.send("Compacting: "+de);
					rc.compact((Rectangular) de, c);
				}
			}
		}
	}
	
	
	
	@Override
	public String getPrefix() {
		return "HCS ";
	}

}
