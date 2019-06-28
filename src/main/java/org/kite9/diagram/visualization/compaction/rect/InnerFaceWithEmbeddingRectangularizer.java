package org.kite9.diagram.visualization.compaction.rect;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;

/**
 * First-pass rectangularization only does square faces with content in them.
 * 
 * @author robmoffat
 *
 */
public class InnerFaceWithEmbeddingRectangularizer extends AbstractRectangularizer {

	public InnerFaceWithEmbeddingRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}
	
	@Override
	public void compact(Compaction c, Embedding r, Compactor rc) {
		log.send("InnerFaceWithEmbeddingRectangularizer Of: "+r);
		super.compact(c, r, rc);
	}

	
	@Override
	protected List<DartFace> selectFacesToRectangularize(Compaction c, List<DartFace> faces) {
		return faces.stream()
				.filter(df -> df.getContainedFaces().size() > 0)
				.collect(Collectors.toList());
	}

	/**
	 * This version will remove any element from the map where there are more than 4 turns (i.e.
	 * it's not an initial rectangle anyway).
	 */
	@Override
	protected void performFaceRectangularization(Compaction c, Map<DartFace, List<VertexTurn>> stacks) {
		for (Iterator<Entry<DartFace, List<VertexTurn>>> iterator = stacks.entrySet().iterator(); iterator.hasNext();) {
			Entry<DartFace, List<VertexTurn>> elem = iterator.next();
			if (elem.getValue().size() > 4) {
				iterator.remove();
			}
		}
	}

	
}
