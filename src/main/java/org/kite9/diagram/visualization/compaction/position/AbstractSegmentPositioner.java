package org.kite9.diagram.visualization.compaction.position;

import java.util.List;

import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

public abstract class AbstractSegmentPositioner implements Logable, CompactionStep {

	protected Kite9Log log = new Kite9Log(this);

	public AbstractSegmentPositioner() {
		super();
	}
		
	/**
	 * Sets the positions of each dart, whilst simultaneously checking that all the darts have a valid length.
	 */
	protected boolean setAndCheckDartPositions(List<Segment> horizontal2) {
		for (Segment segment : horizontal2) {
			for (Dart d : segment.getDartsInSegment()) {
				Vertex from = d.getFrom();
	    	    Vertex to = d.getTo();
	    	    Dimension2D fromri = from.getPosition();
	    	    Dimension2D tori = to.getPosition();

	    	    double x1 = fromri.x();
	    	    double y1 = fromri.y();
	    	    double x2 = tori.x();
	    	    double y2 = tori.y();

	    	    RectangleRenderingInformation ri = (RectangleRenderingInformation) d.getRenderingInformation();
	    	    ri.setPosition(new Dimension2D(x1, y1));
	    	    ri.setSize(new Dimension2D(x2, y2));
	    	    
	    	    double xc = x2 - x1;
	    	    double yc = y2 - y1;

	    	    if ((d.getDrawDirection()==Direction.RIGHT) && (xc<0)) {
					throw new LogicException("Dart length invalid: "+d+" "+xc+" segment "+segment);
				}
				
				if ((d.getDrawDirection()==Direction.LEFT) && (xc>0)) {
					throw new LogicException("Dart length invalid: "+d+" "+xc+" segment "+segment);
				}
				
				if ((d.getDrawDirection()==Direction.UP) && (yc>0)) {
					throw new LogicException("Dart length invalid: "+d+" "+yc+" segment "+segment);
				}
				
				if ((d.getDrawDirection()==Direction.DOWN) && (yc<0)) {
					throw new LogicException("Dart length invalid: "+d+" "+yc+" segment "+segment);
				}
			}
		}
		return true;
	}

	public String getPrefix() {
		return "SEGP";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}