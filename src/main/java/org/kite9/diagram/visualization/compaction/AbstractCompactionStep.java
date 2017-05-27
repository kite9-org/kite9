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

	protected double getMinimumDistance(Slideable<Segment> froms, Slideable<Segment> tos, Direction d) {
		return froms.minimumDistanceTo(tos);
	}

	protected double getMinimumDistance(boolean horizontalDart, Segment froms, Segment tos, Segment along, boolean concave) {
		// where a segment is part of a grid, we can have more than one underlying diagram element.
		// in these cases, we have to consider pairs.
		if ((froms.getUnderlyingInfo().size() > 1) && (tos.getUnderlyingInfo().size() > 1)) {
			Set<DiagramElement> diagramElements = new HashSet<>();
			froms.getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).forEach(a -> diagramElements.add(a));
			tos.getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).forEach(a -> diagramElements.add(a));
			double max = 0;
			
			for (DiagramElement diagramElement : diagramElements) {
				UnderlyingInfo fromUI = getUnderlyingFor(froms, diagramElement);
				UnderlyingInfo toUI = getUnderlyingFor(tos, diagramElement);
				if ((fromUI != null) && (toUI != null)) {
					max = Math.max(max, getMinimumDistance(horizontalDart, fromUI, toUI, along, concave));
				}
			}
			
			return max;
			
		} else if (froms.getUnderlyingInfo().size() == 1) {
			return getMinimumDistance(horizontalDart, froms.getUnderlyingInfo().iterator().next(), tos, along, concave);
		} else {	
			return getMinimumDistance(horizontalDart, tos.getUnderlyingInfo().iterator().next(), froms, along, concave);
		}
	}
	
	private double getMinimumDistance(boolean horizontalDart, UnderlyingInfo fromUI, Segment tos, Segment along, boolean concave) {
		double max = 0;
		for (UnderlyingInfo toUI : tos.getUnderlyingInfo()) {
			max = Math.max(max, getMinimumDistance(horizontalDart, fromUI, toUI, along, concave));
		}
		
		return max;
	}

	private double getMinimumDistance(boolean horizontalDart, UnderlyingInfo fromUI, UnderlyingInfo toUI, Segment along, boolean concave) {
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
		
		DiagramElement alongDe = getAlongDiagramElement(along);
		
		
		return displayer.getMinimumDistanceBetween(fromde, fromUnderlyingSide, tode, toUnderlyingSide, horizontalDart ? Direction.RIGHT : Direction.DOWN, alongDe, concave);
	}

	private DiagramElement getAlongDiagramElement(Segment along) {
		return along == null ? null : along.getSingleUnderlying();
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
	
	private DiagramElement moveUp(DiagramElement move, int toDepth, int cDepth) {
		while (cDepth > toDepth) {
			move = move.getParent();
			cDepth--;
		}
		
		return move;
	}
 
	private boolean contains(DiagramElement a, DiagramElement b) {
		int ad = a.getDepth();
		int bd = b.getDepth();
		
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
	protected void separate(Slideable<Segment> s1, Slideable<Segment> s2, Direction d, Compaction c, Segment along) {
		boolean horizontal = d == Direction.LEFT || d == Direction.RIGHT;
		double minDistance = getMinimumDistance(horizontal, s1.getUnderlying(), s2.getUnderlying(), along, true);
		s1.getSlackOptimisation().ensureMinimumDistance(s1, s2, (int) minDistance);
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
