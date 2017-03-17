package org.kite9.framework.serialization;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.SVG12CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * This class represents an object which provide support for the
 * 'margin' shorthand property.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: MarginShorthandManager.java 475685 2006-11-16 11:16:05Z cam $
 */
public class PaddingAndMarginShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {

	private final String propertyPrefix;
	
    public PaddingAndMarginShorthandManager(String pp) {
    	this.propertyPrefix = pp;
    }
    
    /**
     * Implements {@link ValueManager#getPropertyName()}.
     */
    public String getPropertyName() {
        return propertyPrefix;
    }
    
    /**
     * Implements {@link ShorthandManager#isAnimatableProperty()}.
     */
    public boolean isAnimatableProperty() {
        return true;
    }

    /**
     * Implements {@link ShorthandManager#isAdditiveProperty()}.
     */
    public boolean isAdditiveProperty() {
        return false;
    }

    /**
     * Implements {@link ShorthandManager#setValues(CSSEngine,ShorthandManager.PropertyHandler,LexicalUnit,boolean)}.
     */
    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {
        if (lu.getLexicalUnitType() == LexicalUnit.SAC_INHERIT)
            return;

        LexicalUnit []lus  = new LexicalUnit[4];
        int cnt=0;
        while (lu != null) {
            if (cnt == 4)
                throw createInvalidLexicalUnitDOMException
                    (lu.getLexicalUnitType());
            lus[cnt++] = lu;
            lu = lu.getNextLexicalUnit();
        }
        switch (cnt) {
        case 1: lus[3] = lus[2] = lus[1] = lus[0]; break;
        case 2: lus[2] = lus[0];  lus[3] = lus[1]; break;
        case 3: lus[3] = lus[1]; break;
        default:
        }

        ph.property(propertyPrefix+"-top",    lus[0], imp);
        ph.property(propertyPrefix+"-right",  lus[1], imp);
        ph.property(propertyPrefix+"-bottom", lus[2], imp);
        ph.property(propertyPrefix+"-left",   lus[3], imp);
    }
}
