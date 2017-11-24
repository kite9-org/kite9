package org.kite9.diagram.batik.bridge;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.FlowGlyphLayout;
import org.apache.batik.bridge.FlowTextNode;
import org.apache.batik.bridge.FlowTextPainter;
import org.apache.batik.bridge.TextLayoutFactory;
import org.apache.batik.bridge.TextNode.Anchor;
import org.apache.batik.bridge.TextSpanLayout;
import org.apache.batik.bridge.svg12.SVGFlowRootElementBridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.kite9.diagram.batik.format.ExtendedSVG;
import org.kite9.diagram.batik.format.ExtendedSVGGraphics2D;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;


/**
 * Overrides the regular text bridge to allow SVG to be written as text, rather than converted to glyph vectors each time.
 * 
 * @author robmoffat
 *
 */
public class LocalRenderingFlowRootElementBridge extends SVGFlowRootElementBridge {

	static final TextLayoutFactory CUSTOM_TEXT_LAYOUT = new TextLayoutFactory() {
		
		@Override
		public TextSpanLayout createTextLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
			return new FlowGlyphLayout(aci, charMap, offset, frc) {

				@Override
				public void draw(Graphics2D g2d) {
					if (g2d instanceof ExtendedSVG) {
						ExtendedSVGGraphics2D eSVG = (ExtendedSVGGraphics2D) g2d;
						Paint basePaint = g2d.getPaint();
						Font baseFont = g2d.getFont();
						TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute (GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
						float lineHeight = (float) aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.LINE_HEIGHT);
						@SuppressWarnings("unchecked")
						List<GVTFontFamily> gvtFontFamilies = (List<GVTFontFamily>) aci.getAttribute(GVT_FONT_FAMILIES);

				        if (tpi == null) return;
				        if (!tpi.visible) return;

				        Paint  fillPaint   = tpi.fillPaint;
				        
				        if (fillPaint != null) {
					        Font toUse = eSVG.handleGVTFontFamilies(gvtFontFamilies);
				        	g2d.setFont(toUse);
			                g2d.setPaint(fillPaint);
							float x = (float) getOffset().getX();
							float y = (float) getOffset().getY();
							g2d.drawString(aci, x, y);
							eSVG.setTextBounds(eSVG.getTextBounds().createUnion(new Rectangle2D.Float(x, y, 0f, lineHeight)));
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
		FlowTextNode fn = getFlowNode(node);
		
		fn.setTextPainter(new FlowTextPainter() {
			
			
			@Override
			protected TextLayoutFactory getTextLayoutFactory() {
				return CUSTOM_TEXT_LAYOUT;
			}
			
		});
	}
	
	public static FlowTextNode getFlowNode(GraphicsNode n) {
		CompositeGraphicsNode cgn = (CompositeGraphicsNode) n;
		
		for (GraphicsNode gn : (List<GraphicsNode>) cgn.getChildren()) {
			if (gn instanceof FlowTextNode) {
				return (FlowTextNode) gn;
			}
		}
		
		throw new Kite9ProcessingException("No Flow node!");
	}
	
}
