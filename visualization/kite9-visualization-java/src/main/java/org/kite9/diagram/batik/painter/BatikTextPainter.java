package org.kite9.diagram.batik.painter;

import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.processors.TextWrapProcessor;
import org.kite9.diagram.model.position.Rectangle2D;
import org.w3c.dom.Element;

public class BatikTextPainter extends BatikLeafPainter {

    public BatikTextPainter(Element theElement, ElementContext ctx) {
        super(theElement, ctx);
    }

    /**
     * This works as long as we don't go messing with the baseline of the text - default only will do.
     *
     * @return
     */
    @Override
    public Rectangle2D bounds() {
        getGraphicsNode();
        double lineHeight = TextWrapProcessor.Companion.calculateLineHeight(getTheElement(), ctx);

        Rectangle2D ir = super.bounds();
        if (ir == null) {
            return null;
        }
        double height = ir.getHeight();
        double units = Math.ceil(height / lineHeight);
        double newHeight = units * lineHeight;
        double ascent = 0 - ctx.getCssStyleDoubleProperty("font-size", getTheElement());
        return new Rectangle2D(ir.getX(), ascent, ir.getWidth(), newHeight);
    }
}
