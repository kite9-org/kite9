package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import org.kite9.diagram.common.elements.DebugLine;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;

public class DebugLineDisplayer extends AbstractRouteDisplayer {

	boolean active = false;
	
	public DebugLineDisplayer(CompleteDisplayer parent, Stylesheet ss, Graphics2D g2) {
		super(ss, g2);
		this.parent = parent;
	}
 
	@Override
	public boolean canDisplay(DiagramElement element) {
		return (element instanceof DebugLine) && active;
	}
	
	public LineDisplayer STRAIGHT_DISPLAYER = new LineDisplayer() {

		@Override
		public void drawMove(Move m1, Move next, Move prev, GeneralPath gp) {
			gp.lineTo(m1.xe + xo, m1.ye + yo);
		}

		@Override
		public float cornerRadius() {
			return 0;
		}
	};

	@Override
	public void draw(DiagramElement element, RenderingInformation ri) {
		if (isOutputting()) {
			RouteRenderingInformation ri2 = (RouteRenderingInformation) ri;
			drawRouting(ri2, ss.getDebugLinkStroke(), Color.BLUE, null, NULL_END_DISPLAYER, NULL_END_DISPLAYER, STRAIGHT_DISPLAYER, false, true, 0);
			arrangeString(ss.getDebugTextFont(), Color.RED, ((DebugLine)element).getL1(), ri2.getWaypoint(0), new Dimension2D(10,10), new Dimension2D(0, 0), new Dimension2D(0,0), true, Justification.LEFT, 10);
			arrangeString(ss.getDebugTextFont(), Color.GREEN, ((DebugLine)element).getL2(), ri2.getWaypoint(1), new Dimension2D(10,10), new Dimension2D(0, 0), new Dimension2D(0,0), true, Justification.LEFT, 10);
		}
	}

	@Override
	public double getLinkMargin(DiagramElement de, Direction d) {
		return 0;
	}

	@Override
	public double getPadding(DiagramElement element, Direction d) {
		return 0;
	}
}
