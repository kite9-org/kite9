package org.kite9.framework.serialization;

import org.apache.batik.css.engine.value.svg12.MarginLengthManager;

/**
 * We don't want padding inherited between elements, so this simply prevents that from happening.
 * 
 * @author robmoffat
 *
 */
public class PaddingLengthManager extends MarginLengthManager {

	public PaddingLengthManager(String prop) {
		super(prop);
	}

	@Override
	public boolean isInheritedProperty() {
		return false;
	}

}
