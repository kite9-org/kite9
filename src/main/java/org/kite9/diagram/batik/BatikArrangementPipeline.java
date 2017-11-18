package org.kite9.diagram.batik;

import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.visitors.DiagramChecker;
import org.kite9.diagram.model.visitors.DiagramElementVisitor;
import org.kite9.diagram.model.visitors.VisitorAction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;
import org.kite9.framework.xml.DiagramKite9XMLElement;

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
	public DiagramKite9XMLElement arrange(DiagramKite9XMLElement d) {
		DiagramKite9XMLElement out = super.arrange(d);
//		drawDiagramElements(d.getDiagramElement());
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
