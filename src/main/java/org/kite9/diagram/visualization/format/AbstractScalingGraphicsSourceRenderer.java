package org.kite9.diagram.visualization.format;

import java.awt.Graphics2D;

import org.kite9.diagram.position.Dimension2D;

/**
 * Provides optional width and height parameters, allowing you to scale the output image to whatever size you want.
 * 
 * Where the exact dimensions are specified, the diagram is surrounded with enough whitespace so that the whole diagram is 
 * shown.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractScalingGraphicsSourceRenderer<X> extends AbstractGraphicsSourceRenderer<X> {

	private Integer width, height;
	
	static class ResponseHolder {
		Dimension2D size;
		float scale;
	}
	
	public AbstractScalingGraphicsSourceRenderer(Integer width, Integer height) {
		this.width = width;
		this.height = height;
	}
	
	public final GraphicsLayer getGraphicsLayer(GraphicsLayerName layer, Dimension2D size) {
		if (size.getWidth() == 1) {
			return getGraphics(layer, 1f, size, size);
		}
		
		ResponseHolder rh = getScaling(size);
		return getGraphics(layer, rh.scale, rh.size, size);
	}

	protected ResponseHolder getScaling(Dimension2D size) {
		float scale = 1;
		if (width != null) {
			scale = (float) width / (float) size.getWidth();
		}
		
		if (height != null) {
			float scale2 = (float) height / (float) size.getHeight();
			scale = Math.min(scale, scale2);
		}
		
		int pixelWidth = width == null ? (int) (size.getWidth() * scale) : width;
		int pixelHeight = height == null ? (int) (size.getHeight() * scale) : height;
		
		ResponseHolder out = new ResponseHolder();
		out.scale = scale;
		out.size = new Dimension2D(pixelWidth, pixelHeight);
		
		return out;
	}


	protected void applyScaleAndTranslate(Graphics2D g2, float scale, Dimension2D imageSize, Dimension2D diagramSize) {
		double sx = (imageSize.getWidth()-(diagramSize.getWidth()*scale)) / 2f;
		double sy = (imageSize.getHeight()-(diagramSize.getHeight()*scale)) / 2f;
		g2.translate((int) sx, sy);
		g2.scale(scale, scale);
	}
	
	@Override
	public Dimension2D getImageSize(Dimension2D diagramSize) {
		return getScaling(diagramSize).size;
	}

	protected abstract GraphicsLayer getGraphics(GraphicsLayerName layer, float scale, Dimension2D imageSize, Dimension2D diagramArea);
}
