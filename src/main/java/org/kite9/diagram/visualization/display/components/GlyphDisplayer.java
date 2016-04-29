package org.kite9.diagram.visualization.display.components;

import java.util.Collections;
import java.util.List;

import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.StyledText;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.display.style.TextBoxStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;


public class GlyphDisplayer extends AbstractTextWithContentBoxModelDisplayer {
	
	AbstractBoxModelDisplayer lineDisplayer; 
	

	public GlyphDisplayer(CompleteDisplayer parent, Stylesheet ss, GraphicsLayer g2, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	
	
	public boolean canDisplay(DiagramElement element) {
		return element instanceof Glyph;
	}

	@Override
	public StyledText getLabel(DiagramElement de) {
		return ((Glyph)de).getLabel();
	}

	@Override
	public List<Symbol> getSymbols(DiagramElement de) {
		return ((Glyph)de).getSymbols();
	}

	@Override
	public StyledText getStereotype(DiagramElement de) {
		return ((Glyph)de).getStereotype();
	}

	@Override
	public TextBoxStyle getUnderlyingStyle(DiagramElement de) {
		return ss.getGlyphBoxStyle();
	}

	/**
	 * Adds a divider if necessary
	 */
	@Override
	public List<DiagramElement> getContents(DiagramElement de) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<DiagramElement> out =  (List<DiagramElement>) (List) ((Glyph)de).getText();

		if ((out == null) || (out.size() == 0)) {
			return Collections.emptyList();
		} else {
			return out;
		}
	}

	@Override
	protected boolean applyBoxContentCentering() {
		return true;
	}



	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return ss.getGlyphDefaultShape();
	}
}
