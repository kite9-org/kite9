package org.kite9.diagram.visualization.planarization.rhd.links;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.grid.GridPositionerImpl;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.common.range.IntegerRange;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.logging.LogicException;

public class BasicContradictionHandler implements Logable, ContradictionHandler {

	private Kite9Log log = new Kite9Log(this);
	ElementMapper em;

	public BasicContradictionHandler(ElementMapper em) {
		this.em = em;
	}

	@Override
	public void setContradicting(Iterable<? extends BiDirectional<Connected>> connections, boolean dontRender) {
		for (BiDirectional<Connected> bic : connections) {
			setContradiction(bic, dontRender);
		}
	}

	@Override
	public void setContradiction(BiDirectional<Connected> bic, boolean dontRender) {
		log.error("Contradiction: " + bic);
		if (bic instanceof Connection) {
			Tools.setConnectionContradiction((Connection) bic, true, !dontRender);
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
		} else if (ld2 == null) {
			return ld1.getDirection();
		} else {
			return checkContradiction(ld1.getDirection(), ld1.isOrderingLink(), ld1.getLinkRank(), ld1.getConnections(), ld2.getDirection(), ld2.isOrderingLink(), ld2.getLinkRank(),
					ld2.getConnections(), containerLayout);
		}
	}

	@Override
	public Direction checkContradiction(Direction ad, boolean aOrdering, int aRank, Iterable<? extends BiDirectional<Connected>> ac, Direction bd, boolean bOrdering, int bRank,
			Iterable<? extends BiDirectional<Connected>> bc, Layout containerLayout) {

		if (containerLayout != null) {
			switch (containerLayout) {
			case HORIZONTAL:
				if (GroupPhase.isVerticalDirection(ad)) {
					setContradicting(ac, false);
				}
				if (GroupPhase.isVerticalDirection(bd)) {
					setContradicting(bc, false);
				}
				break;
			case VERTICAL:
				if (GroupPhase.isHorizontalDirection(ad)) {
					setContradicting(ac, false);
				}
				if (GroupPhase.isHorizontalDirection(bd)) {
					setContradicting(bc, false);
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
			setContradicting(bc, false);
			return ad;
		} else if (bOrdering && !aOrdering) {
			setContradicting(ac, false);
			return bd;
		} else if (!bOrdering && !aOrdering) {
			if (aRank >= bRank) {
				setContradicting(bc, false);
				return ad;
			} else {
				setContradicting(ac, false);
				return bd;
			}
		} else {
			throw new LogicException("Contradicting, ordering direction: " + ad + " " + bd);
		}

	}

	/**
	 * Simple test to make sure that c doesn't contradict the direction of the
	 * top-level container it passes through. Or, that something is connecting
	 * to an element inside itself.
	 */
	@Override
	public void checkForContainerContradiction(Connection c) {
		Direction drawDirection = c.getDrawDirection();
		DiagramElement from = c.getFrom();
		DiagramElement to = c.getTo();
		
		if (from == to) {
			setContradiction(c, true);
		}
		
		if ((from instanceof Diagram) || (to instanceof Diagram)) {
			setContradiction(c, true);
			return;
		}
		
		if (((Connected) from).getContainer().getLayout() == Layout.GRID) {
			setContradiction(c, true);
			return;
		}
		
		if (((Connected) to).getContainer().getLayout() == Layout.GRID) {
			setContradiction(c, true);
			return;
		}
		

		while (true) {
			Container fromC = ((Connected) from).getContainer();
			Container toC = ((Connected) to).getContainer();

			if (drawDirection != null) {

				// directed connections breaking normal layouts
				if (fromC == toC) {
					Layout l = fromC.getLayout();
					if (l == null) {
						return;
					} else {
						switch (l) {
						case HORIZONTAL:
							verticalContradiction(c, drawDirection);
							break;
						case VERTICAL:
							horizontalContradiction(c, drawDirection);
							break;
						case UP:
						case DOWN:
						case LEFT:
						case RIGHT:
							checkOrdinalContradiction(l, drawDirection, (Connected) from, (Connected) to, fromC, c);
							break;
						case GRID:
							gridContradiction(c, drawDirection, (Connected) from, (Connected) to);
							break;
						}
					}
				}
			}
			
			// check for illegal containment
			if (to instanceof Container) {
				if (((Container) to).getContents().contains(from)) {
					setContradiction(c, true);
				}
			}

			if (from instanceof Container) {
				if (((Container) from).getContents().contains(to)) {
					setContradiction(c, true);
				}
			}
			
			if (fromC == toC) {
				return;
			}

			int depthFrom = fromC.getDepth();
			int depthTo = toC.getDepth();
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

	private void gridContradiction(Connection c, Direction drawDirection, Connected fromC, Connected toC) {
		
		// do special grid checking
		switch (drawDirection) {
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


	private void gridPositionOverlapOrContradiction(IntegerRange a, IntegerRange b, Connection c) {
		boolean fromInside = (a.getFrom() <= b.getFrom()) && (a.getFrom() >= b.getTo());
		boolean toInside = (a.getTo() <= b.getFrom()) && (a.getTo() >= b.getTo());
		if (!(fromInside || toInside)) {
			setContradiction(c, false);
		}
	}

	private void gridPositionAfterOrContradiction(IntegerRange a, IntegerRange b, Connection c) {
		if (a.getTo() < b.getFrom()) {
			setContradiction(c, false);
		}
	}

	protected void checkOrdinalContradiction(Layout l, Direction d, Connected from, Connected to, Container fromC, Connection c) {
		Direction ld = GroupPhase.getDirectionForLayout(l);
		if (GroupPhase.isHorizontalDirection(ld) != GroupPhase.isHorizontalDirection(d)) {
			setContradiction(c, false);
			return;
		}

		// ld and d in the same axis
		boolean reversed = ld != d;
		int fromI = fromC.getContents().indexOf(from);
		int toI = fromC.getContents().indexOf(to);

		boolean contradiction = fromI < toI ? reversed : !reversed;
		if (contradiction)
			setContradiction(c, false);
	}

	private void horizontalContradiction(Connection c, Direction drawDirection) {
		if (GroupPhase.isHorizontalDirection(drawDirection)) {
			setContradiction(c, false);
		}
	}

	private void verticalContradiction(Connection c, Direction drawDirection) {
		if (GroupPhase.isVerticalDirection(drawDirection)) {
			setContradiction(c, false);
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
