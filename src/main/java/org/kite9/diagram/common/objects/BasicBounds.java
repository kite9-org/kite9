package org.kite9.diagram.common.objects;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.framework.logging.LogicException;

public class BasicBounds implements Bounds {
	
	public static final BasicBounds EMPTY_BOUNDS = new BasicBounds(-1, -1);

	
	private double min, max;

	@Override
	public Bounds expand(Bounds other) {
		if (this == EMPTY_BOUNDS) {
			return other;
		} else if (other == EMPTY_BOUNDS) {
			return this;
		}
		BasicBounds o2 = (BasicBounds) other;
		//System.out.println("merging "+o2+" with "+this);
		return new BasicBounds(Math.min(o2.getDistanceMin(), this.getDistanceMin()), 
				Math.max(o2.getDistanceMax(), this.getDistanceMax()));
	}
	
	@Override
	public Bounds narrow(Bounds other) {
		if (this == EMPTY_BOUNDS) {
			return this;
		} else if (other == EMPTY_BOUNDS) {
			return other;
		}
		BasicBounds o2 = (BasicBounds) other;
		double lower = Math.max(o2.getDistanceMin(), this.getDistanceMin());
		double upper = Math.min(o2.getDistanceMax(), this.getDistanceMax());

		if (lower >= upper) {
			return EMPTY_BOUNDS;
		}

		return new BasicBounds(lower, upper);
	}
	

	@Override
	public double getDistanceMin() {
		return min;
	}

	@Override
	public double getDistanceMax() {
		return max;
	}
	
	@Override
	public double getDistanceCenter() {
		return (getDistanceMax()+getDistanceMin())/2d;
	}

	public BasicBounds(double min, double max) {
		super();
		this.min = min;
		this.max = max;
		if (min > max) {
			throw new LogicException("Illegal Bounds");
		}
	}

	public static final NumberFormat nf = new DecimalFormat(".0000");

	public String toString() {
		return "(bb, g="+
				nf.format(getDistanceMin())+
				"-"+
				nf.format(getDistanceMax())+")";
	}

	/**
	 * As well as following the usual -1, 0, 1 compare operation, this also returns the common level of the two bounds 
	 * being compared.
	 */
	@Override
	public int compareTo(Bounds s) {
		if (getDistanceMax() < s.getDistanceMin()) {
			return -1;
		} else if (getDistanceMin() > s.getDistanceMax()) {
			return 1;
		} else {
			return 0;
		}
	}


	@Override
	public Bounds keep(double buffer, double width, BigFraction atFraction) {
		double span = max - min - (buffer * 2d);
		double pos = atFraction.doubleValue() * span;
		double lower = min + pos - (width / 2d) + buffer;
		double upper = min + pos + (width / 2d) + buffer;
		lower = Math.max(min+buffer, lower);
		upper = Math.min(max-buffer, upper);
		return new BasicBounds(lower, upper);
	}
	
	@Override
	public Bounds keep(double buffer, double width, double fraction) {
		double span = max - min - (buffer * 2d);
		double pos = fraction * span;
		double lower = min + pos - (width / 2d) + buffer;
		double upper = min + pos + (width / 2d) + buffer;
		lower = Math.max(min+buffer, lower);
		upper = Math.min(max-buffer, upper);
		return new BasicBounds(lower, upper);
	}

//	@Override
//	public Bounds keepMax(double lb, double ub) {
//		if ((lb == 0) && (ub == 0)) {
//			return this;
//		}
//		return new BasicBounds(this.max - ub, this.max - lb);
//	}
//
//
//	@Override
//	public Bounds keepMin(double lb, double ub) {
//		if ((lb == 0) && (ub == 0)) {
//			return this;
//		}
//		return new BasicBounds(this.min+lb ,this.min+ub);
//	}
//	
//
//	@Override
//	public Bounds keepMid(double w) {
//		double mid = (this.min + this.max) / 2d;
//		if ((mid <0) || (mid > 1)) {
//			return BasicBounds.EMPTY_BOUNDS;
//		}
//		return new BasicBounds(mid-(w/2),mid+(w/2));
//	}

}
