package org.kite9.diagram.batik.text;

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
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextSpanLayout;
import org.apache.batik.bridge.svg12.SVGFlowRootElementBridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.AttributedCharacterSpanIterator;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;

/**
 * Overrides the regular text bridge to allow SVG to be written as text, rather
 * than converted to glyph vectors each time.
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
						// remember this stuff
						ExtendedSVGGraphics2D eSVG = (ExtendedSVGGraphics2D) g2d;
						Paint basePaint = g2d.getPaint();
						Font baseFont = g2d.getFont();

						// iterate over glyphs
						GVTGlyphVector gv = getGlyphVector();
						int charStart = 0;
						int charEnd = 0;
						double linePosition = 0;
						double startPosition = 0;
						for (int i = 0; i < gv.getNumGlyphs(); i++) {
							if (gv.isGlyphVisible(i)) {
								int charCount = gv.getCharacterCount(i, i);
								Point2D p = gv.getGlyphPosition(i);
								if (linePosition != p.getY()) {
									if (charEnd - charStart > 0) {
										AttributedCharacterIterator innerAci = new AttributedCharacterSpanIterator(aci, charStart, charEnd);
										outputTextSpan(innerAci, g2d, eSVG, startPosition, linePosition);
									}

									// we need to start a new line
									charStart = charEnd;
									linePosition = p.getY();
									startPosition = p.getX();
								}

								charEnd += charCount;
								//eSVG.setTextBounds(gv.getLogicalBounds().createUnion(eSVG.getTextBounds()));
							}
						}

						if (charEnd - charStart > 0) {
							AttributedCharacterIterator innerAci = new AttributedCharacterSpanIterator(aci, charStart, charEnd);
							outputTextSpan(innerAci, g2d, eSVG, startPosition, linePosition);
						}
						
						g2d.setPaint(basePaint);
						g2d.setFont(baseFont);
					} else {
						super.draw(g2d);
					}
				}

				private void outputTextSpan(AttributedCharacterIterator aci, Graphics2D g2d, ExtendedSVGGraphics2D eSVG, double x, double y) {
					TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
					@SuppressWarnings("unchecked")
					List<GVTFontFamily> gvtFontFamilies = (List<GVTFontFamily>) aci.getAttribute(GVT_FONT_FAMILIES);

					if (tpi == null)
						return;
					if (!tpi.visible)
						return;

					Paint fillPaint = tpi.fillPaint;

					if (fillPaint != null) {
						Font toUse = eSVG.handleGVTFontFamilies(gvtFontFamilies);
						g2d.setFont(toUse);
						g2d.setPaint(fillPaint);
						g2d.drawString(aci, (float) x, (float) y);
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

			/**
			 * Since the flowRoot will always render considering line height, 
			 * we should use that here.
			 */
			@SuppressWarnings("unchecked")
			@Override
			public Rectangle2D getBounds2D(TextNode node) {
				Rectangle2D out = super.getBounds2D(node);
				
				List<TextRun> textRuns = node.getTextRuns();
				double minY = 0;
				double maxY = 0;
				for (TextRun textRun : textRuns) {
					TextSpanLayout tsl = textRun.getLayout();
					if (!tsl.isVertical()) {
						Point2D startPoint = textRun.getLayout().getOffset();
						GVTLineMetrics glm = tsl.getLineMetrics();
						float height = glm.getHeight();
						maxY = Math.max(maxY, startPoint.getY() + height);
						minY = Math.min(minY, startPoint.getY());
					}
				}
				out.add(new Point2D.Double(out.getMinX(), maxY));
				out.add(new Point2D.Double(out.getMinX(), minY));
				return out;
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
