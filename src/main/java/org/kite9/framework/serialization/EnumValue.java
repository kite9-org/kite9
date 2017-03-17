package org.kite9.framework.serialization;

import org.apache.batik.css.engine.value.AbstractValue;

public class EnumValue extends AbstractValue {
	
	private Enum<?> theValue;
	
	public EnumValue(Enum<?> x) {
		this.theValue = x;
	}

	@Override
	public String getCssText() {
		return EnumManager.cssValueFor(theValue.toString());
	}
	
	public Enum<?> getTheValue() {
		return theValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EnumValue) {
			return this.theValue == ((EnumValue)obj).getTheValue();
		} else {
			return false;
		}
	}
	
}
