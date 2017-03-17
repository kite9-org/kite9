package org.kite9.diagram.position;

/**
 * Stores value for each of the four compass points.
 * 
 * @author robmoffat
 */
public class DirectionalValues {
	
	public static final DirectionalValues ZERO = new DirectionalValues(0, 0, 0, 0);

	public double getTop() {
		return top;
	}

	public double getRight() {
		return right;
	}

	public double getBottom() {
		return bottom;
	}

	public double getLeft() {
		return left;
	}

	double top;
	double right;
	double bottom;
	double left;
	
	public DirectionalValues(double up, double right, double down, double left) {
		this.top = up;
		this.bottom = down;
		this.left = left;
		this.right = right;
	}
	
	public DirectionalValues(double[] vals) {
		this.top = vals[Direction.UP.ordinal()];
		this.bottom = vals[Direction.DOWN.ordinal()];
		this.left = vals[Direction.LEFT.ordinal()];
		this.right = vals[Direction.RIGHT.ordinal()];
	}

	/**
	 * Helper method to increase padding
	 */
	public DirectionalValues add(DirectionalValues internalPadding) {
		return new DirectionalValues(
				internalPadding.top + this.top,
				internalPadding.right+this.right,
				internalPadding.bottom + this.bottom,
				internalPadding.left + this.left
				);
		
	}

	public double get(Direction d) {
		switch (d) {
		case UP:
			return top;
		case DOWN:
			return bottom;
		case LEFT:
			return left;
		case RIGHT:
			return right;
		}
		
		return 0;
	}

	public DirectionalValues add(double trimForStroke) {
		return new DirectionalValues(
				this.top + trimForStroke,
				this.right+trimForStroke,
				this.bottom + trimForStroke,
				this.left + trimForStroke
				);
	}
}
