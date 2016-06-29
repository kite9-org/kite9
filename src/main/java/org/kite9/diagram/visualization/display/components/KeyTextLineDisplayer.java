package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.ContainerProperty;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.TextContainingDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.display.style.TextBoxStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class KeyTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	public KeyTextLineDisplayer(CompleteDisplayer parent, GraphicsLayer g2,
			Stylesheet ss, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	public KeyTextLineDisplayer(GraphicsLayer g2, Stylesheet ss) {
		super(g2, ss);
	}

	@Override
	public TextContainingDiagramElement getLabel(DiagramElement de) {
		return ((TextLine) de);
	}

	@Override
	public ContainerProperty<Symbol> getSymbols(DiagramElement de) {
		return ((TextLine) de).getSymbols();

	}

	@Override
	public TextContainingDiagramElement getStereotype(DiagramElement de) {
		return null;
	}

	@Override
	public TextBoxStyle getUnderlyingStyle(DiagramElement de) {
		return ss.getKeySymbolStyle();
	}

	@Override
	protected boolean applyBoxContentCentering() {
		return false;
	}
	
	@Override
	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine) 
				&& ((TextLine)element).getParent() instanceof Key);
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return null;
	}

}
