package org.kite9.diagram.visualization.planarization.rhd.position;

import java.awt.geom.Rectangle2D;

public class Tools {


	public static boolean intersects(Rectangle2D container, Rectangle2D to) {
		return axisIntersect(container.getMinX(), container.getMaxX(), to.getMinX(), to.getMaxX()) &&
			axisIntersect(container.getMinY(), container.getMaxY(), to.getMinY(), to.getMaxY());
	}

	public static boolean intersectsX(Rectangle2D container, double x) {
		return pointIntersect(x,container.getMinX(), container.getMaxX());
	}
	
	private static boolean axisIntersect(double amin, double amax, double bmin, double bmax) {
		return pointIntersect(amin, bmin, bmax) || 
				pointIntersect(bmin, amin, amax) || 
				pointIntersect(amax, bmin, bmax) || 
				pointIntersect(bmax, amin, amax);
	}
	
	private static boolean pointIntersect(double v, double min, double max) {
		return (min <= v) && (max>=v);
	}
	

	public static boolean contains(Rectangle2D container, Rectangle2D item) {
		return pointIntersect(item.getMinX(), container.getMinX(), container.getMaxX()) &&
				pointIntersect(item.getMaxX(), container.getMinX(), container.getMaxX()) &&
				pointIntersect(item.getMinY(), container.getMinY(), container.getMaxY()) &&
				pointIntersect(item.getMaxY(), container.getMinY(), container.getMaxY());
	}
	
	public static boolean contains(double amin, double amax, double bmin, double bmax) {
		return pointIntersect(amin, bmin, bmax) &&
				pointIntersect(amax, bmin, bmax);
	}

	
}
