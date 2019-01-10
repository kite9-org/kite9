package org.kite9.diagram.visualization.compaction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.orthogonalization.DartFace;

/**
 * Embeddings are basically wrappers around outer faces in the diagram.  We compact outer faces from the 
 * bottom up.
 * 
 * @author robmoffat
 *
 */
public interface Embedding {

	/**
	 * Used in rectangularization
	 */
	List<DartFace> getDartFaces();
		
	List<Embedding> getInnerEmbeddings();
	
	public Collection<Segment> getVerticalSegments(Compaction c);

	public Collection<Segment> getHorizontalSegments(Compaction c);
	
	public boolean isTopEmbedding();

}
