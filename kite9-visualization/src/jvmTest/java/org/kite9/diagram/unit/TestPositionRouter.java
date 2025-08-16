package org.kite9.diagram.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.mgt.router.LineRoutingInfo;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing;
import org.kite9.diagram.visualization.planarization.rhd.position.BoundsBasedPositionRoutingInfo;
import org.kite9.diagram.visualization.planarization.rhd.position.Corner;
import org.kite9.diagram.visualization.planarization.rhd.position.LinePositionRoutingInfo;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo;
import org.kite9.diagram.common.HelpMethods;

public class TestPositionRouter {

	@BeforeClass
	public static void setLoggingFactory() {
		Kite9Log.Companion.setFactory(l -> new Kite9LogImpl(l));
	}

	PositionRoutableHandler2D prh = new PositionRoutableHandler2D();

	@Test
	public void testOverWhenHorizNextTo() {
		BoundsBasedPositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 0.3),
				new BasicBounds(0, 0.3));
		BoundsBasedPositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(0.5, 0.6),
				new BasicBounds(0.1, 0.2));
		BoundsBasedPositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(0.6, 0.7),
				new BasicBounds(0.4, 0.5));

		List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(pri, past, to);
		initAndPrint(l);

		LineRoutingInfo lri = prh.move(null, pri, null);
		lri = prh.move(lri, past, Routing.OVER_FORWARDS);
		lri = prh.move(lri, to, null);
		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertTrue(getHeight(lpri) == 0);
		Assert.assertTrue(getWidth(lpri) == 0);
		Assert.assertTrue(getMinX(lpri) == 0.6);
		Assert.assertEquals(0.6d, lpri.getRunningCost(), 0.01);
	}

	private double getMinX(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getX();
		return (b.getDistanceMin());
	}

	private double getMinY(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getY();
		return (b.getDistanceMin());
	}

	private double getWidth(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getX();
		return (b.getDistanceMax() - b.getDistanceMin());
	}

	private double getHeight(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getY();
		return (b.getDistanceMax() - b.getDistanceMin());
	}

	@Test
	public void testDownWhenHorizNextTo() {
		PositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 0.3), new BasicBounds(0, .3));
		PositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(0.5, 0.6),
				new BasicBounds(.1, .2));
		PositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(0.7, 0.8), new BasicBounds(0, .4));

		List<PositionRoutingInfo> l = HelpMethods.createList(pri, past, to);
		initAndPrint(l);

		LineRoutingInfo lri = prh.move(null, pri, null);
		lri = prh.move(lri, past, Routing.UNDER_FORWARDS);
		lri = prh.move(lri, to, null);

		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertEquals(0.1, getHeight(lpri), 0.001);
		Assert.assertEquals(0, getWidth(lpri), 0.001);
		Assert.assertEquals(.7, getMinX(lpri), 0.001);
		Assert.assertEquals(.2, getMinY(lpri), 0.001);
		Assert.assertEquals(.4d, lpri.getRunningCost(), 0.01);

	}

	@Test
	public void testGettingHome() {
		PositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, .3), new BasicBounds(0, .3));
		PositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(.5, .6), new BasicBounds(.1, .2));

		List<PositionRoutingInfo> l = HelpMethods.createList(pri, past);
		initAndPrint(l);

		LineRoutingInfo lri = prh.move(null, pri, null);
		lri = prh.move(lri, past, null);
		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertTrue(getHeight(lpri) == .1);
		Assert.assertTrue(getWidth(lpri) == 0);
		Assert.assertTrue(getMinX(lpri) == .5);
		Assert.assertTrue(lpri.getRunningCost() == .2);
	}

	@Test
	public void testRoundAPost() {
		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, .1), new BasicBounds(0, .3));
		PositionRoutingInfo post = new BoundsBasedPositionRoutingInfo(new BasicBounds(.5, .6), new BasicBounds(.1, .2));

		List<PositionRoutingInfo> l = HelpMethods.createList(start, post);
		initAndPrint(l);

		LineRoutingInfo lri1 = prh.move(null, start, null);
		lri1 = prh.move(lri1, post, null);
		Assert.assertTrue(lri1.getRunningCost() == .4);

		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, post, Routing.OVER_FORWARDS);
		lri2 = prh.move(lri2, post, Routing.UNDER_BACKWARDS);
		lri2 = prh.move(lri2, start, null);
		Assert.assertEquals(1.1d, lri2.getRunningCost(), 0.01);
	}

	@Test
	public void testSomethingBigNotReallyInTheWay3() {
		// taken from the 10_1 fail.
		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(.2, .3), new BasicBounds(0, .1));
		PositionRoutingInfo post = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, .1), new BasicBounds(.3, .4));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(.4, .5),
				new BasicBounds(0, .1));

		List<PositionRoutingInfo> l = HelpMethods.createList(start, post, finish);
		initAndPrint(l);

		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, post, Routing.OVER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertEquals(.1d, lri2.getRunningCost(), 0.01);
	}

	@Test
	public void testBrokenBelow1() {
		// taken from the 10_1 fail (2).
		// looks like:
		// 2
		// 0 3
		// 1

		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(.6251, .7491),
				new BasicBounds(.5631, .6246));
		PositionRoutingInfo posta9 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.5631, .6246),
				new BasicBounds(.6251, .7491));
		PositionRoutingInfo posta6 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363, .9979),
				new BasicBounds(.5009, .5628));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363, .9979),
				new BasicBounds(.5631, .6246));

		List<PositionRoutingInfo> l = HelpMethods.createList(start, posta9, posta6, finish);
		initAndPrint(l);

		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, posta9, Routing.UNDER_FORWARDS);
		lri2 = prh.move(lri2, posta6, Routing.UNDER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertTrue(PositionRoutableHandler2D.Companion.eq(.6842, lri2.getRunningCost()));
	}

	@Test
	public void testBrokenBelow2() {
		// taken from the 10_1 fail (2).
		// looks like:
		// 2
		// 0 3
		// 1

		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(.6251, .7491),
				new BasicBounds(.5631, .6246));
		PositionRoutingInfo posta9 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.5631, .6246),
				new BasicBounds(.6251, .7491));
		PositionRoutingInfo posta6 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363, .9979),
				new BasicBounds(.5009, .5628));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363, .9979),
				new BasicBounds(.5631, .6246));

		List<PositionRoutingInfo> l = HelpMethods.createList(start, posta9, posta6, finish);
		initAndPrint(l);

		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, posta9, Routing.OVER_FORWARDS);
		lri2 = prh.move(lri2, posta6, Routing.UNDER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertTrue(PositionRoutableHandler2D.Companion.eq(.1872, lri2.getRunningCost()));
	}

	@Test
	public void boundsTesting() {
		Bounds b = prh.getTopLevelBounds(true);
		Bounds left = prh.narrow(Layout.LEFT, b, true, false);
		Bounds right = prh.narrow(Layout.RIGHT, b, true, false);
		Assert.assertFalse(prh.overlaps(left, right));
		Assert.assertTrue(prh.overlaps(b, right));
		Assert.assertTrue(prh.overlaps(left, b));
	}

	private void initAndPrint(List<? extends PositionRoutingInfo> l) {
		Printer p = new Printer();
		int i = 0;
		for (PositionRoutingInfo b : l) {
			if (b instanceof BoundsBasedPositionRoutingInfo) {
				p.add((BoundsBasedPositionRoutingInfo) b, "" + i);
			}
			i++;
		}

		p.print();
		prh.initRoutableOrdering(l);

		i = 0;
		for (PositionRoutingInfo b : l) {
			if (b instanceof BoundsBasedPositionRoutingInfo) {
				Map<Routing, Corner> avoidanceCorners = ((BoundsBasedPositionRoutingInfo) b).getAvoidanceCorners();
				System.out.println(i + "    "
						+ ((avoidanceCorners == PositionRoutableHandler2D.Companion.getBASIC_AVOIDANCE_CORNERS()) ? "ac"
								: avoidanceCorners));
				i++;
			}
		}
	}

	private BoundsBasedPositionRoutingInfo[] createPositions1() {
		List<BoundsBasedPositionRoutingInfo> out = new ArrayList<BoundsBasedPositionRoutingInfo>();

		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(0, 1)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(0, 1)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(0, 1)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(1, 2)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(2, 3)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(2, 3)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(2, 3)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(1, 2)));

		return (BoundsBasedPositionRoutingInfo[]) out.toArray(new BoundsBasedPositionRoutingInfo[out.size()]);
	}

	private BoundsBasedPositionRoutingInfo[] createPositionsTH() {
		List<BoundsBasedPositionRoutingInfo> out = new ArrayList<BoundsBasedPositionRoutingInfo>();

		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(0, 1)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(0, 1)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(0, 1)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(2, 3)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(4, 5)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(4, 5)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(4, 5)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(2, 3)));

		return (BoundsBasedPositionRoutingInfo[]) out.toArray(new BoundsBasedPositionRoutingInfo[out.size()]);
	}

	private BoundsBasedPositionRoutingInfo[] createPositionsBH() {
		List<BoundsBasedPositionRoutingInfo> out = new ArrayList<BoundsBasedPositionRoutingInfo>();

		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(1, 2)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(1, 2)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(1, 2)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(3, 4)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(5, 6)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(5, 6)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(5, 6)));
		out.add(new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(3, 4)));

		return (BoundsBasedPositionRoutingInfo[]) out.toArray(new BoundsBasedPositionRoutingInfo[out.size()]);
	}

	static class Printer {
		char[][] in = new char[100][];
		int maxWidth, maxHeight;

		public void add(BoundsBasedPositionRoutingInfo bb, String c) {
			for (double x = bb.getX().getDistanceMin(); x < bb.getX().getDistanceMax(); x++) {
				for (double y = bb.getY().getDistanceMin(); y < bb.getY().getDistanceMax(); y++) {
					place((int) x, (int) y, c.charAt(0));
				}
			}

		}

		private void place(int x, int y, char c) {
			char[] line = in[y];
			if (line == null) {
				line = new char[100];
				for (int i = 0; i < line.length; i++) {
					line[i] = '.';
				}
				in[y] = line;
			}

			line[x] = c;
			maxWidth = Math.max(maxWidth, x);
			maxHeight = Math.max(maxHeight, y);
		}

		public void print() {
			System.out.println("Shape");
			System.out.println("-----");

			for (int y = 0; y <= maxHeight; y++) {
				char[] line = in[y];
				if (line == null) {
					System.out.println("");
				} else {
					System.out.println(new String(line, 0, maxWidth + 1));
				}
			}

			System.out.println("");
		}
	}

}
