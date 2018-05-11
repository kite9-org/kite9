package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg12.MarginLengthManager;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class ConnectionAlignmentLengthManager extends MarginLengthManager {

	public ConnectionAlignmentLengthManager(String prop) {
		super(prop);
	}

    public int getPropertyType() {
        return SVGTypes.TYPE_NUMBER_OR_PERCENTAGE;
    }

	@Override
	public Value getDefaultValue() {
		return new StringValue(CSSPrimitiveValue.CSS_IDENT,
                org.apache.batik.util.CSSConstants.CSS_NONE_VALUE);
	}
    
	public boolean isInheritedProperty() {
		return false;
	}
	
	/**
	 * Preserves percentages
	 */
	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return value;
		}

		if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
			return value;
		}

		return super.computeValue(elt, pseudo, engine, idx, sm, value);

	}
	
	
    
}
