package org.kite9.diagram.visualization.batik;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.diagram.visualization.display.Displayer;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.CSSConstants;

public class BatikDisplayer extends AbstractCompleteDisplayer {

	public BatikDisplayer(boolean buffer, int gridSize) {
		super(buffer, gridSize);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CostedDimension size(DiagramElement element, Dimension2D within) {
		if (element instanceof Text) {
			 return CostedDimension.ZERO;
		} else if (element instanceof Container) {
			Value left = element.getCSSStyleProperty(CSSConstants.PADDING_LEFT_PROPERTY);
			Value right = element.getCSSStyleProperty(CSSConstants.PADDING_RIGHT_PROPERTY);
			Value up = element.getCSSStyleProperty(CSSConstants.PADDING_TOP_PROPERTY);
			Value down = element.getCSSStyleProperty(CSSConstants.PADDING_BOTTOM_PROPERTY);
			return new CostedDimension(left.getFloatValue()+right.getFloatValue(), up.getFloatValue()+down.getFloatValue(), CostedDimension.UNBOUNDED);
		}
		
		throw new Kite9ProcessingException("Can't size: "+element);
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation ri) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOutputting() {
		return true;
	}

	@Override
	public void setOutputting(boolean outputting) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canDisplay(DiagramElement element) {
		return true;
	}

	@Override
	public Displayer getDisplayer(DiagramElement de) {
		return this;
	}

	@Override
	public boolean requiresDimension(DiagramElement de) {
		if (de instanceof Text) {
			String label = getLabel(de);
			if ((label==null) || (label.trim().length()==0)) {
				return false;
			} 
		} 
		
		return true;
	}
	
	public String getLabel(DiagramElement de) {
		return ((Text)de).getText();
	}

	@Override
	public double getLinkMargin(DiagramElement a, Direction d) {
		return 5;
	}

	@Override
	public double getPadding(DiagramElement element, Direction d) {
		Value v;
		switch (d) {
		case UP:
			v = element.getCSSStyleProperty(CSSConstants.PADDING_TOP_PROPERTY);
			break;
		case DOWN:
			v = element.getCSSStyleProperty(CSSConstants.PADDING_BOTTOM_PROPERTY);
			break;
		case LEFT:
			v = element.getCSSStyleProperty(CSSConstants.PADDING_LEFT_PROPERTY);
			break;
		case RIGHT:
			v = element.getCSSStyleProperty(CSSConstants.PADDING_RIGHT_PROPERTY);
			break;
		default:
			throw new Kite9ProcessingException("No direction set");
		}
		
		return v.getFloatValue();
	}

}
