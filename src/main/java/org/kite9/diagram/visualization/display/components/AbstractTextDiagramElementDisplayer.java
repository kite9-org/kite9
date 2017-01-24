package org.kite9.diagram.visualization.display.components;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.TextStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer2D;

/**
 * Handles rendering and sizing of `Text` `DiagramElements`. 
 * @author robmoffat
 * 
 */
public abstract class AbstractTextDiagramElementDisplayer extends AbstractRectangularDiagramElementDisplayer {

	public AbstractTextDiagramElementDisplayer(GraphicsLayer2D g2) {
		super(g2);
	}

	public AbstractTextDiagramElementDisplayer(CompleteDisplayer parent, GraphicsLayer2D g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	@Override
	protected void drawBoxContents(DiagramElement element, RectangleRenderingInformation r) {
		Rectangle2D ri = getDrawingRectangle(element, r);
		String label = safeGetText(getLabel(element));
		double xStart = ri.getMinX();
		double yStart = ri.getMinY();
		TextStyle ls = getLabelStyle(element);
		if (ls != null) {
			double baseline2 =getBaseline(ls.getFont(), g2, label);
			arrangeString(ls.getFont(), ls.getColor(), label, new Dimension2D(xStart, yStart), new Dimension2D(ri.getWidth(), ri.getHeight()), true, ls.getJust(), baseline2);
		}
	}
	
	public abstract String getLabel(DiagramElement de);
	
	public boolean hasContent(DiagramElement de) {
		String label = safeGetText(getLabel(de));
		boolean lab = (label != null) && (label.length() > 0);
		return lab;
	}
	
	public TextStyle getLabelStyle(DiagramElement de) {
		return new TextStyle(de);
	}
	
	@Override
	protected Dimension2D sizeBoxContents(DiagramElement e, Dimension2D within) {		
//		TextStyle ls = getLabelStyle(e);
		String text = safeGetText(getLabel(e));
		return g2.getStringBounds(e, text);
			
//		CostedDimension nameSize = ls != null ? arrangeString(ls.getFont(), ls.getColor(), ), 
//					CostedDimension.ZERO, CostedDimension.UNBOUNDED, false, Justification.CENTER, 0) : CostedDimension.ZERO;
//			
//		return nameSize;
	}

	private String safeGetText(String st) {
		return st == null ? null : st;
	}
	
	
	
}
