package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg12.SVG12ValueConstants;
import org.apache.batik.css.engine.value.svg12.TextAlignManager;
import org.apache.batik.util.SVG12CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Overrides the SVG1.2 with a HTML-compatible "text-align"
 * supporting left, right, center.
 */
public class TextAlignManager2 extends TextAlignManager {

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String CENTER = "center";
    private static final String JUSTIFY = "justify";


    private static final Value LEFT_VALUE  = new StringValue(CSSPrimitiveValue.CSS_IDENT, "left");
    private static final Value CENTER_VALUE = new StringValue(CSSPrimitiveValue.CSS_IDENT,"center");
    private static final Value RIGHT_VALUE    = new StringValue(CSSPrimitiveValue.CSS_IDENT, "right");
    private static final Value JUSTIFY_VALUE    = new StringValue(CSSPrimitiveValue.CSS_IDENT, "justify");

    static {
        values.put(LEFT, LEFT_VALUE);
        values.put(RIGHT, RIGHT_VALUE);
        values.put(CENTER, CENTER_VALUE);
        values.put(JUSTIFY, JUSTIFY_VALUE);
    }

    @Override
    public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
        Value out = super.createValue(lu, engine);
        return convert(out);
    }

    private Value convert(Value out) {
        switch (out.getStringValue()) {
            case LEFT:
                return SVG12ValueConstants.START_VALUE;
            case RIGHT:
                return SVG12ValueConstants.END_VALUE;
            case CENTER:
                return SVG12ValueConstants.MIDDLE_VALUE;
            case JUSTIFY:
                return SVG12ValueConstants.FULL_VALUE;
        }

        return out;
    }

    @Override
    public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
        Value out = super.createStringValue(type, value, engine);
        return convert(out);
    }
}
