package org.kite9.framework.common;


public class StackSearchException extends RuntimeException {

	public StackSearchException(Class<?> ann) {
		super("Could not find annotated method in stack: "+ann);
	}

	private static final long serialVersionUID = -4653211618062874387L;

}
