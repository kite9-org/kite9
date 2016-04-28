package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.Graphics2D;
import java.util.List;

import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.primitives.BiDirectional;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.Label;
import org.kite9.diagram.primitives.StyledText;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.BoxStyle;
import org.kite9.diagram.visualization.display.java2d.style.DirectionalValues;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.TextBoxStyle;

/**
 * Labels for the Connections in the diagram 
 * 
 * @author robmoffat
 * 
 */
public class ConnectionLabelTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	public ConnectionLabelTextLineDisplayer(CompleteDisplayer parent, Graphics2D g2, Stylesheet ss, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	public ConnectionLabelTextLineDisplayer(Graphics2D g2, Stylesheet ss) {
		super(g2, ss);
	}

	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine) && ((TextLine) element).getParent() instanceof BiDirectional<?>);
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		RectangleRenderingInformation ri = (RectangleRenderingInformation) r;

		HPos hj = ri.getHorizontalJustification();
		VPos vj = ri.getVerticalJustification();

		// get label actual size
		CostedDimension cd = size(element, CostedDimension.UNBOUNDED);

		// set initial bounds on label
		double xStart = ri.getPosition().x();
		double yStart = ri.getPosition().y();
		double xEnd = ri.getPosition().x()+ri.getSize().x();
		double yEnd = ri.getPosition().y()+ri.getSize().y();
		
		// align label
		double slackx = ri.getSize().x() - cd.x();
		double slacky = ri.getSize().y() - cd.y();
		
		if (hj == HPos.RIGHT) {
			xStart += slackx;
		} else if (hj == null) {
			xStart += slackx / 2;
			xEnd -= slackx / 2;
		} else {
			xEnd -= slackx;
		}

		if (vj == VPos.DOWN) {
			yStart += slacky;
		} else {
			yEnd -= slacky;
		}
		
		super.draw(element, new RectangleRenderingInformation(
				new Dimension2D(xStart, yStart),
				new Dimension2D(xEnd - xStart, yEnd - yStart),
				ri.getHorizontalJustification(), ri.getVerticalJustification(), ri.isRendered()));
	}
	
	/**
	 * This is a little tricky.  IF we are dealing with right aligned boxes, we need to swap the margins and
	 * round, same for down instead of up
	 */
	@Override
	public TextBoxStyle getBoxStyle(DiagramElement de) {
		RectangleRenderingInformation rri = (RectangleRenderingInformation) ((Label)de).getRenderingInformation();
		HPos hj = rri.getHorizontalJustification();
		VPos vj = rri.getVerticalJustification();
	
		TextBoxStyle original = super.getBoxStyle(de);
		DirectionalValues margins = original.getMargin();

		boolean revx = hj ==HPos.RIGHT;
		boolean revy = vj == VPos.DOWN;
		
		margins = new DirectionalValues(
				revy ? margins.getBottom() : margins.getTop(), 
				revx ? margins.getLeft() : margins.getRight(), 
				revy ? margins.getTop() : margins.getBottom(), 
				revx ? margins.getRight() : margins.getLeft());
		
		
		original.setMargin(margins);
		return original;
	}
	
	

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return ss.getConnectionLabelStyle();
	}

	@Override
	public StyledText getLabel(DiagramElement de) {
		return ((TextLine)de).getText();
	}

	@Override
	public List<Symbol> getSymbols(DiagramElement de) {
		return ((TextLine)de).getSymbols();
	}

	@Override
	public StyledText getStereotype(DiagramElement de) {
		return null;
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return ss.getConnectionLabelDefaultShape();
	}
}
