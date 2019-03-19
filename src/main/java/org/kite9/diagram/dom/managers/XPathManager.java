package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Used for creating a CSS property that reflects an enum in Java.
 */
public class XPathManager extends AbstractValueManager {

	private final String propertyName;
	private final Value defaultValue;
	private final boolean inherited;

	public XPathManager(String propertyName, String defaultValue, boolean inherited) {
		this.propertyName = propertyName;
		this.defaultValue = createStringValue(defaultValue);
		this.inherited = inherited;
	}

	public static final StringValue createStringValue(String e) {
		return new StringValue(CSSPrimitiveValue.CSS_STRING, e);
	}

	public boolean isInheritedProperty() {
		return inherited;
	}

	public boolean isAnimatableProperty() {
		return false;
	}

	public boolean isAdditiveProperty() {
		return false;
	}

	public int getPropertyType() {
		return SVGTypes.TYPE_IDENT;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Value getDefaultValue() {
		return defaultValue;
	}

	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		return value;
	}
	

    public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return ValueConstants.INHERIT_VALUE;

        case LexicalUnit.SAC_IDENT:
            String s = lu.getStringValue().toLowerCase();            
            return createStringValue(s);

        default:
            throw createInvalidLexicalUnitDOMException
                (lu.getLexicalUnitType());
        }
    }

    /**
     * Implements {@link
     * ValueManager#createStringValue(short,String,CSSEngine)}.
     */
    public Value createStringValue(short type, String value, CSSEngine engine)
        throws DOMException {
        
    	if (type != CSSPrimitiveValue.CSS_IDENT) {
            throw createInvalidStringTypeDOMException(type);
        }
        
        return createStringValue(value);
    }


}
