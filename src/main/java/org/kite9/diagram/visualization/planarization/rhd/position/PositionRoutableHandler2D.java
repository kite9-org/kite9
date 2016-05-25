package org.kite9.diagram.visualization.planarization.rhd.position;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.Routable;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.hints.PositioningHints;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.HintMap;
import org.kite9.diagram.visualization.planarization.mgt.router.LineRoutingInfo;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Implementation of the {@link RoutableReader} functionality, but using 2D RoutableHandler Bounds as the underlying storage.
 * 
 * @author robmoffat
 *
 */
public class PositionRoutableHandler2D extends AbstractPositionRoutableReader implements RoutableHandler2D, Logable {

	public static final BasicBounds TOP = new BasicBounds(0, 1);

	
	
	protected Map<Object, Bounds> tempx = new HashMap<Object, Bounds>(1000);
	protected Map<Object, Bounds> placedx = new HashMap<Object, Bounds>(1000);
	protected Map<Object, Bounds> tempy = new HashMap<Object, Bounds>(1000);
	protected Map<Object, Bounds> placedy = new HashMap<Object, Bounds>(1000);
	
	Kite9Log log = new Kite9Log(this);
	
	public PositionRoutableHandler2D() {
		super();
	}

	@Override
	public RoutingInfo getPlacedPosition(Object r) {
		Bounds x = placedx.get(r);
		Bounds y = placedy.get(r);
		if ((x == null) || (y==null)) {
			return null;
		}
		return createRouting(x, y);
	}

	@Override
	public RoutingInfo createRouting(Bounds x, Bounds y) {
		return new BoundsBasedPositionRoutingInfo(x, y);
	}
	
	@Override
	public void clearTempPositions(boolean horiz) {
		if (horiz) {
			tempx.clear();
		} else {
			tempy.clear();
		}
	}
	
	private static final double THIN_GUTTER = 0.001;
	private static final double THICK_GUTTER = 0.01;
	

	public Bounds narrow(Layout d, Bounds in, boolean horiz, boolean applyGutters) {
		in = in == null ? getTopLevelBounds(horiz) : in;
		Bounds multiplicationFrame = getMultiplicationFrame(d, horiz);
		BasicBounds bbounds = (BasicBounds) in;
		double gx = bbounds.getDistanceMin() + (bbounds.getDistanceMax() - bbounds.getDistanceMin()) * multiplicationFrame.getDistanceMin();
		double gw = (bbounds.getDistanceMax()- bbounds.getDistanceMin()) * (multiplicationFrame.getDistanceMax() - multiplicationFrame.getDistanceMin());
		
		if (applyGutters) {
			boolean thick = isThickGutter(d, horiz);
			double g = gw * (thick ? THICK_GUTTER : THIN_GUTTER);
			gx += g;
			gw -= 2*g;
		}
		
		return new BasicBounds(gx, gx+gw);
		
	}
	
	public static final BasicBounds TOP_HALF = new BasicBounds(0, .5d);
	public static final BasicBounds BOTTOM_HALF = new BasicBounds(.5d, 1);

	
	/**
	 * Given a direction, returns a rectangle with details of how the
	 * coordinates should be multiplied to achieve the new orientation.
	 */
	public Bounds getMultiplicationFrame(Layout d, boolean horiz) {
	
		if (d == null) {
			return TOP;
		}

		switch (d) {
		case LEFT:
			return horiz ? TOP_HALF : TOP;
		case UP:
			return horiz ? TOP : TOP_HALF;
		case RIGHT:
			return horiz ? BOTTOM_HALF : TOP;
		case DOWN:
			return horiz ? TOP: BOTTOM_HALF;
		default:
			return TOP;
		}
	}
	
	/**
	 * Note: "HORIZONTAL" and "VERTICAL" seem to go against the grain. This is deliberate so that if for example, a
	 * container is horizontal, then links to it are placed preferentially above or below it.
	 */
	public boolean isThickGutter(Layout d, boolean horiz) {
		if (d==null) {
			return false;
		}
		
		if (horiz) {
			switch (d) {
			case UP:
			case DOWN:
			case HORIZONTAL:
				return true;
			case VERTICAL:
			case LEFT:
			case RIGHT:
			default:
				return false;
			}
		} else {
			switch (d) {
			case LEFT:
			case RIGHT:
			case VERTICAL:
				return true;
			case HORIZONTAL:
			case UP:
			case DOWN:
			default:
				return false;
			}
		}
	
	}

	@Override
	public Bounds getPosition(Object r, boolean horiz) {
		if (horiz) {
			return placedx.get(r);
		} else {
			return placedy.get(r);
		}
	}

