package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.ContainerProperty;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.StyledDiagramElement;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class KeyTextLineDisplayer extends AbstractTextBoxModelDisplayer {

	public KeyTextLineDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public KeyTextLineDisplayer(GraphicsLayer g2) {
		super(g2);
	}

	@Override
	public Text getLabel(DiagramElement de) {
		return ((TextLine) de);
	}

	@Override
	public ContainerProperty<Symbol> getSymbols(DiagramElement de) {
		return ((TextLine) de).getSymbols();

	}

	@Override
	public Text getStereotype(DiagramElement de) {
		return null;
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
	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine) 
				&& ((TextLine)element).getParent() instanceof Key);
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return null;
	}

}
