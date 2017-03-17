package org.kite9.diagram.style;

public interface IntegerRange {

	int getFrom();

	int getTo();

	public static boolean notSet(IntegerRange in) {
		return (in == null) || (in.getFrom() == -1);
	}
}