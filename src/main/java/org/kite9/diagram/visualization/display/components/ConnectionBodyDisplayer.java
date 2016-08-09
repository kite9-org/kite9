package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.StyledDiagramElement;
import org.kite9.diagram.adl.TextContainingDiagramElement;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.xml.Arrow;
import org.kite9.diagram.xml.ContainerProperty;
import org.kite9.diagram.xml.Symbol;


public class ConnectionBodyDisplayer extends AbstractTextBoxModelDisplayer {

	public ConnectionBodyDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public boolean canDisplay(DiagramElement element) {
		return element instanceof Arrow;
	}

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle((StyledDiagramElement) de);
	}

	@Override
	public TextContainingDiagramElement getLabel(DiagramElement de) {
		return ((Arrow)de).getLabel();
	}

	@Override
	public ContainerProperty<Symbol> getSymbols(DiagramElement de) {
		return null;
	}

	@Override
	public TextContainingDiagramElement getStereotype(DiagramElement de) {
		return null;
	}
	
	

	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		if (requiresDimension(element)) {
			super.draw(element, r);
		}
	}

	@Override
	public CostedDimension size(DiagramElement element, Dimension2D within) {
		return super.size(element, within);
	}
	
	@Override
	public boolean requiresDimension(DiagramElement de) {
		TextContainingDiagramElement label = getLabel(de);
		if ((label==null) || (label.getText() == null) || (label.getText().trim().length()==0)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected boolean applyBoxContentCentering() {
		return true;
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return AbstractBoxModelDisplayer.DEFAULT_SHAPE;
	}
	
	
}
