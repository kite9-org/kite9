package org.kite9.diagram.unit;

import org.junit.Assert;
import org.junit.Test;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.layout.ExitMatrix;
import org.kite9.diagram.visualization.planarization.rhd.layout.ExitMatrixEvaluator;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public class ExitMatrixEvaluatorTest {

	@Test
	public void testSideInterfere() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix a = new ExitMatrix(
			new float[][] { 
				new float[] { 1, 1, 2 }, 
				new float[] { 0, 0, 0 },
				new float[] { 0, 0, 0 }}, false);
		
		// 2 for the corner crossing. 3 x .5 for the chance of cross with the middle
		Assert.assertEquals(3.5f, (float) new ExitMatrixEvaluator().countOverlaps(a, a, Layout.LEFT, rh));
	}
	
	@Test
	public void testSideOcclusionNone() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix left = new ExitMatrix(
			new float[][] { 
				new float[] { 0, 0, 0 }, 
				new float[] { 0, 0, 1 },
				new float[] { 0, 0, 0 }},  false);
		
		ExitMatrix right = new ExitMatrix(
				new float[][] { 
					new float[] { 0, 0, 0 }, 
					new float[] { 0, 0, 1 },
					new float[] { 0, 0, 0 }}, false);
		
		Assert.assertEquals(0f, (float) new ExitMatrixEvaluator().countOverlaps(left, right, Layout.LEFT, rh));
	}
	

	@Test
	public void testSideOcclusionLots() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix left = new ExitMatrix(
			new float[][] { 
				new float[] { 0, 0, 0 }, 
				new float[] { 0, 0, 1 },
				new float[] { 0, 0, 0 }}, false);
		
		ExitMatrix right = new ExitMatrix( 
				new float[][] { 
					new float[] { 2, 1, 3 }, 
					new float[] { 0, 0, 1 },
					new float[] { 0, 5, 0 }}, false);
		
		// edge leaving left has to cross 5 others, with prob .5
		Assert.assertEquals(2.5f, (float)  new ExitMatrixEvaluator().countOverlaps(left, right, Layout.LEFT, rh));
	}
	
	@Test
	public void testFacingOcclusion() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix left = new ExitMatrix( 
			new float[][] { 
				new float[] { 0, 3, 0 }, 
				new float[] { 0, 0, 1 },
				new float[] { 0, 0, 0 }}, false);
		
		ExitMatrix right = new ExitMatrix(
				new float[][] { 
					new float[] { 0, 3, 0 }, 
					new float[] { 1, 0, 1 },
					new float[] { 0, 0, 0 }}, false);
		
		// edges facing each other count at .25 each
		Assert.assertEquals(.5f, (float) new ExitMatrixEvaluator().countOverlaps(left, right, Layout.LEFT, rh));
	}
	
	@Test
	public void testExtraDistance() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix left = new ExitMatrix(
			new float[][] { 
				new float[] { 0, 3, 0 }, 
				new float[] { 0, 0, 1 },
				new float[] { 0, 0, 0 }}, false);
		
		left.setSize(new BasicBounds(0, .25),  new BasicBounds(0, .5));
		ExitMatrix right = new ExitMatrix( 
				new float[][] { 
					new float[] { 0, 3, 0 }, 
					new float[] { 1, 0, 1 },
					new float[] { 0, 0, 0 }}, false);
		
		right.setSize(new BasicBounds(0, .25),  new BasicBounds(0, .5));
		
		// right matrix left side gets cost of .25, left matrix right side gets .25
		Assert.assertEquals(.5f, (float) new ExitMatrixEvaluator().calculateExtraExternalLinkDistance(left, right, Layout.LEFT, rh));
		// right (now bottom) matrix up side gets cost of .5 each
		Assert.assertEquals(1.5f, (float) new ExitMatrixEvaluator().calculateExtraExternalLinkDistance(left, right, Layout.UP, rh));
	}
	
	@Test
	public void testSideCrossing() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix top = new ExitMatrix(
			new float[][] { 
				new float[] { 0, 0, 1 }, 
				new float[] { 3, 0, 3 },
				new float[] { 1, 0, 2 }}, false);
		
		ExitMatrix bottom = new ExitMatrix( 
				new float[][] { 
					new float[] { 0, 3, 0 }, 
					new float[] { 0, 0, 0 },
					new float[] { 0, 0, 0 }}, false);
		
				
		// bottom matrix up (3) must cross left of top (4), with factor 1/2 .
		Assert.assertEquals(6f, (float) new ExitMatrixEvaluator().countOverlaps(top, bottom, Layout.UP, rh));
	}
	
	/**
	 * In this case, the narrow should be on the bottom, otherwise there will be too much occlusion
	 */
	@Test
	public void testWideAndNarrowSpans() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix wide = new ExitMatrix( 
			new float[][] { 
				new float[] { 0, 0, 0 }, 
				new float[] { 0, 0, 0 },
				new float[] { 0, 2, 0 }}, false);
		
		ExitMatrix narrow = new ExitMatrix(
				new float[][] { 
					new float[] { 0, 0, 0 }, 
					new float[] { 0, 0, 0 },
					new float[] { 0, 1, 0 }}, false);
		
		wide.setSpans(new Bounds[] { new BasicBounds(0, 1), new BasicBounds(0, 1), new BasicBounds(0, 1), new BasicBounds(0, 1)});
		narrow.setSpans(new Bounds[] { new BasicBounds(.25, .5), new BasicBounds(.25, .5), new BasicBounds(.25, .5), new BasicBounds(.25, .5)});
		wide.setSize(new BasicBounds(0, 1), new BasicBounds(0, 1));
		narrow.setSize(new BasicBounds(.25, .5), new BasicBounds(.25, .5));
		
		// since wide is wider than narrow, we are only likely to cross about 1/2 of the wide stuff.
		Assert.assertEquals(.25f, (float) new ExitMatrixEvaluator().countOverlaps(wide, narrow, Layout.UP, rh));
		
		// narrow must cross one of wide's links, factor = 1/2
		Assert.assertEquals(.5f, (float) new ExitMatrixEvaluator().countOverlaps(wide, narrow, Layout.DOWN, rh));

		// wide link going past narrow
		Assert.assertEquals(.5f, (float) new ExitMatrixEvaluator().calculateExtraExternalLinkDistance(wide, narrow, Layout.UP, rh));
		// single narrow link going further
		Assert.assertEquals(1f, (float) new ExitMatrixEvaluator().calculateExtraExternalLinkDistance(wide, narrow, Layout.DOWN, rh));

	}
	
	/**
	 * narrow and wide are offset, resulting in minimal occlusion
	 */
	@Test
	public void testWideAndNarrowSpansOffset() {
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		ExitMatrix wide = new ExitMatrix( 
			new float[][] { 
				new float[] { 0, 0, 0 }, 
				new float[] { 0, 0, 0 },
				new float[] { 0, 3, 0 }}, false);
		
		ExitMatrix narrow = new ExitMatrix(
				new float[][] { 
					new float[] { 0, 0, 0 }, 
					new float[] { 0, 0, 0 },
					new float[] { 0, 1, 0 }}, false);
		
		wide.setSpans(new Bounds[] { new BasicBounds(0, .75), new BasicBounds(0, .75), new BasicBounds(0, .75), new BasicBounds(0, .75)});
		narrow.setSpans(new Bounds[] { new BasicBounds(.5, 1), new BasicBounds(.5, 1), new BasicBounds(.5, 1), new BasicBounds(.5, 1)});
		
		// wide on top, but only 1 link is obscured, having 1/2 chance of crossing b, and 1/2 multiplier
		Assert.assertEquals(.25f, (float) new ExitMatrixEvaluator().countOverlaps(wide, narrow, Layout.UP, rh));
		
		// only half of top is obscured, by 1/3 of the other. 1/2 * 1 * 1/2 multiplier = .25
		Assert.assertEquals(.25f, (float) new ExitMatrixEvaluator().countOverlaps(wide, narrow, Layout.DOWN, rh));

	}
	
	
}
