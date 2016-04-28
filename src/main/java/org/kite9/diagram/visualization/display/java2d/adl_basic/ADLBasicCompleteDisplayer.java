package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.visualization.display.ComponentDisplayer;
import org.kite9.diagram.visualization.display.java2d.AbstractOrderedGraphics2DCompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.io.PathConverter;
import org.kite9.diagram.visualization.display.java2d.style.sheets.BasicStylesheet;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;

/**
 * Handles display of components and shadows
 * 
 * @author robmoffat
 *
 */
public class ADLBasicCompleteDisplayer extends AbstractOrderedGraphics2DCompleteDisplayer {

	boolean watermark;
	
	public ADLBasicCompleteDisplayer(Stylesheet ss, boolean watermark, boolean buffer) {
		super(ss == null ? new BasicStylesheet() : ss, buffer);
		this.watermark = watermark;
	}

	private GraphicsSourceRenderer<?> gs;
	private Dimension2D imageSize;
	private PathConverter pc = new PathConverter() {
		
		@Override
		public String convert(Shape shape, double x, double y) {
			return null;
		}
	};
	
	public PathConverter getPathConverter() {
		return pc;
	}

	public void setPathConverter(PathConverter pc) {
		this.pc = pc;
	}
	
	public static final int SHADOW_LAYER = 0;
	public static final int FG_LAYER = 1;
		

	@Override
	public void initialize(GraphicsSourceRenderer<?> gs, Dimension2D diagramSize) {
		super.initialize(gs, diagramSize);
		Graphics2D g2 = gs.getGraphicsLayer(SHADOW_LAYER, 1f, diagramSize);
		Paint p = ss.getBackground();
		this.imageSize = gs.getImageSize(diagramSize);
		
		paintBackground(g2, imageSize, p);
		this.gs = gs;
		
		// need to do shadows first
		orderedRender(g2, true, ss.getShadowXOffset(), ss.getShadowYOffset());		

		// now do the foreground, connections first
		g2 = gs.getGraphicsLayer(FG_LAYER, 1f, diagramSize);
		orderedRender(g2, false, 0, 0);		
		
		// debug overlay
		displayers.add(new DebugLineDisplayer(this, ss, g2));
	}



	private void orderedRender(Graphics2D g2, boolean shadow, int xo, int yo) {
		displayers.add(new DiagramDisplayer(this, ss, g2, shadow, xo, yo));
		displayers.add(new ContextDisplayer(this, ss, g2, shadow, xo, yo));
		displayers.add(new LinkDisplayer(this, ss, g2, shadow, xo, yo) {

			@Override
			protected Shape getPerimeterShape(DiagramElement de) {
				ComponentDisplayer cd = getDisplayer(de);
				if (cd instanceof AbstractBoxModelDisplayer) {
					return ((AbstractBoxModelDisplayer)cd).getPerimeter(de, 
							(RectangleRenderingInformation) ((PositionableDiagramElement)de).getRenderingInformation());
				} else {
					return null;
				}
			}			
		});
		displayers.add(new ConnectionLabelTextLineDisplayer(this, g2, ss, shadow, xo, yo));
		displayers.add(new ContextLabelTextLineDisplayer(this, g2, ss, shadow, xo, yo));
		displayers.add(new GlyphDisplayer(this, ss, g2, shadow, xo, yo));
		displayers.add(new GlyphTextLineDisplayer(this, g2, ss, shadow, xo, yo));
		displayers.add(new GlyphCompositionalShapeDisplayer(this, g2, ss, shadow, xo, yo));
		displayers.add(new KeyDisplayer(this, ss, g2, shadow, xo, yo));
		displayers.add(new KeyTextLineDisplayer(this, g2, ss, shadow, xo, yo));
		displayers.add(new ConnectionBodyDisplayer(this, ss, g2, shadow, xo, yo));
		
	}
   
	@Override
	public void finish() {
		super.finish();
		
		if (watermark) {
			new WatermarkRenderer().display(gs, this.imageSize, ss);
		}
	}


	public void paintBackground(Graphics2D g2, Dimension2D d, Paint p) {
		if (d != null) {
			g2.setPaint(p);
			AffineTransform at = g2.getTransform();
			AffineTransform at2 = new AffineTransform();
			at2.scale(d.getWidth(), d.getHeight());
			g2.setTransform(at2);
			g2.fillRect(0, 0, 1, 1);
			g2.setTransform(at);
	//		} else {
	//			g2.fillRect(0, 0, (int) d.getWidth(), (int) d.getHeight());			
	//		}
		}
	}



	@Override
	public boolean isOutputting() {
		return true;
	}



	@Override
	public void setOutputting(boolean outputting) {
		for (ComponentDisplayer cd : displayers) {
			cd.setOutputting(outputting);
		}
	}
	
}
