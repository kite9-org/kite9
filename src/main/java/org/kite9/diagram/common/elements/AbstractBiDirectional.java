package org.kite9.diagram.common.elements;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.logging.LogicException;

public abstract class AbstractBiDirectional<X> implements BiDirectional<X> {

	/**
	 * For serialization
	 */
	public AbstractBiDirectional() {
	}

	protected X from, to;
	private String id;
	
	
	public final String getID() {
		return id;
	}
	
	public void setID(String id) {
		this.id = id;
	}
	
	public X getFrom() {
		return from;
	}

	public void setFrom(X from) {
		this.from = from;
	}

	public X getTo() {
		return to;
	}

	public void setTo(X to) {
		this.to = to;
	}

	private static int counter = 0; 

	protected static synchronized String createID() {
		return AUTO_GENERATED_ID_PREFIX+counter++;
	}
	
	public static final String AUTO_GENERATED_ID_PREFIX = "auto:";

	public AbstractBiDirectional(X from, X to, Direction drawDirection) {
		this.id = createID();
		setFrom(from);
		setTo(to);
		setDrawDirection(drawDirection);
	}

	@Override
	public String toString() {
		return "[" + getFrom() + "-" + getTo() + "]";
	}

	public X otherEnd(X end) {
		if (end == getFrom())
			return getTo();
		if (end == getTo())
			return getFrom();
		throw new LogicException("This is not an end: " + end + " of " + this);
	}

	public boolean meets(BiDirectional<X> e) {
		return getFrom().equals(e.getTo()) || getFrom().equals(e.getFrom())
				|| getTo().equals(e.getTo()) || getTo().equals(e.getFrom());
	}

	public boolean meets(X v) {
		return getFrom().equals(v) || getTo().equals(v);
	}

	public Direction getDrawDirectionFrom(X end) {
		if (getDrawDirection() == null)
			return null;

		if (end.equals(getFrom())) {
			return getDrawDirection();
		}

		if (end.equals(getTo())) {
			return Direction.reverse(getDrawDirection());
		}

		throw new RuntimeException(
				"Trying to get direction from an end that's not set: " + end
						+ " in " + this);
	}

	protected Direction drawDirection;
	
	public void setDrawDirection(Direction d) {
		this.drawDirection = d;
	}

	public void setDrawDirectionFrom(Direction d, X end) {

		if (end.equals(getFrom())) {
			setDrawDirection(d);
		} else if (end.equals(getTo())) {
			setDrawDirection(Direction.reverse(d));
		} else {
			throw new RuntimeException(
					"Trying to set direction from an end that's not set: " + end
							+ " in " + this);
		}
	}

	public Direction getDrawDirection() {
		return drawDirection;
	}
	
	
}