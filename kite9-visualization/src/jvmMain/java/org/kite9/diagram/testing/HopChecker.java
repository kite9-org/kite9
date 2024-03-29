package org.kite9.diagram.testing;

import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.testing.DiagramChecker.ExpectedLayoutException;

public class HopChecker {
	
	public static interface HopAction {
		
		public void action(RouteRenderingInformation rri, int hopCount, Connection c);
		
	}

	public static void checkHops(Diagram d, final HopAction ca) {
		DiagramElementVisitor dev = new DiagramElementVisitor();
		dev.visit(d, new VisitorAction() {

			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					Connection con = (Connection) de;
					if (con.getDrawDirection() != null) {
						try {
							RouteRenderingInformation rri = (RouteRenderingInformation) con.getRenderingInformation();
							int hopCount = 0;
							for (Boolean b : rri.getHops()) {
								hopCount = hopCount + (b ? 1 :0);
							}
							ca.action(rri, hopCount, con);
						} catch (ExpectedLayoutException e) {
							throw e;
						} catch (Exception e) {
							throw new RuntimeException("Could not find location of : " + con);
						}
					}
				}
			}
		});
	}
	
}
