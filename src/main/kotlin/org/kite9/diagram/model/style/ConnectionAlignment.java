package org.kite9.diagram.model.style;

public class ConnectionAlignment {

	public static enum Measurement {
		NONE, PERCENTAGE, PIXELS
	}
	
	private final Measurement type;
	private final double amount;
	
	public Measurement getType() {
		return type;
	}

	public double getAmount() {
		return amount;
	}

	public ConnectionAlignment(Measurement type, double amount) {
		super();
		this.type = type;
		this.amount = amount;
	}
	
	public static final ConnectionAlignment NONE = new ConnectionAlignment(Measurement.NONE, 0);
}
