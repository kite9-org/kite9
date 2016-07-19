package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.ContainerProperty;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.TextContainingDiagramElement;
import org.kite9.diagram.style.StyledDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class GlyphTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	@Override
	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine) 
				&& ((TextLine)element).getOwner() instanceof Glyph);
	}
		
	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		super.draw(element, r);
	}




	public GlyphTextLineDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}


	public GlyphTextLineDisplayer(GraphicsLayer g2) {
		super(g2);
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
	public TextContainingDiagramElement getLabel(DiagramElement de) {
		return ((TextLine)de);
	}
	
	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle((StyledDiagramElement) de);
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
