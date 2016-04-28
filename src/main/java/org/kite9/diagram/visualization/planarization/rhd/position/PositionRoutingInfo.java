package org.kite9.diagram.visualization.planarization.rhd.position;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.kite9.diagram.common.elements.RoutingInfo;

public abstract class PositionRoutingInfo implements RoutingInfo {
	
	public static final NumberFormat nf = new DecimalFormat(".0000");

	public abstract double getMinX();
	public abstract double getMaxX();
	public abstract double getMinY();
	public abstract double getMaxY();
	public abstract double getWidth();
	public abstract double getHeight();
	public abstract boolean isBreakingOrder();
	
	@Override
	public String outputX() {
		return nf.format(getMinX())+"-"+nf.format(getMaxX());
	}
	
	@Override
	public String outputY() {
		return nf.format(getMinY())+"-"+nf.format(getMaxY());
	}
	
	public String toString() {
		return outputX() + ", "+ outputY();
	}
	
}