	@Override
	public Bounds getTempPosition(Object r, boolean horiz) {
		if (horiz) {
			return tempx.get(r);
		} else {
			return tempy.get(r);
		}
	}

	@Override
	public Bounds getTopLevelBounds(boolean horiz) {
		return TOP;
	}

	@Override
	public void setPlacedPosition(Object r, Bounds ri, boolean horiz) {
		if (horiz) {
			placedx.put(r, ri);
		} else {
			placedy.put(r, ri);
		}
	}

	@Override
	public void setTempPosition(Object r, Bounds ri, boolean horiz) {
		if (horiz) {
			tempx.put(r, ri);
		} else {
			tempy.put(r, ri);
		}
	}

	
	
	@Override
	public void outputSettings() {
		log.send("x positions: ", placedx);
		log.send("y positions: ", placedy);
	}

	@Override
	public String getPrefix() {
		return "RH2D";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	@Override
	public Bounds getBoundsOf(RoutingInfo ri, boolean horiz) {
		BoundsBasedPositionRoutingInfo pri = (BoundsBasedPositionRoutingInfo) ri;
		if (horiz) {
			return pri.x;
		} else {
			return pri.y;			
		}
	}

	@Override
	public DPos compare(Object a, Object b, boolean horiz) {
		Bounds ab, bb;
		if (horiz) {
			ab = a instanceof BoundsBasedPositionRoutingInfo ? ((BoundsBasedPositionRoutingInfo)a).x : placedx.get(a);
			bb = b instanceof BoundsBasedPositionRoutingInfo ? ((BoundsBasedPositionRoutingInfo)b).x : placedx.get(b);
		} else {
			ab = a instanceof BoundsBasedPositionRoutingInfo ? ((BoundsBasedPositionRoutingInfo)a).y : placedy.get(a);
			bb = b instanceof BoundsBasedPositionRoutingInfo ? ((BoundsBasedPositionRoutingInfo)b).y : placedy.get(b);
		}
		
		return compareBounds(ab, bb);
		
	}

	@Override
	public DPos compareBounds(Bounds ab, Bounds bb) {
		if (ab.getDistanceMax() <= bb.getDistanceMin()) {
			return DPos.BEFORE;
		} else if (ab.getDistanceMin() >= bb.getDistanceMax()) {
			return DPos.AFTER;
		} else {
			return DPos.OVERLAP;
		}
	}

	@Override
	public void setPlacedPosition(Object a, RoutingInfo ri) {
		setPlacedPosition(a, getBoundsOf(ri, true), true);
		setPlacedPosition(a, getBoundsOf(ri, false), false);
	}

	@Override
	public boolean overlaps(Bounds a, Bounds b) {
		double highestMin = Math.max(((BasicBounds)a).getDistanceMin(), ((BasicBounds)b).getDistanceMin());
		double lowestMax = Math.min(((BasicBounds)a).getDistanceMax(), ((BasicBounds)b).getDistanceMax());
		return (highestMin < lowestMax);
	}
	
	@Override
	public double distance(RoutingInfo from, RoutingInfo to, boolean horiz) {
		Bounds bFrom = getBoundsOf(from, horiz);
		Bounds bTo = getBoundsOf(to, horiz);
		return bTo.getDistanceCenter() - bFrom.getDistanceCenter();
	}

	@Override
	public boolean overlaps(RoutingInfo a, RoutingInfo b) {
		boolean horizOverlap = overlaps(getBoundsOf(a, true), getBoundsOf(b, true));
		boolean vertOverlap = overlaps(getBoundsOf(a, false), getBoundsOf(b, false));
		return horizOverlap && vertOverlap;
	}

	@Override
	public int order(RoutingInfo a, RoutingInfo b) {
		Bounds ax = ((BoundsBasedPositionRoutingInfo)a).x;
		Bounds bx = ((BoundsBasedPositionRoutingInfo)b).x;
		Bounds ay = ((BoundsBasedPositionRoutingInfo)a).y;
		Bounds by = ((BoundsBasedPositionRoutingInfo)b).y;
		
		int cx = ax.compareTo(bx);
		int cy = ay.compareTo(by);
		if (cy == 0) {
			//System.out.println("cx = "+cx);
			return cx;
		} else if (cx == 0) {
			//System.out.println("cy = "+cy);
			return cy;
		} else if (Math.abs(cx) < Math.abs(cy)) {
			//System.out.println("cx = "+cx);
			return cx;
		} else {
			//System.out.println("cy = "+cy);
			return cy;
		}
	}
	
	public BufferedImage drawPositions(Collection<? extends Routable> out) {
		double size = out.size() * 40;
		BufferedImage bi = new BufferedImage((int) size+60, (int) size+60, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) size+60, (int) size+60);

		Color[] cols = { Color.GREEN, Color.RED, Color.BLUE, Color.DARK_GRAY };
		int i = 0;

		for (Routable o : out) {
			RoutingInfo ri = o.getRoutingInfo();
			PositionRoutingInfo pri = (PositionRoutingInfo) ri;
			if (pri != null) {
				g.setColor(cols[i % 4]);
				g.setStroke(new BasicStroke(1));
				i++;
				g.drawRoundRect((int) (pri.getMinX() * size+20), (int) (pri.getMinY() * size+20),
						(int) (pri.getWidth() * size), (int) (pri.getHeight() * size), 3, 3);
				g.drawString(o.toString(), (int) (pri.centerX() * size+20), (int) (pri.centerY() * size+20));
			}
		}
		g.dispose();
		return bi;
	}

