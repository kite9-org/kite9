package org.kite9.diagram.visualization.compaction.rect;

import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
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
	public void compact(Compaction c, Embedding e, Compactor rc) {
		for (Embedding e2: e.getInnerEmbeddings()) {
			log.send("Compacting: "+e2);
			rc.compact(e2, c);
		}
	}
	
	
	
	@Override
	public String getPrefix() {
		return "HCS ";
	}

}
