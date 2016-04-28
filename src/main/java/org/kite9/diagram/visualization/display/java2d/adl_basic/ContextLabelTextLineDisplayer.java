package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.Graphics2D;
import java.util.List;

import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.StyledText;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.TextBoxStyle;

public class ContextLabelTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	public ContextLabelTextLineDisplayer(CompleteDisplayer parent, Graphics2D g2, Stylesheet ss, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	public ContextLabelTextLineDisplayer(Graphics2D g2, Stylesheet ss) {
		super(g2, ss);
	}

	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine) && ((TextLine)element).getParent() instanceof Context);
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
		
		List<Symbol> symbols = getSymbols(element);
		boolean syms = (symbols != null) && (symbols.size() > 0);
		
		if (!syms) {
			xStart += slackx / 2;
			xEnd -= slackx / 2;
		}
		
		if (vj == VPos.DOWN) {
			yStart += slacky;
		}
		
		super.draw(element, new RectangleRenderingInformation(
				new Dimension2D(xStart, yStart),
				new Dimension2D(xEnd - xStart, yEnd - yStart),
				ri.getHorizontalJustification(), ri.getVerticalJustification(), ri.isRendered()));
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
	public TextBoxStyle getUnderlyingStyle(DiagramElement de) {
		return ss.getContextLabelStyle();
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return ss.getContextLabelDefaultShape();
	}

}
