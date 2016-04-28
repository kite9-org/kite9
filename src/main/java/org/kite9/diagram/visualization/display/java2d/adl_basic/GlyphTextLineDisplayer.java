package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.Graphics2D;
import java.util.List;

import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.StyledText;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.TextBoxStyle;

public class GlyphTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	@Override
	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine) 
				&& ((TextLine)element).getParent() instanceof Glyph);
	}
		
	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		super.draw(element, r);
	}




	public GlyphTextLineDisplayer(CompleteDisplayer parent, Graphics2D g2,
			Stylesheet ss, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}


	public GlyphTextLineDisplayer(Graphics2D g2, Stylesheet ss) {
		super(g2, ss);
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
	public StyledText getLabel(DiagramElement de) {
		return ((TextLine)de).getText();
	}
	
	@Override
	public TextBoxStyle getUnderlyingStyle(DiagramElement de) {
		return ss.getGlyphTextLineStyle();
	}

	@Override
	protected boolean applyBoxContentCentering() {
		return false;
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return null;
	}

}
