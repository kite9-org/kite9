package org.kite9.diagram.visualization.compaction;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.Side;
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;


/**
 * This contains utility methods to deal with insertion of sub-graphs within the overall graph.
 * You should extend this wherever you need to add vertices to a segment.  
 */
public abstract class AbstractCompactionStep implements CompactionStep, Logable {

	protected Kite9Log log = new Kite9Log(this);
	
	protected CompleteDisplayer displayer;

	public AbstractCompactionStep(CompleteDisplayer cd) {
		this.displayer = cd;
	}
	
	@Override
	public String getPrefix() {
		return null;
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	protected double getMinimumDistance(Slideable froms, Slideable tos, Direction d) {
		return froms.minimumDistanceTo(tos);
	}

	protected double getMinimumDistance(boolean horizontalDart, Segment froms, Segment tos) {
		// where a segment is part of a grid, we can have more than one underlying diagram element.
		// in these cases, we have to consider pairs.
		if ((froms.getUnderlyingInfo().size() > 1) || (tos.getUnderlyingInfo().size() > 1)) {
			Set<DiagramElement> diagramElements = new HashSet<>();
			froms.getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).forEach(a -> diagramElements.add(a));
			tos.getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).forEach(a -> diagramElements.add(a));
			double max = 0;
			
			for (DiagramElement diagramElement : diagramElements) {
				UnderlyingInfo fromUI = getUnderlyingFor(froms, diagramElement);
				UnderlyingInfo toUI = getUnderlyingFor(tos, diagramElement);
				if ((fromUI != null) && (toUI != null)) {
					max = Math.max(max, getMinimumDistance(horizontalDart, fromUI, toUI));
				}
			}
			
			return max;
			
		} else {
			UnderlyingInfo fromUI = froms.getUnderlyingInfo().iterator().next();
			UnderlyingInfo toUI = tos.getUnderlyingInfo().iterator().next();
			return getMinimumDistance(horizontalDart, fromUI, toUI);
		}
	
	}

	private double getMinimumDistance(boolean horizontalDart, UnderlyingInfo fromUI, UnderlyingInfo toUI) {
		DiagramElement tode = toUI.getDiagramElement();
		Direction toUnderlyingSide = convertSideToDirection(horizontalDart, toUI.getSide());
		DiagramElement fromde = fromUI.getDiagramElement();
		Direction fromUnderlyingSide = convertSideToDirection(horizontalDart, fromUI.getSide());

		if (!needsLength(fromde, tode)) {
			return 0;
		}
				
		// side checking
		if ((fromUnderlyingSide!=null) && (toUnderlyingSide!=null)) {
			if (fromUnderlyingSide==toUnderlyingSide) {
				// check whether there is containment
				boolean containment =  contains(fromde, tode);
				
				if (!containment) {
					return 0;
				}
				
			}
		}
		
		
		return displayer.getMinimumDistanceBetween(fromde, fromUnderlyingSide, tode, toUnderlyingSide, horizontalDart ? Direction.RIGHT : Direction.DOWN);
	}
	
	private UnderlyingInfo getUnderlyingFor(Segment froms, DiagramElement diagramElement) {
		for (UnderlyingInfo ui : froms.getUnderlyingInfo()) {
			if (ui.getDiagramElement() == diagramElement) {
				return ui;
			}
		}
		
		return null;
	}

	private Direction convertSideToDirection(boolean horizontalDart, Side side) {
		switch (side) {
		case END:
			return horizontalDart ? Direction.RIGHT : Direction.DOWN;
		case START:
			return horizontalDart ? Direction.LEFT : Direction.UP;
		default:
			return null;
		}
	}

	private int getDepth(DiagramElement de) {
		DiagramElement c = de.getParent();
		if (c == null) {
			return 0;
		} else {
			return getDepth(c) + 1;
		}

	}
	
	private DiagramElement moveUp(DiagramElement move, int toDepth, int cDepth) {
		while (cDepth > toDepth) {
			move = move.getParent();
			cDepth--;
		}
		
		return move;
	}
 
	private boolean contains(DiagramElement a, DiagramElement b) {
		int ad = getDepth(a);
		int bd = getDepth(b);
		
		if ((ad < bd) && (a instanceof Container)) {
			// b might be in a
			b = moveUp(b, ad+1, bd);
			return ((Container)a).getContents().contains(b);
		} else if ((ad > bd) && (b instanceof Container)) {
			// a might be in b
			a = moveUp(a, bd+1, ad);
			return ((Container)b).getContents().contains(a);
		} else {
			return false;
		}
	}

	private boolean needsLength(DiagramElement a, DiagramElement b) {
		if ((a instanceof DirectionEnforcingElement) || (b instanceof DirectionEnforcingElement)) {
			return false;
		}
		
		return true;
	}
	
	
	
	
	
	/**
	 * Uses the SlackOptimisation to set a minimum distance between outside and inside parts.
	 */
	protected void separate(Slideable s1, Slideable s2, SegmentSlackOptimisation so, Direction d, Compaction c) {
		boolean horizontal = d == Direction.LEFT || d == Direction.RIGHT;
		double minDistance = getMinimumDistance(horizontal, (Segment) s1.getUnderlying(), (Segment) s2.getUnderlying());
		so.ensureMinimumDistance(s1, s2, (int) minDistance);
	}
//
//
//	private void separate(Vertex a, Segment to, Segment extend, Direction d, List<Dart> result, Compaction c) {
//		if (to.getVerticesInSegment().contains(a)) {
//			return;
//		}
//		
//		Vertex rv = c.createCompactionVertex(to, extend);
//		double length = getMinimumDistance(c, a, rv, d);
//		Dart da = c.getOrthogonalization().createDart(a, rv, null, d, length);
//		da.setChangeCost(Dart.EXTEND_IF_NEEDED, null);
//		result.add(da);
//	}
	
}
