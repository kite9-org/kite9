package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.value.AbstractValueManager;
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
public class PortPlacementManager extends MarginLengthManager {

	public PortPlacementManager(String prop) {
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

}
