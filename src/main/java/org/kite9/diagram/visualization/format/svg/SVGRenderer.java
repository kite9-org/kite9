package org.kite9.diagram.visualization.format.svg;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.format.AbstractScalingGraphicsSourceRenderer;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class SVGRenderer extends AbstractScalingGraphicsSourceRenderer<String> {

	private DOMImplementation domImpl;
	private Document document;
	private SVGGraphics2D g2;
	private Integer internalWidth, internalHeight;
	
	public SVGRenderer() {
		this(null, null);
	}
	
	public SVGRenderer(Integer width, Integer height) {
		super(width, height);
	}


	public String render(Diagram something) {
		try {
			Dimension2D out = size(something);
			dea.initialize(this, out);
			drawDiagramElements(something);
			dea.finish();
			g2.dispose();
			
			Writer outw = new StringWriter();
			g2.stream(outw, true);
			outw.flush();
			outw.close();
			return outw.toString();
		} catch (Exception e) {
			throw new Kite9ProcessingException("Couldn't create SVG Graphics:", e);
		}
	}

	public Graphics2D getGraphics(int layer, float transparency, float scale, Dimension2D imageSize, Dimension2D diagramSize) {
		
		if ((internalWidth==null) || (internalWidth != imageSize.getWidth()) ||  (internalHeight != imageSize.getHeight())) {
			domImpl = GenericDOMImplementation.getDOMImplementation();
		    String svgNS = "http://www.w3.org/2000/svg";
		    document = domImpl.createDocument(svgNS, "svg", null);
		    SVGGeneratorContext context = SVGGeneratorContext.createDefault(document);
		    context.setExtensionHandler(new GradientExtensionHandler());
		    
			g2 = new SVGGraphics2D(context, false);	
			setRenderingHints(g2);
			applyScaleAndTranslate(g2, scale, imageSize, diagramSize);
			this.internalWidth = (int) imageSize.getWidth();
			this.internalHeight = (int) imageSize.getHeight();
		}

		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,transparency));
		
		return g2;
	}

}
