package org.kite9.diagram.visualization.planarization.rhd.links;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.grid.GridPositionerImpl;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.serialization.IntegerRangeValue;

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
		} else {
			// this will only get called when we are adding an illegal.
			// however, this would be setting a contradiction on a layout, so 
			// we should do nothing here.
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
			DiagramElement from = c.getFrom();
			DiagramElement to = c.getTo();
			
			while (true) {
				Container fromC = ((Connected)from).getContainer();
				Container toC = ((Connected)to).getContainer();
				
				if (fromC == toC) {
					Layout l = fromC.getLayout();
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
							checkOrdinalContradiction(l, drawDirection, (Connected) from, (Connected) to, fromC, c);
							return;
						}
					}
				}
				
				if ((fromC.getContainer() == toC.getContainer()) && (fromC.getContainer().getLayout() == Layout.GRID)) {
					// do special grid checking
					switch (c.getDrawDirection()) {
					case LEFT:
						gridPositionAfterOrContradiction(GridPositionerImpl.getXOccupies(fromC), GridPositionerImpl.getXOccupies(toC), c);
						gridPositionOverlapOrContradiction(GridPositionerImpl.getYOccupies(fromC), GridPositionerImpl.getYOccupies(toC), c);
						break;
					case RIGHT:
						gridPositionAfterOrContradiction(GridPositionerImpl.getXOccupies(toC), GridPositionerImpl.getXOccupies(fromC), c);
						gridPositionOverlapOrContradiction(GridPositionerImpl.getYOccupies(fromC), GridPositionerImpl.getYOccupies(toC), c);
						break;
					case UP:
						gridPositionAfterOrContradiction(GridPositionerImpl.getYOccupies(fromC), GridPositionerImpl.getYOccupies(toC), c);
						gridPositionOverlapOrContradiction(GridPositionerImpl.getXOccupies(fromC), GridPositionerImpl.getXOccupies(toC), c);
						break;
					case DOWN:
						gridPositionAfterOrContradiction(GridPositionerImpl.getYOccupies(toC), GridPositionerImpl.getYOccupies(fromC), c);
						gridPositionOverlapOrContradiction(GridPositionerImpl.getXOccupies(fromC), GridPositionerImpl.getXOccupies(toC), c);
						break;
					}
				}
				
				int depthFrom = em.getContainerDepth(fromC);
				int depthTo = em.getContainerDepth(toC);
				if (depthFrom < depthTo) {
					to = toC;
				} else if (depthFrom > depthTo) {
					from = fromC;
				} else {
					to = toC;
					from = fromC;
				}
			}
		}
	}

	private void gridPositionOverlapOrContradiction(IntegerRangeValue a, IntegerRangeValue b, Connection c) {
		boolean fromInside = (a.getFrom() <= b.getFrom()) && (a.getFrom() >= b.getTo());
		boolean toInside = (a.getTo() <= b.getFrom()) && (a.getTo() >= b.getTo());
		if (!(fromInside || toInside)) {
			setContradiction(c);
		} 
	}

	private void gridPositionAfterOrContradiction(IntegerRangeValue a, IntegerRangeValue b, Connection c) {
		if (a.getTo() < b.getFrom()) {
			setContradiction(c);
		}
	}

	@Override
	public void checkOrdinalContradiction(Layout l, Direction d, Connected from, Connected to, Container fromC, Connection c) {
		Direction ld = GroupPhase.getDirectionForLayout(l, true);		// TODO: probably need to fix later.
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
