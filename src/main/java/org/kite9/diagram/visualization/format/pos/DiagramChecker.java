package org.kite9.diagram.visualization.format.pos;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.xml.Diagram;
import org.kite9.framework.logging.LogicException;

public class DiagramChecker {
	
	public static interface ConnectionAction {
		
		public void action(RouteRenderingInformation rri, Object d, Connection c);
		
	}

	public static void checkConnnectionElements(Diagram d, final ConnectionAction ca) {
		DiagramElementVisitor dev = new DiagramElementVisitor();
		dev.visit(d, new VisitorAction() {

			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					Connection con = (Connection) de;

					try {
						RouteRenderingInformation rri = (RouteRenderingInformation) con.getRenderingInformation();
						Object routeDirection = getRouteDirection(rri);
						ca.action(rri, routeDirection, con);
					} catch (ExpectedLayoutException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException("Could not find location of : " + con);
					}
				}
			}
		});
	}
	
	public static ConnectionAction SET_CONTRADICTING = new ConnectionAction() {
		public void action(RouteRenderingInformation rri, Object d, Connection c) {
			if (c.getDrawDirection() != null) {
				if (d != NO_DISTANCE) {
					rri.setContradicting(d != c.getDrawDirection());
				}
			}
		}
	};
	
	public static final Object MULTIPLE_DIRECTIONS = new Object();
	public static final Object NO_DISTANCE = new Object();
	
	
	public static Object getRouteDirection(RouteRenderingInformation rri) {
		Object current = NO_DISTANCE;
	
		for (int i = 0; i < rri.size()-1; i++) {
			Dimension2D start = rri.getWaypoint(i);
			Dimension2D end = rri.getWaypoint(i+1);
			Direction partD = singleDirection(start, end);
			
			if ((current == NO_DISTANCE) && (partD != null)) {
				current = partD;
			} else if (partD == null) {
				// do nothing
			} else if ((current != null) && (partD != current)) {
				return MULTIPLE_DIRECTIONS;
			}
		}
		
		return current;
		
	}
	

	private static Direction singleDirection(Dimension2D start, Dimension2D end) {
		if ((start.y() == end.y()) && (start.x() > end.x())) {
			return Direction.LEFT;
		} else if ((start.y() == end.y()) && (start.x() < end.x())) {
			return Direction.RIGHT;
		} else if ((start.y() < end.y()) && (start.x() == end.x())) {
			return Direction.DOWN;
		} else if ((start.y() > end.y()) && (start.x() == end.x())) {
			return Direction.UP;
		} else if  ((start.y()  == end.y()) && (start.x() == end.x())) {
			return null;
		}

		throw new LogicException("Diagonal Route?");
	}

	public static class ExpectedLayoutException extends LogicException {

		private static final long serialVersionUID = 1L;

		public ExpectedLayoutException(String arg0) {
			super(arg0);
		}
		
	}
	
	
}
