package org.kite9.diagram.visualization.batik;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;
import org.kite9.diagram.visualization.pipeline.full.AbstractArrangementPipeline;
import org.kite9.diagram.xml.DiagramXMLElement;

public class BatikArrangementPipeline extends AbstractArrangementPipeline {
	
	private final BatikDisplayer displayer;

	public BatikArrangementPipeline(BatikDisplayer displayer) {
		super();
		this.displayer = displayer;
	}

	@Override
	public CompleteDisplayer getDisplayer() {
		return displayer;
	}

	@Override
	public DiagramXMLElement arrange(DiagramXMLElement d) {
		DiagramXMLElement out = super.arrange(d);
		drawDiagramElements(d.getDiagramElement());
		return out;
	}
	
	protected void drawDiagramElements(Container area) {
		DiagramElementVisitor dev = new DiagramElementVisitor();
		VisitorAction va =new VisitorAction() {

			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					RouteRenderingInformation rri = (RouteRenderingInformation) ((Connection) de).getRenderingInformation();
					Object d = DiagramChecker.getRouteDirection(rri);
					DiagramChecker.SET_CONTRADICTING.action(rri, d, (Connection)de);
					displayer.draw(de, rri);
				} else {
					displayer.draw(de, de.getRenderingInformation());
				}
			}
		};

		dev.visit(area, va);
	}

}
