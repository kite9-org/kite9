package org.kite9.diagram.visualization.planarization.rhd.links;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.BiDirectional;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.Connection;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.primitives.Container;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

public class BasicContradictionHandler implements Logable, ContradictionHandler {
	
	private Kite9Log log = new Kite9Log(this);
	ElementMapper em;
	
	public BasicContradictionHandler(ElementMapper em) {
		this.em = em;
	}

	@Override
	public void setContradicting(Iterable<BiDirectional<Connected>> connections) {
		for (BiDirectional<Connected> bic : connections) {
			setContradiction(bic);
		}
	}

	@Override
	public void setContradiction(BiDirectional<Connected> bic) {
		log.error("Contradiction: "+bic);
		if (bic instanceof Connection) {
			Tools.setConnectionContradiction((Connection)bic, true);
		}
	}

	@Override
	public Direction checkContradiction(LinkDetail ld1, LinkDetail ld2, Layout containerLayout) {
		if (ld1 == null) {
			return ld2.getDirection();
		} else if (ld2==null) {
			return ld1.getDirection();
		} else {
			return checkContradiction(
					ld1.getDirection(), ld1.isOrderingLink(), ld1.getLinkRank(), ld1.getConnections(), 
					ld2.getDirection(), ld2.isOrderingLink(), ld2.getLinkRank(), ld2.getConnections(), containerLayout);
		}
	}
	
	
	@Override
	public Direction checkContradiction(Direction ad, boolean aOrdering, int aRank, Iterable<BiDirectional<Connected>> ac,
			Direction bd, boolean bOrdering, int bRank, Iterable<BiDirectional<Connected>> bc, Layout containerLayout) {
		
		if (containerLayout != null) {
		switch (containerLayout) {
			case HORIZONTAL:
				if (GroupPhase.isVerticalDirection(ad)) {
					setContradicting(ac);
				}
				if (GroupPhase.isVerticalDirection(bd)) {
					setContradicting(bc);
				}
				break;
			case VERTICAL:
				if (GroupPhase.isHorizontalDirection(ad)) {
					setContradicting(ac);
				}
				if (GroupPhase.isHorizontalDirection(bd)) {
					setContradicting(bc);
				}
				break;		
			default:
			}
		}
		

		if (ad == bd) {
			return ad;
		} else if (ad == null) {
			return bd;
		} else if (bd == null) {
			return ad;
		} else if (aOrdering && !bOrdering) {
			setContradicting(bc);
			return ad;
		} else if (bOrdering && !aOrdering) {
			setContradicting(ac);
			return bd;
		} else if (!bOrdering && !aOrdering) {
			if (aRank >= bRank) {
				setContradicting(bc);
				return ad;
			} else {
				setContradicting(ac);
				return bd;
			}
		} else {
			throw new LogicException("Contradicting, ordering direction: " + ad + " " + bd);
		}

	}

	/**
	 * Simple test to make sure that c doesn't contradict the direction of the top-level container it passes through.
	 */
	@Override
	public void checkForContainerContradiction(Connection c) {
		Direction drawDirection = c.getDrawDirection();
		if (drawDirection != null) {
			Contained from = (Contained) c.getFrom();
			Contained to = (Contained) c.getTo();
			
			while (true) {
				Container fromC = ((Contained) from).getContainer();
				Container toC = ((Contained) to).getContainer();
				
				if (fromC == toC) {
					Layout l = fromC.getLayoutDirection();
					if (l==null) {
						return;
					} else {
						switch (l) {
						case HORIZONTAL:
							verticalContradiction(c, drawDirection);
							return;
						case VERTICAL:
							horizontalContradiction(c, drawDirection);
							return;
						case UP:
						case DOWN:
						case LEFT:
						case RIGHT:
							checkOrdinalContradiction(l, drawDirection, from, to, fromC, c);
							return;
						}
					}
				}
				
				int depthFrom = em.getContainerDepth(fromC);
				int depthTo = em.getContainerDepth(toC);
				if (depthFrom < depthTo) {
					to = (Contained)toC;
				} else if (depthFrom > depthTo) {
					from = (Contained)fromC;
				} else {
					to = (Contained)toC;
					from = (Contained)fromC;
				}
			}
		}
	}

	@Override
	public void checkOrdinalContradiction(Layout l, Direction d, Contained from, Contained to, Container fromC, Connection c) {
		Direction ld = GroupPhase.getDirectionForLayout(l);
		if (GroupPhase.isHorizontalDirection(ld) != GroupPhase.isHorizontalDirection(d)) {
			setContradiction(c);
			return;
		}
		
		// ld and d in the same axis
		boolean reversed = ld != d;
		int fromI = fromC.getContents().indexOf(from);
		int toI = fromC.getContents().indexOf(to);
		
		boolean contradiction = fromI < toI ? reversed : !reversed;
		if (contradiction)
			setContradiction(c);
	}
	
	private void horizontalContradiction(Connection c, Direction drawDirection) {
		if (GroupPhase.isHorizontalDirection(drawDirection)) {
			setContradiction(c);
		}
	}
	
	private void verticalContradiction(Connection c, Direction drawDirection) {
		if (GroupPhase.isVerticalDirection(drawDirection)) {
			setContradiction(c);
		}
	}


	
	@Override
	public String getPrefix() {
		return "CH  ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	
}
