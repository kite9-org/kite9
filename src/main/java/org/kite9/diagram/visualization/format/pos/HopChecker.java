package org.kite9.diagram.visualization.format.pos;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.format.pos.DiagramChecker.ExpectedLayoutException;
import org.kite9.diagram.xml.DiagramXMLElement;

public class HopChecker {
	
	public static interface HopAction {
		
		public void action(RouteRenderingInformation rri, int hopCount, Connection c);
		
	}

	public static void checkHops(DiagramXMLElement d, final HopAction ca) {
		DiagramElementVisitor dev = new DiagramElementVisitor();
		dev.visit(d.getDiagramElement(), new VisitorAction() {

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
