package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg12.MarginLengthManager;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Re-usable manager for link length css attributes.
 * 
 * @author robmoffat
 *
 */
public class LinkLengthManager extends MarginLengthManager {
	
	private final float def;

	public LinkLengthManager(String prop, float d) {
		super(prop);
		this.def = d;
	}

	@Override
	public boolean isInheritedProperty() {
		return true;
	}

	@Override
	public Value getDefaultValue() {
		return new FloatValue(CSSPrimitiveValue.CSS_PX, def);
	}

}
