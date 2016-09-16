package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;


public class TextDiagramElementDisplayer extends AbstractTextBoxModelDisplayer {

	public TextDiagramElementDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public boolean canDisplay(DiagramElement element) {
		return element instanceof Text;
	}

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle(de);
	}

	@Override
	public String getLabel(DiagramElement de) {
		return ((Text)de).getText();
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
//		String label = getLabel(de);
//		if ((label==null) || (label.trim().length()==0)) {
//			return false;
//		} else {
//			return true;
//		}
		
		return true;
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