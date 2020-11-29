package org.kite9.diagram.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.mgt.router.LineRoutingInfo;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing;
import org.kite9.diagram.visualization.planarization.rhd.position.BoundsBasedPositionRoutingInfo;
import org.kite9.diagram.visualization.planarization.rhd.position.Corner;
import org.kite9.diagram.visualization.planarization.rhd.position.LinePositionRoutingInfo;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo;
import org.kite9.diagram.common.HelpMethods;

import junit.framework.Assert;


public class TestPositionRouter {

	PositionRoutableHandler2D prh = new PositionRoutableHandler2D();

	@Test
	public void testOverWhenHorizNextTo() {
		BoundsBasedPositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 3), new BasicBounds(0, 3));
		BoundsBasedPositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(5, 6), new BasicBounds(1, 2));
		BoundsBasedPositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(6, 7), new BasicBounds(4, 5));
		
		List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(pri, past, to);
		initAndPrint(l);
		
		LineRoutingInfo lri = prh.move(null, pri, null);
		lri = prh.move(lri, past, Routing.OVER_FORWARDS);
		lri = prh.move(lri, to, null);
		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertTrue(getHeight(lpri) == 0);
		Assert.assertTrue(getWidth(lpri) == 0);
		Assert.assertTrue(getMinX(lpri) == 6);
		Assert.assertEquals(6d, lpri.getRunningCost());
	}
	
	private int getMinX(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getX();
		return (int) (b.getDistanceMin());
	}

	private int getMinY(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getY();
		return (int) (b.getDistanceMin());
	}

	
	private int getWidth(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getX();
		return (int) ( b.getDistanceMax() - b.getDistanceMin());
	}

	private int getHeight(LinePositionRoutingInfo lpri) {
		Bounds b = lpri.getPositionForTesting().getY();
		return (int) ( b.getDistanceMax() - b.getDistanceMin());
	}

	@Test
	public void testDownWhenHorizNextTo() {
		PositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 3), new BasicBounds(0, 3));
		PositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(5, 6), new BasicBounds(1, 2));
		PositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(7, 8), new BasicBounds(0, 4));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(pri, past, to);
		initAndPrint(l);
		
		
		LineRoutingInfo lri = prh.move(null, pri, null);
		lri = prh.move(lri, past, Routing.UNDER_FORWARDS);
		lri = prh.move(lri, to, null);

		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertEquals(1, getHeight(lpri));
		Assert.assertEquals(0, getWidth(lpri));
		Assert.assertEquals(7, getMinX(lpri));
		Assert.assertEquals(2, getMinY(lpri));
		Assert.assertEquals(4d, lpri.getRunningCost());

	}
	
	@Test
	@Ignore("Not meeting assumption - why wasn't this already ignored?")
	public void testOverWhenAboveNextTo() {
		PositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(3, 6), new BasicBounds(3, 6));
		PositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(5, 6), new BasicBounds(1, 2));
		PositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 0), new BasicBounds(0, 0));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(pri, past, to);
		initAndPrint(l);
		
		
		LineRoutingInfo lri = prh.move(null, pri, null);
		lri = prh.move(lri, past, Routing.OVER_FORWARDS);
		lri = prh.move(lri, to, null);
		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertEquals(0, getHeight(lpri));
		Assert.assertEquals(0, getWidth(lpri));
		Assert.assertEquals(0, getMinX(lpri));
		Assert.assertEquals(0, getMinY(lpri));
		Assert.assertEquals(6d, lpri.getRunningCost()); 
	}
	
	@Test
	public void testGettingHome() {
		PositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 3), new BasicBounds(0, 3));
		PositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(5, 6), new BasicBounds(1, 2));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(pri, past);
		initAndPrint(l);
		
		
		LineRoutingInfo lri = prh.move(null, pri, null);
		lri = prh.move(lri, past, null);
		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertTrue(getHeight(lpri) == 1);
		Assert.assertTrue(getWidth(lpri) == 0);
		Assert.assertTrue(getMinX(lpri) == 5);
		Assert.assertTrue(lpri.getRunningCost() == 2);
	}
	
	@Test
	public void testRoundAPost() {
		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(0, 3));
		PositionRoutingInfo post = new BoundsBasedPositionRoutingInfo(new BasicBounds(5, 6), new BasicBounds(1, 2));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(start, post);
		initAndPrint(l);
		
		LineRoutingInfo lri1 = prh.move(null, start, null);
		lri1 = prh.move(lri1, post, null);
		Assert.assertTrue(lri1.getRunningCost() == 4);
		
		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, post, Routing.OVER_FORWARDS);
		lri2 = prh.move(lri2, post, Routing.UNDER_BACKWARDS);
		lri2 = prh.move(lri2, start, null);
		Assert.assertEquals(11d, lri2.getRunningCost());
	}
	
	@Test
	@Ignore("Really should fix this")
	public void testSomethingBigNotReallyInTheWay() {
		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(0, 1));
		PositionRoutingInfo post = new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(0, 5));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(3, 4));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(start, post, finish);
		initAndPrint(l);

		
		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, post, Routing.UNDER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertEquals(2d, lri2.getRunningCost());
	}
	
	@Test
	@Ignore("Needs fixing")
	public void testSomethingBigNotReallyInTheWay2() {
		// except, it is in the way because now we go the other way round it.
		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(0, 1));
		PositionRoutingInfo post = new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(0, 5));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(3, 4));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(start, post, finish);
		initAndPrint(l);

		
		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, post, Routing.OVER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertEquals(10d, lri2.getRunningCost());
	}
	
	@Test
	public void testSomethingBigNotReallyInTheWay3() {
		// taken from the 10_1 fail.
		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(0, 1));
		PositionRoutingInfo post = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(3, 4));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(4, 5), new BasicBounds(0, 1));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(start, post, finish);
		initAndPrint(l);

		
		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, post, Routing.OVER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertEquals(1d, lri2.getRunningCost());
	}
	
	@Test
	public void testBrokenBelow1() {
		// taken from the 10_1 fail (2).
		// looks like:
		//      2
		//   0  3 
		//  1

		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(.6251,.7491), new BasicBounds(.5631,.6246));
		PositionRoutingInfo posta9 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.5631,.6246), new BasicBounds(.6251,.7491));
		PositionRoutingInfo posta6 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363,.9979), new BasicBounds(.5009,.5628));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363,.9979), new BasicBounds(.5631,.6246));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(start, posta9, posta6, finish);
		initAndPrint(l);
		
		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, posta9, Routing.UNDER_FORWARDS);
		lri2 = prh.move(lri2, posta6, Routing.UNDER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertTrue(PositionRoutableHandler2D.eq(.6842, lri2.getRunningCost()));
	}
	
	@Test
	public void testBrokenBelow2() {
		// taken from the 10_1 fail (2).
		// looks like:
		//      2
		//   0  3 
		//  1

		PositionRoutingInfo start = new BoundsBasedPositionRoutingInfo(new BasicBounds(.6251,.7491), new BasicBounds(.5631,.6246));
		PositionRoutingInfo posta9 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.5631,.6246), new BasicBounds(.6251,.7491));
		PositionRoutingInfo posta6 = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363,.9979), new BasicBounds(.5009,.5628));
		PositionRoutingInfo finish = new BoundsBasedPositionRoutingInfo(new BasicBounds(.9363,.9979), new BasicBounds(.5631,.6246));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(start, posta9, posta6, finish);
		initAndPrint(l);
		
		LineRoutingInfo lri2 = prh.move(null, start, null);
		lri2 = prh.move(lri2, posta9, Routing.OVER_FORWARDS);
		lri2 = prh.move(lri2, posta6, Routing.UNDER_FORWARDS);
		lri2 = prh.move(lri2, finish, null);
		Assert.assertTrue(PositionRoutableHandler2D.eq(.1872, lri2.getRunningCost()));
	}
	
	@Test
	@Ignore("Not meeting assumption - why wasn't this already ignored?")
	public void testBackwards() {
		PositionRoutingInfo pri = new BoundsBasedPositionRoutingInfo(new BasicBounds(6, 9), new BasicBounds(0, 3));
		PositionRoutingInfo past = new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(1, 2));
		PositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(0, 1), new BasicBounds(4, 5));
		
		List<PositionRoutingInfo> l = HelpMethods.createList(pri, past, to);
		initAndPrint(l);
		
		LineRoutingInfo lri = prh.move(null, to, null);
		lri = prh.move(lri, past, Routing.OVER_BACKWARDS);
		lri = prh.move(lri, pri, null);
		
		LinePositionRoutingInfo lpri = (LinePositionRoutingInfo) lri;
		Assert.assertEquals(0, getHeight(lpri));
		Assert.assertEquals(0, getWidth(lpri));
		Assert.assertEquals(6, getMinX(lpri));
		Assert.assertEquals(3, getMinY(lpri));
		Assert.assertEquals(6d, lpri.getRunningCost());
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
	
	@Ignore("We don't have a fully rotational invariant approach because it never got used")
	@Test
	public void testVariousDifferentMovesAroundAnObstacleClockwise1() {
		double[] totals = new double[] { 1.5d, .5d, .5d, 0d, 3.5d, 2.5d, 2.5d, 1.5d}; 
		BoundsBasedPositionRoutingInfo[] fromPositions = createPositions1();
		BoundsBasedPositionRoutingInfo obstacle = new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(1, 2));
		BoundsBasedPositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(1.5, 2.5));
		Routing r = Routing.OVER_FORWARDS;
		for (int i = 0; i < fromPositions.length; i++) {
			
			List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(fromPositions[i], obstacle, to);
			initAndPrint(l);
			
			LineRoutingInfo lri = prh.move(null, fromPositions[i], null);
			lri = prh.move(lri, obstacle, r);
			lri = prh.move(lri, to, null);	
			System.out.println("Octant "+i+": X: "+lri.getHorizontalRunningCost()+"Y: "+lri.getVerticalRunningCost());
			Assert.assertEquals("Distance Wrong", totals[i], lri.getRunningCost());
		}
	}
	

	@Test
	@Ignore("We don't have a fully rotational invariant approach because it never got used")
	public void testVariousDifferentMovesAroundAnObstacleAntiClockwise1() {
		double[] totals = new double[] { 2d, 2d, 3d, 3d, 0d, 0d, 1d, 1d}; 
		BoundsBasedPositionRoutingInfo[] fromPositions = createPositions1();
		BoundsBasedPositionRoutingInfo obstacle = new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(1, 2));
		BoundsBasedPositionRoutingInfo to = new BoundsBasedPositionRoutingInfo(new BasicBounds(2, 3), new BasicBounds(1.5, 2.5));
		Routing r = Routing.UNDER_FORWARDS;
		for (int i = 0; i < fromPositions.length; i++) {
			
			List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(fromPositions[i], obstacle, to);
			initAndPrint(l);

			
			LineRoutingInfo lri = prh.move(null, fromPositions[i], null);
			lri = prh.move(lri, obstacle, r);
			lri = prh.move(lri, to, null);	
			System.out.println("Octant "+i+": Xc: "+lri.getHorizontalRunningCost()+"Yc: "+lri.getVerticalRunningCost());
			Assert.assertEquals("Distance Wrong", totals[i], lri.getRunningCost());
		}
	}
	
	
	@Ignore("We don't have a fully rotational invariant approach because it never got used")
	@Test
	public void testVariousDifferentMovesAroundAnObstacleClockwise2() {
		BoundsBasedPositionRoutingInfo[] fromPositions = createPositions1();
		BoundsBasedPositionRoutingInfo obstacle = new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(1, 2));
		
		Routing r = Routing.OVER_FORWARDS;
		double[][] totals = new double[][] { new double[] { 4d, 0d, 1d, 1d, 2d, 2d, 3d, 3d} ,
											  new double[] {3d, 0d, 0d, 1d, 1d, 2d, 2d, 3d }}; 
		
		for (int c = 0; c <= 1; c++) {
			for (int i = 0; i < fromPositions.length; i+=1) {
				Double total = totals[c][i];
				System.out.println("NEW ARRANGEMENT");
				for (int t = 0; t < 8; t+=2) {
					BoundsBasedPositionRoutingInfo start = fromPositions[t+c];
					BoundsBasedPositionRoutingInfo to = fromPositions[(i + t+c) % 8];
					
					List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(start, obstacle, to);
					initAndPrint(l);

					System.out.println("Start: "+(t+c)+" to: "+(i + t+c) % 8);
					System.out.println("Start: "+start+" obstacle: "+obstacle+" to: "+to);	
					LineRoutingInfo lri = prh.move(null, start, null);
					lri = prh.move(lri, obstacle, r);
					lri = prh.move(lri, to, null);
					double ttt = lri.getRunningCost();
					if (total == null) {
						total=ttt;
					} else {
						Assert.assertEquals("Not rotationally invariant", total, ttt);
					}

					System.out.println("Octant start "+i+" end "+((i + t) % 8)+": X: "+lri.getHorizontalRunningCost()+"Y: "+lri.getVerticalRunningCost());
				}
			}	
		}
	}
	
	@Ignore("We don't have a fully rotational invariant approach because it never got used")
	@Test
	public void testVariousDifferentMovesAroundAnObstacleClockwise3() {
		BoundsBasedPositionRoutingInfo[] fromPositions = createPositionsTH();
		BoundsBasedPositionRoutingInfo[] toPositions = createPositionsBH();
		
		BoundsBasedPositionRoutingInfo obstacle = new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(2, 4));
		
		Routing r = Routing.OVER_FORWARDS;
		double[] totals = new double[] { 6d, 0d, 6d, 4d };
		
		for (int c = 0; c <= 3; c++) {
			Double total = totals[c];
			int ci = 1 + c*2;
			int ii = ci;
			BoundsBasedPositionRoutingInfo start = fromPositions[ci];
			BoundsBasedPositionRoutingInfo to = toPositions[ii];
			
			List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(start, obstacle, to);
			initAndPrint(l);
			
			System.out.println("Start: "+ci+" to: "+ii+" expecting: "+total);
			System.out.println("Start: "+start+" obstacle: "+obstacle+" to: "+to);	
			LineRoutingInfo lri = prh.move(null, start, null);
			lri = prh.move(lri, obstacle, r);
			lri = prh.move(lri, to, null);
			double ttt = lri.getRunningCost();
			if (total != null) {
				Assert.assertEquals("Not rotationally invariant", total, ttt);
			}

			System.out.println("Octant start "+ci+" end "+ii+": X: "+lri.getHorizontalRunningCost()+"Y: "+lri.getVerticalRunningCost());
		}	
	}
	
	@Ignore("We don't have a fully rotational invariant approach because it never got used")
	@Test
	public void testVariousDifferentMovesAroundAnObstacleAntiClockwise2() {
		BoundsBasedPositionRoutingInfo[] fromPositions = createPositions1();
		BoundsBasedPositionRoutingInfo obstacle = new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(1, 2));
		
		Routing r = Routing.UNDER_FORWARDS;
		double[][] totals = new double[][] { 
				new double[] { 0d, 3d, 3d, 2d, 2d, 1d, 1d, 0d} ,
				  new double[] {0d, 3d, 2d, 2d, 1d, 1d, 0d, 0d }}; 
		
		for (int c = 0; c <= 1; c++) {
			for (int i = 0; i < fromPositions.length; i+=1) {
				Double total = totals[c][i];
				System.out.println("NEW ARRANGEMENT");
				for (int t = 0; t < 8; t+=2) {
					BoundsBasedPositionRoutingInfo start = fromPositions[t+c];
					BoundsBasedPositionRoutingInfo to = fromPositions[(i + t+c) % 8];
					
					List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(start, obstacle, to);
					initAndPrint(l);
					
					System.out.println("Start: "+(t+c)+" to: "+(i + t+c) % 8+" expecting "+total);
					System.out.println("Start: "+start+" obstacle: "+obstacle+" to: "+to);	
					LineRoutingInfo lri = prh.move(null, start, null);
					lri = prh.move(lri, obstacle, r);
					lri = prh.move(lri, to, null);
					double ttt = lri.getRunningCost();
					if (total == null) {
						total=ttt;
					} else {
						Assert.assertEquals("Not rotationally invariant", total, ttt);
					}

					System.out.println("Octant start "+i+" end "+((i + t) % 8)+": X: "+lri.getHorizontalRunningCost()+"Y: "+lri.getVerticalRunningCost());
				}
			}	
		}
	}
	
	@Ignore("We don't have a fully rotational invariant approach because it never got used")
	@Test
	public void testVariousDifferentMovesAroundAnObstacleAntiClockwise3() {
		BoundsBasedPositionRoutingInfo[] fromPositions = createPositionsTH();
		BoundsBasedPositionRoutingInfo[] toPositions = createPositionsBH();
		
		BoundsBasedPositionRoutingInfo obstacle = new BoundsBasedPositionRoutingInfo(new BasicBounds(1, 2), new BasicBounds(2, 4));
		
		Routing r = Routing.UNDER_FORWARDS;
		double[] totals = new double[] { 0d, 4d, 0d, 0d };
		
		for (int c = 0; c <= 3; c++) {
			Double total = totals[c];
			int ci = 1 + c*2;
			int ii = ci;
			BoundsBasedPositionRoutingInfo start = fromPositions[ci];
			BoundsBasedPositionRoutingInfo to = toPositions[ii];
			
			List<BoundsBasedPositionRoutingInfo> l = HelpMethods.createList(start, obstacle, to);
			initAndPrint(l);
			
			System.out.println("Start: "+ci+" to: "+ii);
			System.out.println("Start: "+start+" obstacle: "+obstacle+" to: "+to);	
			LineRoutingInfo lri = prh.move(null, start, null);
			lri = prh.move(lri, obstacle, r);
			lri = prh.move(lri, to, null);
			double ttt = lri.getRunningCost();
			if (total == null) {
				total=ttt;
			} else {
				Assert.assertEquals("Not rotationally invariant", total, ttt);
			}

			System.out.println("Octant start "+ci+" end "+ii+": X: "+lri.getHorizontalRunningCost()+"Y: "+lri.getVerticalRunningCost());
		}	
	}

	private void initAndPrint(List<? extends PositionRoutingInfo> l) {
		Printer p = new Printer();
		int i = 0;
		for (PositionRoutingInfo b : l) {
			if (b instanceof BoundsBasedPositionRoutingInfo) {
				p.add((BoundsBasedPositionRoutingInfo) b, ""+i);
			}
			i++;
		}
		
		p.print();
		prh.initRoutableOrdering(l);
		
		i=0;
		for (PositionRoutingInfo b : l) {
			if (b instanceof BoundsBasedPositionRoutingInfo) {
				Map<Routing, Corner> avoidanceCorners = ((BoundsBasedPositionRoutingInfo)b).getAvoidanceCorners();
				System.out.println(i+"    "+((avoidanceCorners==PositionRoutableHandler2D.BASIC_AVOIDANCE_CORNERS) ? "ac" : avoidanceCorners));
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
					place((int)  x, (int) y, c.charAt(0));
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
					System.out.println(new String(line, 0, maxWidth+1));
				}
			}
			
			System.out.println("");
		}
	}
	
}
