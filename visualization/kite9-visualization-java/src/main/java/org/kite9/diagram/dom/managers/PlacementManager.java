package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg12.MarginLengthManager;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * Re-usable manager for link length css attributes.
 * 
 * @author robmoffat
 *
 */
public class PlacementManager extends MarginLengthManager {

	public PlacementManager(String prop) {
		super(prop);
	}

	@Override
	public boolean isInheritedProperty() {
		return false;
	}

	@Override
	public Value getDefaultValue() {
		return new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 50F);
	}

	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return value;
		}

		if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
			// we have to leave percentages as-is, since you cannot know the
			// pixel size until the parent element size is computed.
			return value;
		}

		return super.computeValue(elt, pseudo, engine, idx, sm, value);
	}
}
