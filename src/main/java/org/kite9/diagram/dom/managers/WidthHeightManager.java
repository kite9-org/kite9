package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg12.MarginLengthManager;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Re-usable manager for size (width, css) css attributes.
 * 
 * @author robmoffat
 *
 */
public class WidthHeightManager extends MarginLengthManager {
	
	private final float def;
	private final boolean inherited;

	public WidthHeightManager(String prop, float d, boolean inherited) {
		super(prop);
		this.def = d;
		this.inherited = inherited;
	}

	@Override
	public boolean isInheritedProperty() {
		return inherited;
	}

	@Override
	public Value getDefaultValue() {
		return new FloatValue(CSSPrimitiveValue.CSS_PX, def);
	}

	@Override
	public boolean isAdditiveProperty() {
		return false;
	}

}
