package org.kite9.diagram.visualization.batik;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.FixedSizeGraphics;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.style.impl.AbstractXMLDiagramElement;
import org.kite9.diagram.visualization.batik.node.GraphicsNodeLookup;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.diagram.visualization.display.Displayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.CSSConstants;
import org.w3c.dom.svg.SVGRect;

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
		if (element instanceof FixedSizeGraphics) {
			StyledKite9SVGElement xml = ((AbstractXMLDiagramElement)element).getTheElement();
			SVGRect bounds = xml.getBBox();
			if (bounds == null) {
				return CostedDimension.ZERO;
			}
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

	/**
	 * Handle scaling before translation, otherwise everything goes wacky.
	 */
	@Override
	public void draw(DiagramElement element, RenderingInformation ri){
		StyledKite9SVGElement xml = ((AbstractXMLDiagramElement)element).getTheElement();
		GraphicsNode node = lookup.getNode(GraphicsLayerName.MAIN, xml);
		Rectangle2D bounds = ((IdentifiableGraphicsNode) node).getSVGBounds();
		System.out.println("Internal bounds of "+element+" : "+bounds);
		AffineTransform existing = node.getTransform();
		AffineTransform global = node.getGlobalTransform();
		System.out.println("Global transform of "+element+" : "+global);
		existing.scale(1d/ global.getScaleX(), 1d /global.getScaleY());

		if (element instanceof FixedSizeGraphics) {
			RectangleRenderingInformation rri = ((FixedSizeGraphics)element).getRenderingInformation();
			translateRelative(bounds, existing, global, rri);
		} else if (element instanceof Container) {
			RectangleRenderingInformation rri = ((Container)element).getRenderingInformation();
			System.out.println("Expected Size of "+element+" : "+rri.getSize());
			System.out.println("Expected Position of "+element+" : "+rri.getPosition());
			
			if (bounds != null) {
				existing.scale(1d / bounds.getWidth(), 1d/bounds.getHeight());
				existing.scale(rri.getSize().getWidth(), rri.getSize().getHeight());
				translateRelative(bounds, existing, global, rri);
			}
		}

	}

	private void translateRelative(Rectangle2D bounds, AffineTransform existing, AffineTransform global, RectangleRenderingInformation rri) {
		existing.translate(-bounds.getX(), -bounds.getY());
		existing.translate(
				(rri.getPosition().x() - global.getTranslateX()) / (existing.getScaleX() * global.getScaleX()),
				(rri.getPosition().y() - global.getTranslateY())  / (existing.getScaleY() * global.getScaleY()));
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
