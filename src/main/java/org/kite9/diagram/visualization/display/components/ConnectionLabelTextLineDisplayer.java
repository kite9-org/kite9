package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.DirectionalValues;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;

/**
 * Labels for the Connections in the diagram 
 * 
 * @author robmoffat
 * 
 */
public class ConnectionLabelTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	public ConnectionLabelTextLineDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public ConnectionLabelTextLineDisplayer(GraphicsLayer g2) {
		super(g2);
	}

	public boolean canDisplay(DiagramElement element) {
		return (element instanceof Label) && ((Label) element).isConnectionLabel() && ((Label)element).hasContent();
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		RectangleRenderingInformation ri = (RectangleRenderingInformation) r;

		HPos hj = ri.getHorizontalJustification();
		VPos vj = ri.getVerticalJustification();

		// get label actual size
		CostedDimension cd = size((Leaf) element, CostedDimension.UNBOUNDED);

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
		
		super.draw(element, new RectangleRenderingInformationImpl(
				new Dimension2D(xStart, yStart),
				new Dimension2D(xEnd - xStart, yEnd - yStart),
				ri.getHorizontalJustification(), ri.getVerticalJustification(), ri.isRendered()));
	}
	
	/**
	 * This is a little tricky.  IF we are dealing with right aligned boxes, we need to swap the margins and
	 * round, same for down instead of up
	 */
	@Override
	public BoxStyle getBoxStyle(DiagramElement de) {
		RectangleRenderingInformation rri = (RectangleRenderingInformation) ((Label)de).getRenderingInformation();
		HPos hj = rri.getHorizontalJustification();
		VPos vj = rri.getVerticalJustification();
	
		BoxStyle original = super.getBoxStyle(de);
		DirectionalValues margins = original.getMargin();

		boolean revx = hj ==HPos.RIGHT;
		boolean revy = vj == VPos.DOWN;
		
		margins = new DirectionalValues(
				revy ? margins.getBottom() : margins.getTop(), 
				revx ? margins.getLeft() : margins.getRight(), 
				revy ? margins.getTop() : margins.getBottom(), 
				revx ? margins.getRight() : margins.getLeft());
		
		
		return new BoxStyle(original, margins);
	}
	
	

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle(de);
	}

	@Override
	public String getLabel(DiagramElement de) {
		return ((Label)de).getText();
	}


	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return new RoundedRectFlexibleShape(5, 0, 0);
	}
}
