package org.kite9.diagram.visualization.planarization.rhd.layout;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.hints.PositioningHints;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Provides the basic code for a top-down approach to laying out groups, but doesn't specify 
 * either the algorithm used to choose the approach or the ordering of the groups.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractTopDownLayoutStrategy implements LayoutStrategy, Logable {
	
	private static final double TOLERANCE = 0.0000001;

	
	public AbstractTopDownLayoutStrategy(RoutableHandler2D rh) {
		this.rh = rh;
	}
	
	Kite9Log log = new Kite9Log(this);
	
	protected RoutableHandler2D rh;

	private void chooseBestCompoundGroupPlacement(GroupPhase gp, GroupPhase.CompoundGroup gg) {
		rh.clearTempPositions(true);
		rh.clearTempPositions(false);
		GroupAxis gt = gg.getAxis();
		Layout ld = gg.getLayout();
		boolean canBeHoriz = gt.isHorizontal() && gt.isLayoutRequired();
		boolean canBeVert = gt.isVertical() && gt.isLayoutRequired();
		boolean horizLayoutUnknown = ((ld == null) || (ld == Layout.HORIZONTAL)) && canBeHoriz;
		boolean vertLayoutUnknown = ((ld == null) || (ld == Layout.VERTICAL)) && canBeVert;
		boolean canDecideLayout = (horizLayoutUnknown || vertLayoutUnknown);
		log.send("Group A: "+gg.getA());
		log.send("Group B: "+gg.getB());
		
		if (canDecideLayout) {
			boolean layoutNeeded = groupsNeedLayout(gg.getA(), gg.getB(), horizLayoutUnknown, vertLayoutUnknown, ld);

			if (layoutNeeded) {
				// this is the expensive part - layout is required. Choose one
				Layout hintedLayout = getHintedLayout(gg, canBeHoriz, canBeVert, ld);
				PlacementApproach best = null;
				best = tryPlacement(gp, gg, best, filterLayout(hintedLayout, horizLayoutUnknown, vertLayoutUnknown), canBeHoriz, canBeVert, true);
				best = tryPlacement(gp, gg, best, filterLayout(Layout.reverse(hintedLayout), horizLayoutUnknown, vertLayoutUnknown), canBeHoriz, canBeVert, false);
				best = tryPlacement(gp, gg, best, filterLayout(Layout.rotateClockwise(hintedLayout), horizLayoutUnknown, vertLayoutUnknown), canBeHoriz, canBeVert, false);
				best = tryPlacement(gp, gg, best, filterLayout(Layout.rotateAntiClockwise(hintedLayout), horizLayoutUnknown, vertLayoutUnknown), canBeHoriz, canBeVert, false);
	
				if (best != null) {
					best.choose();
				}
			} 
		
		} else { 
			PlacementApproach pa = createPlacementApproach(gp, gg, ld, canBeHoriz, canBeVert, true);
			pa.choose();
			log.send("Group layout = "+ld);
		}
	}

	private Layout filterLayout(Layout naturalLayout, boolean horiz, boolean vert) {
		switch (naturalLayout) {
		case LEFT:
		case RIGHT:
			return horiz ? naturalLayout : null;
		case DOWN:
		case UP:
			return vert ? naturalLayout : null;
		default:
			throw new LogicException("Layout should be definite for an approach: "+naturalLayout);
		}
	}
	
	/**
	 * Layout can be hinted either by the {@link PositioningHints} of the groups, or by the ordinal
	 * order of the elements in the container.  If the layout of the container is HORIZONTAL or
	 * VERTICAL, favour the ordinal, otherwise favour the {@link PositioningHints} where available.
	 */
	private Layout getHintedLayout(GroupPhase.CompoundGroup gg, boolean setHoriz, boolean setVert, Layout prescribed) {
		Integer bx = null;
		Integer by = null;
		Layout out = null;
		Group a = gg.getA();
		Group b = gg.getB();
		
		if (setVert) {
			if (prescribed == Layout.VERTICAL) {
				return getVerticalOrdinalLayout(a, b);
			} else if (prescribed == null) {
				by = PositioningHints.compareEitherYBounds(a.getHints(), b.getHints());
				out = getVerticalOrdinalLayout(a, b);
			}
		}
		
		if (setHoriz) {
			if (prescribed == Layout.HORIZONTAL) {
				return getHorizontalOrdinalLayout(a, b);
			} else if (prescribed == null) {
				bx = PositioningHints.compareEitherXBounds(a.getHints(), b.getHints());
				out = getHorizontalOrdinalLayout(a, b);
			}
		}
		
		if (bx != null) {
			if (1 == bx) {
				return Layout.LEFT;
			}else if (-1 == bx) {
				return Layout.RIGHT;
			}
		}
		
		if (by != null) {
			if (1 == by) {
				return Layout.UP;
			} else if (-1 == by) {
				return Layout.DOWN;
			}
		}
		
		return out;
	}

	private Layout getHorizontalOrdinalLayout(Group a, Group b) {
		return a.getGroupOrdinal() < b.getGroupOrdinal() ? Layout.RIGHT: Layout.LEFT;
	}

	private Layout getVerticalOrdinalLayout(Group a, Group b) {
		return a.getGroupOrdinal() < b.getGroupOrdinal() ? Layout.DOWN: Layout.UP;
	}

	private boolean groupsNeedLayout(Group a, Group b, boolean horizLayoutUnknown, boolean vertLayoutUnknown, Layout l) {
		if (groupsOverlap(a, b)) {
			return true;
		}

		boolean straightVerticals = groupsHaveStraightEdges(a, b, false);
		
		if ((horizLayoutUnknown || (l==Layout.VERTICAL)) && straightVerticals) {
			return true;
		}
		
		boolean straightHorizontals = groupsHaveStraightEdges(a, b, true);
		
		if ((vertLayoutUnknown || (l==Layout.HORIZONTAL)) && straightHorizontals) {
			return true;
		}
		
		return false;
	}

	private boolean groupsHaveStraightEdges(Group a, Group b, boolean horiz) {
		int mask = DirectedLinkManager.createMask(null, false, false, horiz ? Direction.LEFT : Direction.UP, horiz ? Direction.RIGHT : Direction.DOWN);
		boolean aHasLinks = a.getLinkManager().subset(mask).size() > 0;
		boolean bHasLinks = b.getLinkManager().subset(mask).size() > 0;
		return aHasLinks || bHasLinks;
	}

	private boolean groupsOverlap(Group a, Group b) {
		RoutingInfo ari = a.getAxis().getPosition(rh, true);
		RoutingInfo bri = b.getAxis().getPosition(rh, true);
		return rh.overlaps(ari, bri);
	}

	private PlacementApproach tryPlacement(GroupPhase gp, GroupPhase.CompoundGroup gg, PlacementApproach best, Layout d, boolean setHoriz, boolean setVert, boolean natural) {
		if ((d!=null) && ((best == null) || (best.getScore() > 0))) {
			PlacementApproach newpl = createPlacementApproach(gp, gg, d, setHoriz, setVert, natural);
			newpl.evaluate();
			log.send(log.go() ? null : gg.getGroupNumber() + " going " + d + "  score: " + newpl.getScore());
			
			if (best == null) {
				return newpl;
			} else if (best.getScore() <= newpl.getScore() + TOLERANCE) {
				return best;
			} else if (best.getScore() >= newpl.getScore() + TOLERANCE) {
				return newpl;
			} else {
				if (best.isNatural()) {
					return best;
				} else if (newpl.isNatural()) {
					return newpl;
				} else {
					return best;
				}
 			}
		}
		return best;
	}
	
	protected abstract PlacementApproach createPlacementApproach(GroupPhase gp, GroupPhase.CompoundGroup gg, Layout ld,
			boolean setHoriz, boolean setVert, boolean natural);

	private void chooseBestPlacement(GroupPhase gp, LayoutQueue lq) {
		Group g = lq.poll();
		while (g != null) {
			g.getAxis().getPosition(rh, false);
			log.send(log.go() ? null : "Ordering "+g.getGroupNumber()+ " size="+g.getSize()+ " links="+g.getLinkManager().getLinkCount());
			StringBuilder out = new StringBuilder(1000);
			log.send(out.toString());
			if (g instanceof CompoundGroup) {
				CompoundGroup cg = (CompoundGroup) g;
				chooseBestCompoundGroupPlacement(gp, cg);
				lq.complete(cg);
				lq.offer(cg.getA());
				lq.offer(cg.getB());
			}  
			
			g = lq.poll();
		}
	}
	
	
	
	@Override
	public String getPrefix() {
		return "TDLS";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	@Override
	public void layout(GroupPhase gp, GroupResult mr, LayoutQueue lq) {
		Group g = mr.groups().iterator().next();
		lq.offer(g);
		chooseBestPlacement(gp, lq);
		//rh.outputSettings();
	}

}
