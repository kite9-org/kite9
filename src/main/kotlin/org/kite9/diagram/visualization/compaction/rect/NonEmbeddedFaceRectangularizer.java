package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.stream.Collectors;

import org.kite9.diagram.common.objects.Rectangle;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.FaceSide;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;

public class NonEmbeddedFaceRectangularizer extends MidSideCheckingRectangularizer {

	public NonEmbeddedFaceRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Embedding r, Compactor rc) {
		log.send("NonEmbeddedFaceRectangularizer: "+r);
		super.compact(c, r, rc);
	}

	@Override
	protected List<DartFace> selectFacesToRectangularize(Compaction c, List<DartFace> faces) {
		return faces.stream().filter(df -> {
			Rectangle<FaceSide> r = c.getFaceSpace(df);
			return r == null;	
		}).collect(Collectors.toList());
		
	}
}
