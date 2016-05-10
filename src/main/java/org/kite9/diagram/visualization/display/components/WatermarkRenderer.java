package org.kite9.diagram.visualization.display.components;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.display.style.TextStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This helper class loads the logo up from an SVG file and adds it to the graphics2d context.
 * 
 * @author robmoffat
 *
 */
public class WatermarkRenderer {
	
	public static final String COPYRIGHT_NORMAL = "diagrams rendered by kite9.com";
	public static final String COPYRIGHT_SHORT = "kite9.com";
	
	public static final int COPYRIGHT_MIN_DIMENSION = 250;	

	public static void main(String[] args) {
		new WatermarkRenderer().displayWatermark(null, new Dimension2D(300, 300), null);
	}
	
	public void display(GraphicsSourceRenderer<?> gs, Dimension2D size, Stylesheet ss) {
		displayWatermark(gs.getGraphicsLayer(GraphicsLayerName.WATERMARK, (float) .15, size), size, ss);

		if ((size.getWidth() < COPYRIGHT_MIN_DIMENSION) || (size.getHeight() < COPYRIGHT_MIN_DIMENSION)) {
			// in these cases, the image is too small to complicate further with a watermark
			return;
		}
			
		displayCopyright(gs.getGraphicsLayer(GraphicsLayerName.COPYRIGHT, (float) 1, size), size, ss); 
	}
	
	public void displayCopyright(GraphicsLayer g2, Dimension2D size, Stylesheet ss) {
		AffineTransform at = g2.getTransform();
		g2.setTransform(new AffineTransform());

		TextStyle ts = ss.getCopyrightStyle();
		Font f = ts.getFont();
		
		FontMetrics fontMetrics = g2.getFontMetrics(f);
		double widthNorm = g2.getStringBounds(fontMetrics, COPYRIGHT_NORMAL).getWidth();
		double widthShort = g2.getStringBounds(fontMetrics, COPYRIGHT_SHORT).getWidth();
		
		String text = COPYRIGHT_NORMAL;
		double width = widthNorm;
		if (widthNorm > size.getWidth()) {
			text = COPYRIGHT_SHORT;
			width = widthShort;
		}
		
		double space = 10;
		double descent = fontMetrics.getDescent();
		double remainder = space - fontMetrics.getHeight();
		double bottom = Math.max(0, remainder / 2);
		
		g2.setFont(f);
		g2.setColor(ts.getColor());
		g2.outputText(f, (size.getHeight() - descent - bottom), ((size.getWidth() / 2d) - (width / 2d)), text);
		
		g2.setTransform(at);
		
	}
	
	public void displayWatermark(GraphicsLayer g2, Dimension2D size, Stylesheet ss) {
		try {
			AffineTransform at = g2.getTransform();
			g2.setTransform(new AffineTransform());
			InputStream theFile = this.getClass().getResourceAsStream("/logo/kite9logo.svg.xml");
			
			 DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			 dbfac.setValidating(false);
			 dbfac.setNamespaceAware(false);
			 DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			 Document d = docBuilder.parse(theFile);
			 
			 // get the path info
			 NodeList nl = d.getElementsByTagName("path");
			 
			 for (int i = 0; i < nl.getLength(); i++) {
				Node xmlNode = nl.item(i);
				if (xmlNode.getNodeName().endsWith("path")) {
					// ok, we have the data, create the path
					Node pathAttr = xmlNode.getAttributes().getNamedItem("d");
					String text  = pathAttr.getTextContent();
					GeneralPath gp = parseSVG(text);
					
					// center path at zero
					Rectangle2D bounds = gp.getBounds2D();
					gp.transform(AffineTransform.getTranslateInstance(-bounds.getCenterX(), -bounds.getCenterY()));
					
					// scale to 1
					gp.transform(AffineTransform.getScaleInstance(1 / bounds.getWidth(), 1 / bounds.getWidth()));
					
					// scaling - 1/5 of total width
					double scale = Math.max(size.getWidth(), size.getHeight()) / 4.0d;
					gp.transform(AffineTransform.getScaleInstance(scale, scale));
					
					// center in image
					gp.transform(AffineTransform.getTranslateInstance(size.getWidth() / 2.0d, size.getHeight() / 2.0d));
					
					g2.setColor(ss.getWatermarkColour());
					g2.fill(gp);
				}
			}
			g2.setTransform(at);
		} catch (Exception e) {
			throw new LogicException("Could not render logo", e);
		}
	}

	private GeneralPath parseSVG(String text) {
		int cmdStart = 0;
		
		GeneralPath g2 = new GeneralPath();
				
		for (int j = 1; j < text.length(); j++) {
			if (Character.isLetter(text.charAt(j))) {
				processCommand(g2, text.substring(cmdStart, j-1));
				cmdStart = j;
			}
		}
		
		return g2;
	}

	private void processCommand(GeneralPath g2, String substring) {
		char command = substring.charAt(0);
		char lcCommand = Character.toLowerCase(command);
		if (lcCommand=='m') {
			processMove(g2, parseArgs(substring.substring(1)), Character.isLowerCase(command));
		} else if (lcCommand=='l') {
			processLine(g2, parseArgs(substring.substring(1)), Character.isLowerCase(command));			
		} else if (lcCommand=='c') {
			processCurve(g2, parseArgs(substring.substring(1)), Character.isLowerCase(command));			
		} else if (lcCommand=='z') {
			processClose(g2);
		}
	}
	
	private void processClose(GeneralPath g2) {
		g2.closePath();
	}

	private void processCurve(GeneralPath g2, Float[] args, boolean rel) {
		Point2D p = g2.getCurrentPoint();
		makeAbsolute(args, rel, g2);
		//Line2D cc2d = new Line2D.Float(p, new Point2D.Float(args[4], args[5]));
		CubicCurve2D cc2d = new CubicCurve2D.Float((float) p.getX(), (float) p.getY(), args[0], args[1], args[2], args[3], args[4], args[5]);
		g2.append(cc2d, true);
	}

	private void makeAbsolute(Float[] args, boolean rel, GeneralPath g2) {
		if (rel) {
			Point2D p = g2.getCurrentPoint();
			for (int i = 0; i < args.length; i++) {
				if (i % 2 ==0) {
					args[i] += (float) p.getX();
				} else {
					args[i] += (float) p.getY();					
				}
			}
		}
	}

	private void processLine(GeneralPath g2, Float[] args, boolean rel) {
		makeAbsolute(args, rel, g2);
		g2.lineTo(args[0], args[1]);
	}

	private void processMove(GeneralPath g2, Float[] args, boolean rel) {
		makeAbsolute(args, rel, g2);
		g2.moveTo(args[0], args[1]);
	}

	private Float[] parseArgs(String substring) {
		List<Float> args = new ArrayList<Float>();
		try {
			int argStart = 0;
			for (int i = 0; i < substring.length(); i++) {
				char c = substring.charAt(i);
				if (c==',') {
					args.add(Float.parseFloat(substring.substring(argStart, i)));
					argStart = i+1;
				} else if ((c=='-') && (i > argStart)) {
					args.add(Float.parseFloat(substring.substring(argStart, i)));
					argStart = i;				
				}
			}
			
			args.add(Float.parseFloat(substring.substring(argStart)));
			
			return (Float[]) args.toArray(new Float[args.size()]);
		} catch (NumberFormatException e) {
			throw new LogicException("Could not parse: "+substring+" args: "+args,e);
		}
	}
}
