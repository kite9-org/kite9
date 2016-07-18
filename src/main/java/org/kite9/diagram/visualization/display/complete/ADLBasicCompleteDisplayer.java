package org.kite9.diagram.visualization.display.complete;

import java.awt.Shape;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.visualization.display.Displayer;
import org.kite9.diagram.visualization.display.components.AbstractBoxModelDisplayer;
import org.kite9.diagram.visualization.display.components.BackgroundDisplayer;
import org.kite9.diagram.visualization.display.components.ConnectionBodyDisplayer;
import org.kite9.diagram.visualization.display.components.ConnectionLabelTextLineDisplayer;
import org.kite9.diagram.visualization.display.components.ContextDisplayer;
import org.kite9.diagram.visualization.display.components.ContextLabelTextLineDisplayer;
import org.kite9.diagram.visualization.display.components.DebugLineDisplayer;
import org.kite9.diagram.visualization.display.components.GlyphCompositionalShapeDisplayer;
import org.kite9.diagram.visualization.display.components.GlyphDisplayer;
import org.kite9.diagram.visualization.display.components.GlyphTextLineDisplayer;
import org.kite9.diagram.visualization.display.components.KeyDisplayer;
import org.kite9.diagram.visualization.display.components.KeyTextLineDisplayer;
import org.kite9.diagram.visualization.display.components.LinkDisplayer;
import org.kite9.diagram.visualization.display.components.WatermarkDisplayer;
import org.kite9.diagram.visualization.display.style.io.PathConverter;
import org.kite9.diagram.visualization.display.style.io.StaticStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;

/**
 * Handles display of components and shadows
 * 
 * @author robmoffat
 *
 */
public class ADLBasicCompleteDisplayer extends AbstractOrderedDisplayer {

	boolean watermark;
	
	public ADLBasicCompleteDisplayer(boolean watermark, boolean buffer) {
		this(watermark, buffer, 12);
	}
	
	public ADLBasicCompleteDisplayer(boolean watermark, boolean buffer, int gridSize) {
		super(buffer, gridSize);
		this.watermark = watermark;
	}

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

	@Override
	public void initialize(GraphicsSourceRenderer<?> gs, Dimension2D diagramSize) {
		super.initialize(gs, diagramSize);
		initBackgroundLayer();			
		initShadows();		
		initMainLayer();	
		initWatermarkLayer();
		initCopyrightLayer();
		initFlannelLayer();
		initDebugLayer();
	}

	private void initFlannelLayer() {
		// TODO Auto-generated method stub
		
	}
	
	private void initWatermarkLayer() {
		GraphicsLayer g2 = gs.getGraphicsLayer(GraphicsLayerName.WATERMARK, diagramSize);
		displayers.add(new WatermarkDisplayer(this, g2, true));		// watermark
	}
	
	private void initCopyrightLayer() {
		GraphicsLayer g2 = gs.getGraphicsLayer(GraphicsLayerName.COPYRIGHT, diagramSize);
		displayers.add(new WatermarkDisplayer(this, g2, false));	// copyright
	}

	public void initDebugLayer() {
		GraphicsLayer g2 = gs.getGraphicsLayer(GraphicsLayerName.DEBUG, diagramSize);
		displayers.add(new DebugLineDisplayer(this, g2));
	}

	public void initMainLayer() {
		GraphicsLayer g2 = gs.getGraphicsLayer(GraphicsLayerName.MAIN, diagramSize);
		orderedRender(g2, false);
	}

	public void initShadows() {
		GraphicsLayer g2 = gs.getGraphicsLayer(GraphicsLayerName.SHADOW, diagramSize);
		orderedRender(g2, true);
	}

	public void initBackgroundLayer() {
		GraphicsLayer g2 = gs.getGraphicsLayer(GraphicsLayerName.BACKGROUND, diagramSize);
		displayers.add(new BackgroundDisplayer(this, g2));
	}



	private void orderedRender(GraphicsLayer g2, boolean shadow) {
//		displayers.add(new AbstractDiagramDisplayer(this, ss, g2, shadow));
		displayers.add(new ContextDisplayer(this, g2, shadow));
		displayers.add(new LinkDisplayer(this, g2, shadow) {

			@Override
			protected Shape getPerimeterShape(DiagramElement de) {
				Displayer cd = getDisplayer(de);
				if (cd instanceof AbstractBoxModelDisplayer) {
					return ((AbstractBoxModelDisplayer)cd).getPerimeter(de, 
							(RectangleRenderingInformation) ((PositionableDiagramElement)de).getRenderingInformation());
				} else {
					return null;
				}
			}			
		});
		displayers.add(new ConnectionLabelTextLineDisplayer(this, g2, shadow));
		displayers.add(new ContextLabelTextLineDisplayer(this, g2, shadow));
		displayers.add(new GlyphDisplayer(this, g2, shadow));
		displayers.add(new GlyphTextLineDisplayer(this, g2, shadow));
		displayers.add(new GlyphCompositionalShapeDisplayer(this, g2, shadow));
		displayers.add(new KeyDisplayer(this, g2, shadow));
		displayers.add(new KeyTextLineDisplayer(this, g2, shadow));
		displayers.add(new ConnectionBodyDisplayer(this, g2, shadow));
	}
   
	@Override 
	public void finish() {
		super.finish();
	}


	@Override
	public boolean isOutputting() {
		return true;
	}



	@Override
	public void setOutputting(boolean outputting) {
		for (Displayer cd : displayers) {
			cd.setOutputting(outputting);
		}
	} 	
}
