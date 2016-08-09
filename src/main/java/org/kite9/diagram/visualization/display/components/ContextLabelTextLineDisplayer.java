package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.StyledDiagramElement;
import org.kite9.diagram.adl.TextContainingDiagramElement;
import org.kite9.diagram.position.BasicRenderingInformation;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.io.StaticStyle;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.xml.ContainerProperty;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Symbol;
import org.kite9.diagram.xml.TextLine;

public class ContextLabelTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	public ContextLabelTextLineDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public ContextLabelTextLineDisplayer(GraphicsLayer g2) {
		super(g2);
	}

	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine)
				&& ((TextLine)element).getParentNode() instanceof Context);
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		RectangleRenderingInformation ri = (RectangleRenderingInformation) r;

		VPos vj = ri.getVerticalJustification();

		// get label actual size
		CostedDimension cd = size(element, CostedDimension.UNBOUNDED);

		// set initial bounds on label
		double xStart = ri.getPosition().x();
		double yStart = ri.getPosition().y();
		double xEnd = ri.getPosition().x()+ri.getSize().x();
		double yEnd = ri.getPosition().y()+ri.getSize().y();
	
		double slacky = ri.getSize().y() - cd.y();
		double slackx = ri.getSize().x() - cd.x();
		
		ContainerProperty<Symbol> symbols = getSymbols(element);
		boolean syms = (symbols != null) && (symbols.size() > 0);
		
		if (!syms) {
			xStart += slackx / 2;
			xEnd -= slackx / 2;
		}
		
		if (vj == VPos.DOWN) {
			yStart += slacky;
		}
		
		super.draw(element, new BasicRenderingInformation(
				new Dimension2D(xStart, yStart),
				new Dimension2D(xEnd - xStart, yEnd - yStart),
				ri.getHorizontalJustification(), ri.getVerticalJustification(), ri.isRendered()));
	}

	@Override
	public TextContainingDiagramElement getLabel(DiagramElement de) {
		return ((TextLine)de);
	}

	@Override
	public ContainerProperty<Symbol> getSymbols(DiagramElement de) {
		return ((TextLine)de).getSymbols();
	}

	@Override
	public TextContainingDiagramElement getStereotype(DiagramElement de) {
		return null;
	}

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle((StyledDiagramElement) de);
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return new RoundedRectFlexibleShape(5, 0, 0);
	}

}
