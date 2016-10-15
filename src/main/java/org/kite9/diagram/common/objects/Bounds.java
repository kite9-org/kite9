package org.kite9.diagram.common.objects;

import org.apache.commons.math.fraction.BigFraction;

public interface Bounds extends Comparable<Bounds> {

	public abstract double getDistanceMin();

	public abstract double getDistanceMax();

	public abstract double getDistanceCenter();
	
	/** 
	 * Returns a new bounds large enough to take both this and other.
	 */
	public Bounds expand(Bounds other);
	
	/**
	 * Returns a new bounds with just the common distance inside
	 */
	public Bounds narrow(Bounds other);
	
	public Bounds keep(double buffer, double width, BigFraction atFraction);
	
	public Bounds keep(double buffer, double width, double atFraction);
	
//	public Bounds keepMax(double lb, double ub);
//	
//	public Bounds keepMin(double ub, double lb);
//	
//	public Bounds keepMid(double w);

}