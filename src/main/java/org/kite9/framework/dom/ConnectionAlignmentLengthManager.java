package org.kite9.framework.dom;

import org.apache.batik.css.engine.value.svg12.MarginLengthManager;
import org.apache.batik.util.SVGTypes;

public class ConnectionAlignmentLengthManager extends MarginLengthManager {

	public ConnectionAlignmentLengthManager(String prop) {
		super(prop);
	}

    public int getPropertyType() {
        return SVGTypes.TYPE_NUMBER_OR_PERCENTAGE;
    }
}
