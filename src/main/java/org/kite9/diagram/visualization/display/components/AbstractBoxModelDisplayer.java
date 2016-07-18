package org.kite9.diagram.visualization.display.components;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import org.kite9.diagram.common.hints.PositioningHints;
import org.kite9.diagram.position.BasicRenderingInformation;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.HintMap;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.style.ShapedDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.TransformedPaint;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.DirectionalValues;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.ShapeStyle;
import org.kite9.diagram.visualization.display.style.io.ShapeHelper;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;

/**
 * Handles display of any element which conforms to the box-model.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractBoxModelDisplayer extends AbstractADLDisplayer {
	
	public static final FlexibleShape DEFAULT_SHAPE = new RoundedRectFlexibleShape(8);

	public AbstractBoxModelDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public AbstractBoxModelDisplayer(GraphicsLayer g2) {
		super(g2);
	}

	public BoxStyle getBoxStyle(DiagramElement de) {
		BoxStyle bs = getUnderlyingStyle(de);		
		return bs;
	}
	
	public abstract BoxStyle getUnderlyingStyle(DiagramElement de);
	
	public FlexibleShape getBorderShape(DiagramElement de) {
		FlexibleShape out = null;
		if (de instanceof ShapedDiagramElement) {
			String name = ((ShapedDiagramElement) de).getShapeName();
			out = ShapeHelper.getFlexibleShapes().get(name);
			if ((out == null) && (name != null)) {
				out = ShapeHelper.getFlexibleShapes().get(name.toUpperCase());
			}
		}
		
		if (out == null) {
			return getDefaultBorderShape(de);
		}
		
		return out;
	}
	
	protected abstract FlexibleShape getDefaultBorderShape(DiagramElement de);

	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		BoxStyle bs = getBoxStyle(element);
		ShapeStyle sh = bs;
		RectangleRenderingInformation r2 = (RectangleRenderingInformation) r;
		Rectangle2D ri = getDrawingRectangle(element, r2);	
		ri = stripMargins(ri, bs);
		FlexibleShape fs = getBorderShape(element);
		
		double xStart = ri.getMinX();
		double yStart = ri.getMinY();
		double xEnd = ri.getMaxX();
		double yEnd = ri.getMaxY();
		
		if (element instanceof PositionableDiagramElement) {
			// set hint positions
			HintMap hints = ((PositionableDiagramElement)element).getPositioningHints();
			if (hints == null) {
				hints = new HintMap();
				((PositionableDiagramElement)element).setPositioningHints(hints);
			}
			hints.put(PositioningHints.MIN_X, (float) xStart);
			hints.put(PositioningHints.MAX_X, (float) xEnd);
			hints.put(PositioningHints.MIN_Y, (float) yStart);
			hints.put(PositioningHints.MAX_Y, (float) yEnd);
		}
		
		if (!isOutputting())
			return;

		if (fs != null) {
			// draw the border
			if (shadow && (sh != null) && (sh.castsShadow()) && (!sh.isInvisible())) {
				int xo = sh.getShadowXOffset();
				int yo = sh.getShadowYOffset();
				Paint background = bs.getShadowPaint();
				if (background != null) {
					g2.setPaint(background);
					Shape path = getShape(bs, fs, ( xStart + xo), (yStart +yo), (xEnd + xo), (yEnd + yo));
					g2.fill(path);
				}
			}
		}
		
		if (shadow)
			return;
		
		if ((fs != null) && (sh != null)) {
			paintBackground(bs, fs, g2, xStart, yStart, xEnd, yEnd);
			Stroke stroke = sh.getStroke();
			Shape s = getShape(bs, fs, xStart, yStart, xEnd, yEnd);
			if ((stroke != null) && (!sh.isInvisible())) {
				Paint border = sh.getStrokeColour();
				g2.setStroke(stroke);
				g2.setPaint(border);
				g2.draw(s);
			}
			r2.setPosition(new Dimension2D(xStart, yStart));
			r2.setSize(new Dimension2D(xEnd-xStart, yEnd - yStart));
		}

		
//		g2.setColor(new Color(1f, 0f, 0f, .3f));
//		g2.drawRect((int) xStart, (int) yStart, (int) (xEnd-xStart), (int) (yEnd - yStart));
//		
		// if there is any slack remaining, even it out
		double rWidth = xEnd - xStart;
		double rHeight = yEnd - yStart;
		Dimension2D used = sizeBoxContents(element, new Dimension2D(rWidth, rHeight));
		
		if (used != null) {
			DirectionalValues padding = getBorderSizes(element, used, bs, fs);
			xStart += padding.getLeft();
			yStart += padding.getTop();
			xEnd -= padding.getRight();
			yEnd -= padding.getBottom();
			
			if (applyBoxContentCentering()) {
				rWidth = xEnd - xStart - used.getWidth();
				rHeight = yEnd - yStart - used.getHeight();
				xStart += rWidth /2;
				xEnd -= rWidth /2;
				yStart += rHeight /2;
				yEnd -= rHeight /2;
			}
			
			
//			g2.setColor(new Color(1f, 0f, 0f, .3f));
//			g2.drawRect((int) xStart, (int) yStart, (int) (xEnd-xStart), (int) (yEnd - yStart));
//			
			RectangleRenderingInformation r3 = new BasicRenderingInformation(
					new Dimension2D(xStart, yStart),
					new Dimension2D(xEnd - xStart, yEnd - yStart), r2.getHorizontalJustification(), r2.getVerticalJustification(), r2.isRendered());
			
			
			
			drawBoxContents(element, r3);
		}
		
		if (element instanceof PositionableDiagramElement) {
			((PositionableDiagramElement)element).setRenderingInformation(r2);
		}
		
	}
	
	public DirectionalValues getBorderSizes(DiagramElement de, Dimension2D contents, BoxStyle bs, FlexibleShape borderShape) {
		DirectionalValues internalPadding = requiresDimension(de) ? bs.getInternalPadding() : DirectionalValues.ZERO;
		Dimension2D padded = new Dimension2D(
				contents.getWidth() + internalPadding.getLeft() + internalPadding.getRight(),
				contents.getHeight() + internalPadding.getTop() + internalPadding.getBottom());
		DirectionalValues borderPadding = (borderShape != null) ?  borderShape.getBorderSizes(padded) : DirectionalValues.ZERO;
		borderPadding = borderPadding.add(internalPadding);
		borderPadding = borderPadding.add(trimForStroke(bs));
		return borderPadding;
	}
	
	@Override
	public double getPadding(DiagramElement element, Direction d) {
		Dimension2D rect = ((RectangleRenderingInformation)(((PositionableDiagramElement)element).getRenderingInformation())).getInternalSize();
		BoxStyle bs = getBoxStyle(element);
		if (requiresDimension(element)) {
			FlexibleShape fs = getBorderShape(element);
			DirectionalValues internalPadding = bs.getInternalPadding();
			if (fs != null) {
				internalPadding = internalPadding.add(fs.getBorderSizes(rect));
			}
			return internalPadding.get(d);
		} else {
			return 0;
		}
	}

	/**
	 * Makes sure the stroke is inset slightly so that we don't expand past the border rectangle.
	 */
	public double trimForStroke(BoxStyle bs) {
		return bs == null ? 0 : bs.getStrokeWidth() / 2;
	}
	
	protected boolean applyBoxContentCentering() {
		return true;
	}
	
	protected void drawBoxContents(DiagramElement element, RectangleRenderingInformation r3) {
	}

	@Override
	public CostedDimension size(DiagramElement element, Dimension2D within) {
		BoxStyle bs = getBoxStyle(element);
		FlexibleShape fs = getBorderShape(element);
		Dimension2D innerWithin = getContentArea(within, fs, bs);
		Dimension2D sizeInner = sizeBoxContents(element, innerWithin);
		DirectionalValues padding = getBorderSizes(element, sizeInner, bs, fs);
		DirectionalValues margin = bs.getMargin();
		double xpad = padding.getLeft() + padding.getRight() + margin.getLeft() + margin.getRight();
		double ypad = padding.getTop() + padding.getBottom() + margin.getTop() + margin.getBottom();
		return new CostedDimension((sizeInner != null ? sizeInner.getWidth() : 0 )+ xpad, (sizeInner != null ? sizeInner.getHeight() : 0) + ypad, within);
	}

	protected abstract Dimension2D sizeBoxContents(DiagramElement element, Dimension2D within);

	protected Double getDrawingRectangle(DiagramElement de, RectangleRenderingInformation ri) {
		return new Rectangle2D.Double(ri.getPosition().x(), 
				ri.getPosition().y(), 
				ri.getSize().x(), 
				ri.getSize().y());
	}

	@Override
	public double getLinkMargin(DiagramElement de, Direction d) {
		BoxStyle bs = getBoxStyle(de);
		FlexibleShape fs = getBorderShape(de);
		DirectionalValues dv = bs.getMargin();
		return dv.get(d) + (fs != null ? fs.getMargin().get(d) : 0);
	}
	

	public DirectionalValues getMargin(FlexibleShape borderShape, BoxStyle bs) {
		DirectionalValues borderMargin = borderShape == null ? DirectionalValues.ZERO : borderShape.getMargin();
		return borderMargin.add(bs.getMargin());
	}

	
	public Shape getPerimeter(DiagramElement de, RectangleRenderingInformation rri) {
		BoxStyle bs = getBoxStyle(de);
		FlexibleShape fs = getBorderShape(de);
		
		if (fs != null) {
			Rectangle2D ri = getDrawingRectangle(de, rri);		
			
			if (fs.hasSpecialPerimiter()) {
				return fs.getPerimeterShape(ri.getMinX(), ri.getMinY(), ri.getMaxX(), ri.getMaxY());
			}
			
			return getShape(bs, fs, ri.getMinX(), ri.getMinY(), ri.getMaxX(), ri.getMaxY());
		} else {
			return null;
		}
	}
	
	public Shape getShape(BoxStyle bs, FlexibleShape fs, double x1, double y1, double x2, double y2) {
		double trim = trimForStroke(bs);
		return fs.getShape(x1+trim, y1+trim, x2-trim, y2-trim);
	}

	private Dimension2D getContentArea(Dimension2D within, FlexibleShape borderShape, BoxStyle bs) {
		within = stripMargins(within, bs);
		Dimension2D remaining = (borderShape != null)  ? borderShape.getContentArea(within) : within;
		double stroke = trimForStroke(bs) * 2;
		DirectionalValues internalPadding = bs.getInternalPadding();
		return new Dimension2D(remaining.getWidth() - internalPadding.getLeft() - internalPadding.getRight() - stroke,
				remaining.getHeight() - internalPadding.getTop() - internalPadding.getBottom() - stroke);
	}

	protected Dimension2D stripMargins(Dimension2D within, BoxStyle bs) {
		DirectionalValues margins = bs.getMargin();
		within = new Dimension2D(within.getWidth() - margins.getLeft() - margins.getRight(), 
				within.getHeight() - margins.getTop() - margins.getBottom());
		return within;
	}
	
	protected Rectangle2D stripMargins(Rectangle2D within, BoxStyle bs) {
		DirectionalValues margins = bs.getMargin();
		return new Rectangle2D.Double(within.getMinX() + margins.getLeft(), 
				within.getMinY() + margins.getTop(),
				within.getWidth() - margins.getLeft() - margins.getRight(),
				within.getHeight() - margins.getTop() - margins.getBottom());
	}
	

	public void paintBackground(BoxStyle bs, FlexibleShape fs, GraphicsLayer g2, double x1, double y1, double x2,
			double y2) {
		final ShapeStyle borderStyle = bs; 
		if ((borderStyle != null) && (borderStyle.isFilled())) {
			AffineTransform orig = new AffineTransform(g2.getTransform());
			double trim = trimForStroke(bs);
			g2.translate(x1+trim, y1+trim);
			double xs = x2-x1 - 2*trim;
			double ys = y2-y1 - 2*trim;
			if ((xs > 0) && (ys > 0)) { 
				g2.scale(xs, ys);
				final AffineTransform paintTransform = g2.getTransform();
				g2.setTransform(orig);
				Shape shape = getShape(bs, fs, x1, y1, x2, y2);
				final Paint p = borderStyle.getBackground(shape);
	
				if (p == null) {
					return;
				}
				
				if (p instanceof Color) {
					// ensures correct transparency settings by treating 
					// this separately.
					g2.setColor((Color)p);
				} else {
					// handles gradient fills by centering the painting 
					// context on what is being drawn
					Paint back2 = new TransformedPaint(p, paintTransform, (String) borderStyle.getBackgroundKey());
					
					g2.setPaint(back2);
				}
				g2.fill(shape);
			}
		}
	}
}
