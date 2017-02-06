package org.kite9.diagram.visualization.batik;

import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.svg.SVGOMTransform;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.style.impl.AbstractXMLDiagramElement;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.diagram.visualization.display.Displayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.CSSConstants;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTransformList;

public class BatikDisplayer extends AbstractCompleteDisplayer {

	private GraphicsNodeLookup lookup;
	
	public BatikDisplayer(boolean buffer, int gridSize, GraphicsNodeLookup lookup) {
		super(buffer, gridSize);
		this.lookup = lookup;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CostedDimension size(DiagramElement element, Dimension2D within) {
		if (element instanceof Text) {
			StyledKite9SVGElement xml = ((AbstractXMLDiagramElement)element).getTheElement();
			SVGRect bounds = xml.getBBox();
			return new CostedDimension(bounds.getWidth(), bounds.getHeight(), within);
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
		if (element instanceof Text) {
			StyledKite9SVGElement xml = ((AbstractXMLDiagramElement)element).getTheElement();
			//SVGRect bounds = xml.getBBox();
			
			SVGAnimatedTransformList transform = xml.getTransform();
			SVGTransformList t2 = transform.getBaseVal();
			
			// so, we're going to need to apply a transform here.
			RectangleRenderingInformation rri = ((Text)element).getRenderingInformation();
//			SVGOMTransform myTransform = new SVGOMTransform();
//			myTransform.setTranslate((float) rri.getPosition().x(), (float) rri.getPosition().y());
//			t2.appendItem(myTransform);
			
			GraphicsNode node = lookup.getNode(GraphicsLayerName.MAIN, xml);
			node.getTransform().translate((float) rri.getPosition().x(), (float) rri.getPosition().y());
			//node.setTransform(myTransform);
			
		}	
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
