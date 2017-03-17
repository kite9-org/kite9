package org.kite9.framework.serialization;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Used to manage grid-rows and grid-columns;
 * 
 * @author robmoffat
 *
 */
public class GridSizeManager extends LengthManager {

	private String propertyName;
	
	public GridSizeManager(String propertyName) {
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
		return CSSPrimitiveValue.CSS_NUMBER;
	}

	@Override
	public Value getDefaultValue() {
		return new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 1);
	}

	@Override
	protected int getOrientation() {
		return 0;
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