	@Override
	public boolean isWithin(RoutingInfo area, RoutingInfo pos) {
		Bounds areax = ((BoundsBasedPositionRoutingInfo)area).x;
		Bounds posx = ((BoundsBasedPositionRoutingInfo)pos).x;
		if (!Tools.contains(posx.getDistanceMin(), posx.getDistanceMin(), areax.getDistanceMin(), areax.getDistanceMax())) {
			return false;
		}
		
		Bounds areay = ((BoundsBasedPositionRoutingInfo)area).y;
		Bounds posy = ((BoundsBasedPositionRoutingInfo)pos).y;
		return Tools.contains(posy.getDistanceMin(), posy.getDistanceMax(), areay.getDistanceMin(), areay.getDistanceMax());
	}

	@Override
	public RoutingInfo increaseBounds(RoutingInfo a, RoutingInfo b) {
		if (isEmptyBounds(a)) {
			return b;
		} else if (isEmptyBounds(b)) {
			return a;
		}
		
		BoundsBasedPositionRoutingInfo ba = (BoundsBasedPositionRoutingInfo) a;
		BoundsBasedPositionRoutingInfo bb = (BoundsBasedPositionRoutingInfo) b;
		
		return new BoundsBasedPositionRoutingInfo(ba.x.expand(bb.x), ba.y.expand(bb.y));
	}

	@Override
	public LineRoutingInfo move(LineRoutingInfo current, RoutingInfo past, Routing r) {
		return new LinePositionRoutingInfo((LinePositionRoutingInfo) current, (BoundsBasedPositionRoutingInfo) past, r);
	}
	
	private BoundsBasedPositionRoutingInfo getBoundsInternal(Object o) {
		if (o instanceof BoundsBasedPositionRoutingInfo) {
			return (BoundsBasedPositionRoutingInfo) o;
		} else if (o instanceof Routable) {
			return (BoundsBasedPositionRoutingInfo) ((Routable) o).getRoutingInfo();
		} else {
			return (BoundsBasedPositionRoutingInfo) getPlacedPosition(o);
		}
	}
	
	public static final Map<Routing, Corner> BASIC_AVOIDANCE_CORNERS = new HashMap<RoutableReader.Routing, Corner>();
	
	static {
		BASIC_AVOIDANCE_CORNERS.put(Routing.OVER_BACKWARDS, Corner.TOP_RIGHT);
		BASIC_AVOIDANCE_CORNERS.put(Routing.OVER_FORWARDS, Corner.TOP_RIGHT);
		BASIC_AVOIDANCE_CORNERS.put(Routing.UNDER_FORWARDS, Corner.BOTTOM_LEFT);
		BASIC_AVOIDANCE_CORNERS.put(Routing.UNDER_BACKWARDS, Corner.BOTTOM_LEFT);
	}
	
	/**
	 * Although we have a planarization line (1d) and a set of positions (2d), there is no description of how the planarization
	 * line progresses through 2d space.   So, we make the assumption that it always moves to the right or down as it goes forward.
	 * This assumption appears to be true for all of the test cases we have constructed, but it's not guaranteed by the system, it's only a
	 * result of the grid-based layout that we are using.
	 */
	private static final boolean THROW_ON_ASSUMPTION_FAIL = true;
	
