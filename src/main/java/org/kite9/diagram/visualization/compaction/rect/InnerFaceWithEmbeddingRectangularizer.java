package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.stream.Collectors;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;

/**
 * First-pass rectangularization only does the inner faces which have content in them:
 * we need to know the size of those in order to calculate the size of the outer faces.
 * 
 * That means we need to rectangularize, and embed the contained faces first.
 * 
 * @author robmoffat
 *
 */
public class InnerFaceWithEmbeddingRectangularizer extends MidSideCheckingRectangularizer {

	public InnerFaceWithEmbeddingRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}
	
	@Override
	public void compact(Compaction c, Embedding r, Compactor rc) {
		log.send("InnerFaceWithEmbeddingRectangularizer Of: "+r);
		super.compact(c, r, rc);
	}

	
	@Override
	protected List<DartFace> selectFacesToRectangularize(List<DartFace> faces) {
		return faces.stream().filter(df -> df.getContainedFaces().size() > 0).collect(Collectors.toList());
	}

	
}
