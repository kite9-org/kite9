package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class ContextLabelDisplayer extends AbstractTextBoxModelDisplayer {

	public ContextLabelDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public ContextLabelDisplayer(GraphicsLayer g2) {
		super(g2);
	}

	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof Label)
				&& (!((Label)element).isConnectionLabel()));
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		RectangleRenderingInformation ri = (RectangleRenderingInformation) r;

		VPos vj = ri.getVerticalJustification();

		// get label actual size
		CostedDimension cd = size((Leaf) element, CostedDimension.UNBOUNDED);
		
		if (ri.getPosition() == null) {
			return;
		}

		// set initial bounds on label
		double xStart = ri.getPosition().x();
		double yStart = ri.getPosition().y();
		double xEnd = ri.getPosition().x()+ri.getSize().x();
		double yEnd = ri.getPosition().y()+ri.getSize().y();
	
		double slacky = ri.getSize().y() - cd.y();
		double slackx = ri.getSize().x() - cd.x();
		
//		ContainerProperty<Symbol> symbols = getSymbols(element);
//		boolean syms = (symbols != null) && (symbols.size() > 0);
//		
//		if (!syms) {
//			xStart += slackx / 2;
//			xEnd -= slackx / 2;
//		}
//		
//		if (vj == VPos.DOWN) {
//			yStart += slacky;
//		}
		
		super.draw(element, new RectangleRenderingInformationImpl(
				new Dimension2D(xStart, yStart),
				new Dimension2D(xEnd - xStart, yEnd - yStart),
				ri.getHorizontalJustification(), ri.getVerticalJustification(), ri.isRendered()));
	}

	@Override
	public String getLabel(DiagramElement de) {
		return ((Label)de).getText();
	}

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle(de);
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return new RoundedRectFlexibleShape(5, 0, 0);
	}

}
