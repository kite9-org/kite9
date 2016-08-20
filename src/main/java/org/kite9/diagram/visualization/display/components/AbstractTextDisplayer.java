package org.kite9.diagram.visualization.display.components;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.TextStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public abstract class AbstractTextDisplayer extends AbstractBoxModelDisplayer {

	public AbstractTextDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public abstract String getText(DiagramElement de);

	@Override
	protected void drawBoxContents(DiagramElement element,
			RectangleRenderingInformation r3) {
		Rectangle2D ri = getDrawingRectangle(element, r3);
//		g2.setColor(Color.GREEN);
//		g2.draw(ri);
		TextStyle ls = getTextStyle(element);
		String label = getText(element);
		double xStart = ri.getMinX();
		double yStart = ri.getMinY();
		
		// draw label, and syms if they haven't already been done
		double baseline2 = getBaselineToUse(element);
//		g2.setColor(Color.RED);
//		g2.drawRect((int) xStart, (int) (yStart+baseline), 100, 1);
		arrangeString(ls.getFont(), ls.getColor(), label, new Dimension2D(xStart, yStart), new Dimension2D(ri.getWidth(), ri.getHeight()), true, getJustification(element), baseline2);
	}
	
	protected Justification getJustification(DiagramElement de) {
		return getTextStyle(de).getJust();
	}

	protected abstract TextStyle getTextStyle(DiagramElement element);
	
	protected double getBaselineToUse(DiagramElement de) {
		TextStyle ts = getTextStyle(de);
		return getBaseline(ts.getFont(), g2, getText(de));
	}
	

	@Override
	protected Dimension2D sizeBoxContents(DiagramElement element,
			Dimension2D within) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
