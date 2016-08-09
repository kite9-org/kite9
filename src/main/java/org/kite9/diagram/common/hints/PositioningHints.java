package org.kite9.diagram.common.hints;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.adl.PositionableDiagramElement;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;

/**
 * Hints are attached to {@link PositionableDiagramElement}s.  These allow the grouping and layout 
 * to proceed along lines already established by a previous rendering.
 * 
 * @author robmoffat
 *
 */
public class PositioningHints {
	
	enum Approach { MAX, MIN;
	
	public Float merge(Float a, Float b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		} else if (this==Approach.MAX) {
			return Math.max(a,b);
		} else if (this==Approach.MIN) {
			return Math.min(a, b);
		}
		
		return null;
	}}
	
	public static final String PLAN_MIN_X = "px1";
	public static final String PLAN_MAX_X = "px2";
	public static final String PLAN_MIN_Y = "py1";
	public static final String PLAN_MAX_Y = "py2";

	public static final String MIN_X = "x1";
	public static final String MAX_X = "x2";
	public static final String MIN_Y = "y1";
	public static final String MAX_Y = "y2";
	
	public static Map<String, Approach> PLANARIZATION_HINTS = new HashMap<String, Approach>();
	public static Map<String, Approach> POSITION_HINTS = new HashMap<String, Approach>();
	public static Map<String, Approach> ALL_HINTS = new HashMap<String, Approach>();
	
	static {
		PLANARIZATION_HINTS.put(PLAN_MIN_X, Approach.MIN);
		PLANARIZATION_HINTS.put(PLAN_MIN_Y, Approach.MIN);
		PLANARIZATION_HINTS.put(PLAN_MAX_X, Approach.MAX);
		PLANARIZATION_HINTS.put(PLAN_MAX_Y, Approach.MAX);
		POSITION_HINTS.put(MIN_X, Approach.MIN);
		POSITION_HINTS.put(MIN_Y, Approach.MIN);
		POSITION_HINTS.put(MAX_X, Approach.MAX);
		POSITION_HINTS.put(MAX_Y, Approach.MAX);
		
		ALL_HINTS.putAll(PLANARIZATION_HINTS);
		ALL_HINTS.putAll(POSITION_HINTS);
		
	}
	
	public static Map<String, Float> merge(Map<String, Float> a, Map<String, Float> b) {
		if ((a.size() == 0) && (b.size() == 0)) {
			return Collections.emptyMap();
		}
		
		Map<String, Float> out = new HashMap<String, Float>();
		for (String k : ALL_HINTS.keySet()) {
			Approach v = ALL_HINTS.get(k);
			out.put(k, v.merge(a.get(k), b.get(k)));
		}
		
		return out;
	}
	
	public static void writeHints(Map<String, Float> from, Map<String, Float> to, Map<String, Approach> hints) {
		for (String h : hints.keySet()) {
			Float v = from.get(h);
			if (v != null) {
				to.put(h, v);
			}
		}
	}

	public static Integer compareEitherXBounds(Map<String, Float> from, Map<String, Float> to) {
		return compareEitherBounds(from, to, PLAN_MIN_X, PLAN_MAX_X, MIN_X, MAX_X);
	}
	
	public static Integer compareEitherYBounds(Map<String, Float> from, Map<String, Float> to) {
		return compareEitherBounds(from, to, PLAN_MIN_Y, PLAN_MAX_Y, MIN_Y, MAX_Y);
	}
	
	private static Integer compareEitherBounds(Map<String, Float> from, Map<String, Float> to, String p1, String p2, String a1, String a2) {
		Integer bc = compareBounds(from, to, p1, p2);
		if ((bc == null) || (bc == 0)) {
			bc = compareBounds(from, to, a1, a2);
		}
		return bc;
	}
	
	private static Integer compareBounds(Map<String, Float> from, Map<String, Float> to, String p1, String p2) {
		Bounds fb = createBounds(from, p1, p2);
		Bounds tb = createBounds(to, p1, p2);
		if ((fb == null) || (tb == null)) {
			return null;
		} else {
			return fb.compareTo(tb);
		}
	}

	public static Float planarizationDistance(Map<String, Float> a, Map<String, Float> b) {
		Float xD = scalarDistance(a, b, "px1", "px2");
		Float yD = scalarDistance(a, b, "py1", "py2");
		if ((xD == null) || (yD==null)) {
			return null;
		}
		return xD+yD;
	}
	

	public static Float positionDistance(Map<String, Float> a, Map<String, Float> b) {
		Float xD = scalarDistance(a, b, "x1", "x2");
		Float yD = scalarDistance(a, b, "y1", "y2");
		if ((xD == null) || (yD==null)) {
			return null;
		}
		return xD+yD;
	}
	
	public static Float scalarDistance(Map<String, Float> a, Map<String, Float> b, String b1, String b2) {
		Bounds aBounds = createBounds(a, b1, b2);
		Bounds bBounds = createBounds(b, b1, b2);
		return boundsDistance(aBounds, bBounds);
	}

	private static Float boundsDistance(Bounds aBounds, Bounds bBounds) {
		if ((aBounds == null) || (bBounds == null)) {
			return null;
		}
		if (aBounds.getDistanceMax() < bBounds.getDistanceMin()) {
			return (float) (bBounds.getDistanceMin() - aBounds.getDistanceMax());
		} else if (aBounds.getDistanceMin() > bBounds.getDistanceMax()) {
			return (float) (aBounds.getDistanceMin() - bBounds.getDistanceMax());
		} else {
			return 0f;
		}
	}

	private static Bounds createBounds(Map<String, Float> a, String b1, String b2) {
		Float min = a.get(b1);
		Float max = a.get(b2);
		if ((min==null) || (max==null)) {
			return null;
		}
		return new BasicBounds(min, max);
	}
	
	public static void planFill(Map<String, Float> map, float px1, float px2, float py1, float py2) {
		map.put(PLAN_MIN_X, px1);
		map.put(PLAN_MIN_Y, py1);
		map.put(PLAN_MAX_X, px2);
		map.put(PLAN_MAX_Y, py2);
		
	}
}
