package org.kite9.diagram.visualization.format;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.PositionableDiagramElement;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.style.DiagramElement;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.RequiresGraphicsSourceRendererCompleteDisplayer;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;

/**
 * Takes the formatted ADL diagram and displays it on the screen.
 * 
 * Each item that needs to be rendered must have a {@link RenderingInformation} object attached.
 * 
 * @author robmoffat
 */
public abstract class AbstractGraphicsSourceRenderer<X> implements GraphicsSourceRenderer<X> {

	protected RequiresGraphicsSourceRendererCompleteDisplayer dea;
	
	public AbstractGraphicsSourceRenderer() {
		super();
	}

	public void setDisplayer(RequiresGraphicsSourceRendererCompleteDisplayer dea) {
		this.dea = dea;
		dea.initialize(this, new Dimension2D(1,1));
	}
	
	public CompleteDisplayer getDisplayer() {
		return dea;
	}
	
	
	public static CostedDimension size(Container element) {
		RectangleRenderingInformation r = (RectangleRenderingInformation) element.getRenderingInformation();
		
		return new CostedDimension(r.getSize().getWidth(), r.getSize().getHeight(), 0);
	}

	/**
	 * Draws all diagram attr from the diagram
	 * @param area 
	 */
	protected void drawDiagramElements(Container area) {
		DiagramElementVisitor dev = new DiagramElementVisitor();
		VisitorAction va =new VisitorAction() {

			public void visit(DiagramElement de) {
				if (de instanceof Connection) {
					RouteRenderingInformation rri = (RouteRenderingInformation) ((Connection) de).getRenderingInformation();
					Object d = DiagramChecker.getRouteDirection(rri);
					DiagramChecker.SET_CONTRADICTING.action(rri, d, (Connection)de);
				}
				if (de instanceof PositionableDiagramElement) {
					dea.draw(de, ((PositionableDiagramElement)de).getRenderingInformation());
				}
			}
		};

		dev.visit(area, va);
	}

	/**
	 * Common across all graphics sources - set these when you have a Graphics2D.
	 */
	protected void setRenderingHints(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}

	@Override
	public Dimension2D getImageSize(Dimension2D diagramSize) {
		return diagramSize;
	}

	
}