	@Override
	public void initRoutableOrdering(List<? extends Object> items) {
 		for (int i = 0; i < items.size(); i++) {
			BoundsBasedPositionRoutingInfo prev = i==0 ? null : getBoundsInternal(items.get(i-1));
			BoundsBasedPositionRoutingInfo current = getBoundsInternal(items.get(i));
			BoundsBasedPositionRoutingInfo next = (i == items.size()-1) ? null : getBoundsInternal(items.get(i+1));
			
			Map<Routing, Corner> ac = BASIC_AVOIDANCE_CORNERS;
			Object dPrev = getDirectionOfB(prev, current);
			
			if ((dPrev == Direction.UP) || (dPrev == Direction.LEFT)) {
				ac = ensureCopy(ac);
				ac.put(Routing.OVER_BACKWARDS, Corner.BOTTOM_LEFT);
				ac.put(Routing.UNDER_BACKWARDS, Corner.TOP_RIGHT);
				String err = "Assumption not met: "+items.get(i-1)+ " "+items.get(i)+ " "+dPrev;
				log.send(err);
				if (THROW_ON_ASSUMPTION_FAIL) {
					throw new LogicException(err);
				}
			} 
			
			Object dNext = getDirectionOfB(current, next);
			if ((dNext == Direction.UP) || (dNext == Direction.LEFT)) {
				ac = ensureCopy(ac);
				ac.put(Routing.OVER_FORWARDS, Corner.BOTTOM_LEFT);
				ac.put(Routing.UNDER_FORWARDS, Corner.TOP_RIGHT);
				String err = "Assumption not met: "+items.get(i)+ " "+items.get(i+1)+ " "+dNext;
				log.send(err);
				if (THROW_ON_ASSUMPTION_FAIL) {
					throw new LogicException(err);
				}
			}
			
			current.setAvoidanceCorners(ac);
			
		}
	}

	private Map<Routing, Corner> ensureCopy(Map<Routing, Corner> ac) {
		if (ac == BASIC_AVOIDANCE_CORNERS) {
			ac = new HashMap<RoutableReader.Routing, Corner>(6);
			for (Map.Entry<Routing, Corner> e : BASIC_AVOIDANCE_CORNERS.entrySet()) {
				ac.put(e.getKey(), e.getValue());
			}
		}
		
		return ac;
	}
	
	public static final Object OVERLAP = new Object();

	private Object getDirectionOfB(BoundsBasedPositionRoutingInfo a, BoundsBasedPositionRoutingInfo b) {
		if ((a==null) || (b==null)) {
			return null;
		} else if (overlaps(a, b)) {
			return OVERLAP;
		} else if (meq(b.x.getDistanceMin(),a.x.getDistanceMax())) {
			return Direction.RIGHT;
		} else if (meq(b.y.getDistanceMin(),a.y.getDistanceMax())) {
			return Direction.DOWN;
		} else if (meq(a.x.getDistanceMin(),b.x.getDistanceMax())) {
			return Direction.LEFT;
		} else if (meq(a.y.getDistanceMin(),b.y.getDistanceMax())) {
			return Direction.UP;
		} else if (isSamePoint(a, b)) {
			return null;
		} else {
			throw new LogicException("Overlap?");
		}
	}

	private boolean isSamePoint(BoundsBasedPositionRoutingInfo a, BoundsBasedPositionRoutingInfo b) {
		return isSamePointBounds(a.x, b.x) && isSamePointBounds(a.y, b.y);
	}

	private boolean isSamePointBounds(Bounds b1, Bounds b2) {
		return eq(b1.getDistanceMin(), b2.getDistanceMin()) 
				&& eq(b1.getDistanceMax() , b2.getDistanceMax())
				&& eq(b1.getDistanceMin() , b2.getDistanceMax());
	}
		
	private static final Double TOLERANCE = 0.000000001;

	public static boolean eq(double a, double b) {
		return Math.abs(a-b) < TOLERANCE;
	}
	
	public static boolean meq(double a, double b) {
		return a - b > -TOLERANCE;
	}

	@Override
	public void setHints(HintMap hm, RoutingInfo bounds) {
		PositionRoutingInfo pri = (PositionRoutingInfo) bounds;
		hm.put(PositioningHints.PLAN_MIN_X, (float) pri.getMinX());
		hm.put(PositioningHints.PLAN_MIN_Y, (float) pri.getMinY());
		hm.put(PositioningHints.PLAN_MAX_X, (float) pri.getMaxX());
		hm.put(PositioningHints.PLAN_MAX_Y, (float) pri.getMaxY());
	}

	@Override
	public RoutingInfo narrow(RoutingInfo bounds, double vertexTrim) {
		BoundsBasedPositionRoutingInfo pri = (BoundsBasedPositionRoutingInfo) bounds;
		return new BoundsBasedPositionRoutingInfo(narrow(pri.x, vertexTrim), narrow(pri.y, vertexTrim));
	}

	private Bounds narrow(Bounds bbounds, double vertexTrim) {
		return new BasicBounds(bbounds.getDistanceMin() + vertexTrim, bbounds.getDistanceMax() - vertexTrim);

	}
		
}