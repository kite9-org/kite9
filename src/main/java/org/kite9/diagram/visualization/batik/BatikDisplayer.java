package org.kite9.diagram.visualization.batik;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.Decal;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Rectangular;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.style.DiagramElementSizing;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;

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
		if (element instanceof HasLayeredGraphics) {
			DiagramElementSizing sizing = getSizing((HasLayeredGraphics) element);

			if (sizing == null) {
				return CostedDimension.ZERO;
			}
			
			switch (sizing) {
			case FIXED:
				Rectangle2D bounds = ((HasLayeredGraphics) element).getSVGBounds();
				if (bounds == null) {
					return new CostedDimension(1, 1, 0);
				}
				return new CostedDimension(bounds.getWidth(), bounds.getHeight(), within);
			case MINIMIZE:
			case MAXIMIZE:
				Rectangular r = (Rectangular) element;
				double left = r.getPadding(Direction.LEFT);
				double right =  r.getPadding(Direction.RIGHT);
				double up =  r.getPadding(Direction.UP);
				double down =  r.getPadding(Direction.DOWN);
				return new CostedDimension(left+right, up+down, CostedDimension.UNBOUNDED);
			case ADAPTIVE:
			case SCALED:
			case UNSPECIFIED:
			default:
			}
		}
		return CostedDimension.ZERO;

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
			HasLayeredGraphics layered = (HasLayeredGraphics) element;

			if (parent != null) {
				layered.eachLayer(node -> {
					// make sure the graphics node is anchored to it's parent
					GraphicsLayerName name = ((IdentifiableGraphicsNode) node).getLayer();
					IdentifiableGraphicsNode parentNode = (IdentifiableGraphicsNode) ((HasLayeredGraphics) parent).getGraphicsForLayer(name);
					parentNode.add(node);
				});
			}

			
			Rectangle2D bounds = layered.getSVGBounds();
			DiagramElementSizing sizing = getSizing(layered);
			
			if (bounds != null) {				
				// reset the scale first
				layered.eachLayer(node -> {
					AffineTransform existing = node.getTransform();
					AffineTransform global = node.getGlobalTransform();
					System.out.println("Global transform of "+element+" : "+global);
					existing.scale(1d/ global.getScaleX(), 1d /global.getScaleY());
				});
				
				if (sizing == DiagramElementSizing.FIXED)  {
					// apply a translation to the Kite9-specified position
					
					layered.eachLayer(node -> {
						RectangleRenderingInformation rri = (RectangleRenderingInformation) ri;
						AffineTransform global = node.getGlobalTransform();
						AffineTransform existing = node.getTransform();
						translateRelative(bounds, existing, global, rri);
					});
				}
				
				if ((sizing == DiagramElementSizing.SCALED) || (sizing == DiagramElementSizing.ADAPTIVE)){
					// applies scale and translation
					layered.eachLayer(node -> {
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

	private DiagramElementSizing getSizing(HasLayeredGraphics layered) {
		DiagramElementSizing out =  (layered instanceof Rectangular) ?((Rectangular) layered).getSizing() : null;
		System.out.println("Sizing of "+layered+" is "+out);
		return out;
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
		if (element instanceof Rectangular) {
			return ((Rectangular) element).getPadding(d);
		} else if (element instanceof Connection) {
			return ((Connection) element).getPadding(d);
		} else {
			return 0;
		}
	}
	
	@Override
	public double getMargin(DiagramElement element, Direction d) {
		if (element instanceof Rectangular) {
			return ((Rectangular) element).getMargin(d);
		} else if (element instanceof Connection) {
			return ((Connection) element).getMargin(d);
		} else {
			return 0;
		}
	}

}
