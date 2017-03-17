package org.kite9.framework.common;

public class StateNotMatchingException extends RuntimeException {

	private static final long serialVersionUID = 3060867066393532823L;

	public StateNotMatchingException(Enum<?> currentValue, Object values) {
		super("Was expecting "+currentValue+" but had "+values);
	}
	
}
