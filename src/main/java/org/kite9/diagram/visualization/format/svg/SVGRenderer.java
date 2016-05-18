package org.kite9.diagram.visualization.format.svg;

import java.awt.AlphaComposite;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.format.AbstractScalingGraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGRenderer extends AbstractScalingGraphicsSourceRenderer<String> {

	protected DOMImplementation domImpl;
	protected Document document;
	protected Element topGroup;
	protected SVGGraphics2D g2;
	protected Integer internalWidth, internalHeight;
	
	public SVGRenderer() {
		this(null, null);
	}
	
	public SVGRenderer(Integer width, Integer height) {
		super(width, height);
		
		// ensure we can embed textures in svg
		if (ImageWriterRegistry.getInstance().getWriterFor("image/png") == null) {
			ImageWriterRegistry.getInstance().register(new ImageWriter() {
				
				@Override
				public void writeImage(RenderedImage image, OutputStream out, ImageWriterParams params) throws IOException {
					ImageIO.write(image, "PNG", out);
				}
				
				@Override
				public void writeImage(RenderedImage image, OutputStream out) throws IOException {
					ImageIO.write(image, "PNG", out);
				}
				
				@Override
				public String getMIMEType() {
					return "image/png";
				}
			});
		}
	}


	public String render(Diagram something) {
		try {
			Dimension2D out = size(something);
			dea.initialize(this, out);
			drawDiagramElements(something);
			dea.finish();
			String outString = output(something);
			g2.dispose();
			return outString;
		} catch (Exception e) {
			throw new Kite9ProcessingException("Couldn't create SVG Graphics:", e);
		}
	}

	protected String output(Diagram something) throws SVGGraphics2DIOException, IOException {
		g2.setTopLevelGroup(topGroup);
		Writer outw = new StringWriter();
		g2.stream(outw, true);
		outw.flush();
		outw.close();
		return outw.toString();
	}

	public GraphicsLayer getGraphics(GraphicsLayerName layer, float transparency, float scale, Dimension2D imageSize, Dimension2D diagramSize) {
		
		if ((internalWidth==null) || (internalWidth != imageSize.getWidth()) ||  (internalHeight != imageSize.getHeight())) {
			// starting a new image size
			if (g2 != null) {
				g2.dispose();
			}
			
			this.internalWidth = (int) imageSize.getWidth();
			this.internalHeight = (int) imageSize.getHeight();
			
			domImpl = GenericDOMImplementation.getDOMImplementation();
		    String svgNS = "http://www.w3.org/2000/svg";
		    document = domImpl.createDocument(svgNS, "svg", null);
		    SVGGeneratorContext context = SVGGeneratorContext.createDefault(document);
		    context.setExtensionHandler(new GradientExtensionHandler());
		    
			g2 = new SVGGraphics2D(context, false); 
			
			topGroup = g2.getTopLevelGroup();
						
			setRenderingHints(g2);
			applyScaleAndTranslate(g2, scale, imageSize, diagramSize);
		}
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,transparency));

		return createGraphicsLayer(layer);
	}

	protected SVGGraphicsLayer createGraphicsLayer(GraphicsLayerName layer) {
		return new SVGGraphicsLayer(g2, layer, document, topGroup, externalizeFonts());
	}

	protected boolean externalizeFonts() {
		return false;
	}

}
