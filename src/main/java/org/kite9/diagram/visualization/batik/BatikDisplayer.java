package org.kite9.diagram.visualization.batik;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.SVG12CSSConstants;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.Decal;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.adl.sizing.FixedSizeGraphics;
import org.kite9.diagram.adl.sizing.HasLayeredGraphics;
import org.kite9.diagram.adl.sizing.ScaledGraphics;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.diagram.visualization.display.Displayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
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
		if (element instanceof FixedSizeGraphics) {
			Rectangle2D bounds = ((FixedSizeGraphics) element).getSVGBounds();
			if (bounds == null) {
				return CostedDimension.ZERO;
			}
			return new CostedDimension(bounds.getWidth(), bounds.getHeight(), within);
		} else if ((element instanceof ScaledGraphics)) {
			// return zero, as these elements can be resized to whatever size is needed
			return CostedDimension.ZERO;
		} else if ((element instanceof Container)) {
			Value left = element.getCSSStyleProperty(CSSConstants.PADDING_LEFT_PROPERTY);
			Value right = element.getCSSStyleProperty(CSSConstants.PADDING_RIGHT_PROPERTY);
			Value up = element.getCSSStyleProperty(CSSConstants.PADDING_TOP_PROPERTY);
			Value down = element.getCSSStyleProperty(CSSConstants.PADDING_BOTTOM_PROPERTY);
			return new CostedDimension(left.getFloatValue()+right.getFloatValue(), up.getFloatValue()+down.getFloatValue(), CostedDimension.UNBOUNDED);
		} else {
			return CostedDimension.ZERO;
		}
		
		//throw new Kite9ProcessingException("Can't size: "+element);
	}

	/**
	 * Handle scaling before translation, otherwise everything goes wacky.
	 */
	@Override
	public void draw(DiagramElement element, RenderingInformation ri){
		
		if (element instanceof Decal) {
			// tells the decal how big it needs to draw itself
			Container parent = element.getContainer();
			RectangleRenderingInformation rri = parent.getRenderingInformation();
			
			((Decal) element).setParentSize(new double[] {0, rri.getSize().getWidth()}, new double[] {0, rri.getSize().getHeight() });
		}
		
		if (element instanceof HasLayeredGraphics) {
			
			DiagramElement parent = element.getParent();
			if (parent != null) {
				((HasLayeredGraphics) element).eachLayer(node -> {
					// make sure the graphics node is anchored to it's parent
					GraphicsLayerName name = ((IdentifiableGraphicsNode) node).getLayer();
					IdentifiableGraphicsNode parentNode = (IdentifiableGraphicsNode) ((HasLayeredGraphics) parent).getGraphicsForLayer(name);
					parentNode.add(node);
				});
			}

			
			Rectangle2D bounds = ((HasLayeredGraphics) element).getSVGBounds();
			
			if (bounds != null) {
				System.out.println("Internal bounds of "+element+" : "+bounds);
				
				// reset the scale first
				((HasLayeredGraphics) element).eachLayer(node -> {
					AffineTransform existing = node.getTransform();
					AffineTransform global = node.getGlobalTransform();
					System.out.println("Global transform of "+element+" : "+global);
					existing.scale(1d/ global.getScaleX(), 1d /global.getScaleY());
				});
				
				if (element instanceof FixedSizeGraphics) {
					// apply a translation to the Kite9-specified position
					
					((HasLayeredGraphics) element).eachLayer(node -> {
						RectangleRenderingInformation rri = (RectangleRenderingInformation) ri;
						AffineTransform global = node.getGlobalTransform();
						AffineTransform existing = node.getTransform();
						translateRelative(bounds, existing, global, rri);
					});
				}
				
				if (element instanceof ScaledGraphics) {
					// appplies scale and translation
					((HasLayeredGraphics) element).eachLayer(node -> {
						RectangleRenderingInformation rri = (RectangleRenderingInformation) ri;
						System.out.println("Expected Size of "+element+" : "+rri.getSize());
						System.out.println("Expected Position of "+element+" : "+rri.getPosition());
						AffineTransform existing = node.getTransform();
						
						if (bounds != null) {
							existing.scale(1d / bounds.getWidth(), 1d/bounds.getHeight());
							existing.scale(rri.getSize().getWidth(), rri.getSize().getHeight());
							AffineTransform global = node.getGlobalTransform();
							translateRelative(bounds, existing, global, rri);
						}
					});
				}
			}
		}
	}

	private void translateRelative(Rectangle2D bounds, AffineTransform existing, AffineTransform global, RectangleRenderingInformation rri) {
		existing.translate(-bounds.getX(), -bounds.getY());
		double xs = global.getScaleX();
		double ys = global.getScaleY();
		double xt = rri.getPosition().x() - global.getTranslateX();
		double yt = rri.getPosition().y() - global.getTranslateY();
		double xst = xt / xs;
		double yst = yt  / ys;
		System.out.println("translate: "+xst+" "+yst);
		existing.translate(xst, yst);
		System.out.println(existing);
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
	
	@Override
	public double getMargin(DiagramElement element, Direction d) {
		Value v;
		switch (d) {
		case UP:
			v = element.getCSSStyleProperty(SVG12CSSConstants.CSS_MARGIN_TOP_PROPERTY);
			break;
		case DOWN:
			v = element.getCSSStyleProperty(SVG12CSSConstants.CSS_MARGIN_BOTTOM_PROPERTY);
			break;
		case LEFT:
			v = element.getCSSStyleProperty(SVG12CSSConstants.CSS_MARGIN_LEFT_PROPERTY);
			break;
		case RIGHT:
			v = element.getCSSStyleProperty(SVG12CSSConstants.CSS_MARGIN_RIGHT_PROPERTY);
			break;
		default:
			throw new Kite9ProcessingException("No direction set");
		}
		
		System.out.println("margin for "+element+" in direction "+d+ " "+v.getFloatValue());
		return v.getFloatValue();
	}

}
