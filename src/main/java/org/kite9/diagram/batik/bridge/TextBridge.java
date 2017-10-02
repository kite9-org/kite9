package org.kite9.diagram.batik.bridge;


import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.FlowGlyphLayout;
import org.apache.batik.bridge.FlowTextPainter;
import org.apache.batik.bridge.TextLayoutFactory;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextSpanLayout;
import org.apache.batik.bridge.svg12.SVG12TextElementBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.svggen.SVGGraphics2D;
import org.kite9.diagram.batik.format.ExtendedSVG;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Element;


/**
 * Overrides the regular text bridge to allow SVG to be written as text, rather than converted to glyph vectors each time.
 * 
 * @author robmoffat
 *
 */
public class TextBridge extends SVG12TextElementBridge {
	
	
	
	public TextBridge() {
		super();
		try {
			this.someFont1 = Font.createFont(Font.TRUETYPE_FONT, TextBridge.class.getResourceAsStream("/fonts/Arial Italic.ttf"));
			this.someFont2 = Font.createFont(Font.TRUETYPE_FONT, TextBridge.class.getResourceAsStream("/fonts/opensans-regular-webfont.ttf"));
		} catch (Exception e) {
			throw new LogicException();
		}
	}

	private final Font someFont1;
	private final Font someFont2;
	

	class ExtendedTextLayoutFactory implements TextLayoutFactory {
		
		public ExtendedTextLayoutFactory(Element el) {
			super();
			this.el = el;
		}

		private Element el;
		
		@Override
		public TextSpanLayout createTextLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
			return new FlowGlyphLayout(aci, charMap, offset, frc) {

				@Override
				public void draw(Graphics2D g2d) {
					if (g2d instanceof ExtendedSVG) {
						ExtendedSVG eSVG = (ExtendedSVG) g2d;
						Paint basePaint = g2d.getPaint();
						Font baseFont = g2d.getFont();
						TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute (GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
				        if (tpi == null) return;
				        if (!tpi.visible) return;

				        Paint  fillPaint   = tpi.fillPaint;
				        
				        if (fillPaint != null) {
				        	g2d.setFont(someFont2.deriveFont(40));
			                g2d.setPaint(fillPaint);
			                eSVG.transcribeXML(el);
			            }
					        
						g2d.setPaint(basePaint);
						g2d.setFont(baseFont);
					} else {
						super.draw(g2d);
					}
				}
			};
		}
	};

	@Override
	public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
		super.buildGraphicsNode(ctx, e, node);
		((TextNode)node).setTextPainter(new FlowTextPainter() {
			
			
			@Override
			protected TextLayoutFactory getTextLayoutFactory() {
				return new ExtendedTextLayoutFactory(e);
			}
			
		});
	}
	
}
