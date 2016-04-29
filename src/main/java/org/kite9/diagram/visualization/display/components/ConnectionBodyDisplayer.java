package org.kite9.diagram.visualization.display.components;

import java.util.List;

import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.StyledText;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.display.style.TextBoxStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;


public class ConnectionBodyDisplayer extends AbstractTextBoxModelDisplayer {

	public ConnectionBodyDisplayer(CompleteDisplayer parent, Stylesheet ss, GraphicsLayer g2, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	public boolean canDisplay(DiagramElement element) {
		return element instanceof Arrow;
	}

	@Override
	public TextBoxStyle getUnderlyingStyle(DiagramElement de) {
		return ss.getConnectionBodyStyle();
	}

	@Override
	public StyledText getLabel(DiagramElement de) {
		return ((Arrow)de).getLabel();
	}

	@Override
	public List<Symbol> getSymbols(DiagramElement de) {
		return null;
	}

	@Override
	public StyledText getStereotype(DiagramElement de) {
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
		StyledText label = getLabel(de);
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
		return ss.getConnectionBodyDefaultShape();
	}
	
	
}
