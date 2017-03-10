package org.kite9.diagram.visualization.batik.bridge;


import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.ConcreteTextLayoutFactory;
import org.apache.batik.bridge.FlowTextPainter;
import org.apache.batik.bridge.GlyphLayout;
import org.apache.batik.bridge.TextLayoutFactory;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextSpanLayout;
import org.apache.batik.bridge.svg12.SVG12TextElementBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.w3c.dom.Element;


/**
 * Overrides the regular text bridge to allow SVG to be written as text, rather than converted to glyph vectors each time.
 * 
 * @author robmoffat
 *
 */
public class TextBridge extends SVG12TextElementBridge {

	private static final TextLayoutFactory TEXT_LAYOUT_FACTORY = new ConcreteTextLayoutFactory() {

		@Override
		public TextSpanLayout createTextLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
			return new GlyphLayout(aci, charMap, offset, frc) {

				@Override
				public void draw(Graphics2D g2d) {
					Paint basePaint = g2d.getPaint();
					TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute (GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
			        if (tpi == null) return;
			        if (!tpi.visible) return;

			        Paint  fillPaint   = tpi.fillPaint;
			        
			        if (fillPaint != null) {
		                g2d.setPaint(fillPaint);
						g2d.drawString(aci, (float) getOffset().getX(), (float) getOffset().getY());
		            }

				        
					g2d.setPaint(basePaint);
				}
			};
		}
	};

	private static final FlowTextPainter TEXT_PAINTER = new FlowTextPainter() {

		@Override
		protected TextLayoutFactory getTextLayoutFactory() {
			return TEXT_LAYOUT_FACTORY;
		}

	};

	@Override
	public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
		super.buildGraphicsNode(ctx, e, node);
		((TextNode)node).setTextPainter(TEXT_PAINTER);
	}


	
	
}
