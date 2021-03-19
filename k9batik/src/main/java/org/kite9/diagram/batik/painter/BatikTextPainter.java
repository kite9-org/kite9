package org.kite9.diagram.batik.painter;

import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.model.position.Rectangle2D;
import org.w3c.dom.Element;

public class BatikTextPainter extends BatikLeafPainter {

    public BatikTextPainter(Element theElement, ElementContext ctx) {
        super(theElement, ctx);
    }

    @Override
    public Rectangle2D bounds() {
        double lineHeight = ctx.getCssStyleDoubleProperty("line-height", getTheElement());
        Rectangle2D ir = super.bounds();
        if (ir == null) {
            return null;
        }
        double height = ir.getHeight();
        double units = Math.ceil(height / lineHeight);
        height = units * lineHeight;
        return new Rectangle2D(ir.getX(), ir.getY(), ir.getWidth(), height);
    }
}
