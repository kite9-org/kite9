package org.kite9.framework.serialization;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * Used for creating a CSS property that reflects an enum in Java.
 */
public class IntegerRangeManager extends AbstractValueManager {
	
	public static final IntegerRangeValue NOT_SET = new IntegerRangeValue(-1, -2);

    private final String propertyName;
	
	public IntegerRangeManager(String propertyName) {
		super();
		this.propertyName = propertyName;
	}

	@Override
	public boolean isInheritedProperty() {
		return false;
	}

	@Override
	public boolean isAnimatableProperty() {
		return false;
	}

	@Override
	public boolean isAdditiveProperty() {
		return false;
	}

	@Override
	public int getPropertyType() {
		return SVGTypes.TYPE_NUMBER_LIST;
	}

	@Override
	public Value getDefaultValue() {
		return NOT_SET;
	}

	@Override
	public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_INTEGER) {
			int start = lu.getIntegerValue();
			int end = start;
			if (lu.getNextLexicalUnit() != null) {
				end = lu.getNextLexicalUnit().getIntegerValue();
			}
			if ((start<=end) && (start>=0)) {
				return new IntegerRangeValue(start, end);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		return value;
	}
}
