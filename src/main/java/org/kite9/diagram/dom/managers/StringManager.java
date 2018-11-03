package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

public class StringManager extends AbstractValueManager {
	
	private final String propertyName;
	private final Value defaultValue;
	private final boolean inherited;

	public StringManager(String propertyName, Value defaultValue, boolean inherited) {
		this.propertyName = propertyName;
		this.defaultValue = defaultValue;
		this.inherited = inherited;
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
	public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
		return new StringValue(CSSPrimitiveValue.CSS_STRING, lu.getStringValue());
	}
	
	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		return value;
	}
}
