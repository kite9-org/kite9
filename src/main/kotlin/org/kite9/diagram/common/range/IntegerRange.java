package org.kite9.diagram.common.range;

public interface IntegerRange {

	int getFrom();

	int getTo();

	public static boolean notSet(IntegerRange in) {
		return (in == null) || (in.getFrom() > in.getTo());
	}
}