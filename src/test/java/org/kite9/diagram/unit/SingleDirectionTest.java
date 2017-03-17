package org.kite9.diagram.unit;

import org.junit.Test;
import org.kite9.diagram.common.algorithms.so.PositionChangeNotifiable;
import org.kite9.diagram.common.algorithms.so.SingleDirection;

import junit.framework.Assert;

public class SingleDirectionTest {

	private PositionChangeNotifiable createNotifiable(String name) {
		return new PositionChangeNotifiable() {
			
			@Override
			public void changedPosition(int pos) {
				System.out.println(name+" moved to "+pos);
			}
			
			public String toString() {
				return name;
			}
		};
	}
	
	/**
	 * A ---10---> B --5--> D
	 * E ---5 ---> |
	 *      C <-5- |        |
	 *      | -------8----> |
	 * 
	 */
	@Test
	public void testSlideableIncreasing() {
		SingleDirection a = new SingleDirection(createNotifiable("A"), true);
		SingleDirection b = new SingleDirection(createNotifiable("B"), true);
		SingleDirection c = new SingleDirection(createNotifiable("C"), true);
		SingleDirection d = new SingleDirection(createNotifiable("D"), true);
		SingleDirection e = new SingleDirection(createNotifiable("E"), true);
		
		// a, b, c to start with
		a.addForwardConstraint(b, 10);
		b.addBackwardConstraint(c, 5);
		a.increasePosition(0);
		Assert.assertEquals(0, (int) a.getPosition());
		Assert.assertEquals(10, (int) b.getPosition());
		Assert.assertEquals(5, (int) c.getPosition());
		Assert.assertEquals(null, d.getPosition());

		// check d
		b.addForwardConstraint(d, 5);
		c.addForwardConstraint(d, 8);
		Assert.assertEquals(15, (int) d.getPosition());
		
		// check e
		e.addForwardConstraint(b, 5);
		Assert.assertEquals(10, (int) b.getPosition());
		Assert.assertEquals(5, (int) e.minimumDistanceTo(b, 100));
		Assert.assertEquals(10, (int) e.minimumDistanceTo(d, 100));

		e.increasePosition(0);
		Assert.assertEquals(0, (int) e.getPosition());
		
		// without moving e
		Assert.assertEquals(15, (int) e.minimumDistanceTo(d, e.getPosition()));
		// allow it to move
		Assert.assertEquals(10, (int) e.minimumDistanceTo(d, 100));
		
		Assert.assertEquals(0, d.getMaxDepth());
		Assert.assertEquals(3, a.getMaxDepth());
		Assert.assertEquals(1, c.getMaxDepth());
		
//		Assert.assertTrue(a.hasTransitiveForwardConstraintTo(d));
//		Assert.assertFalse(c.hasTransitiveForwardConstraintTo(b));
//		Assert.assertTrue(e.hasTransitiveForwardConstraintTo(b));
//		
	}
	
	/**
	 * D <------20--------- A
	 * |         B <---10-- A
	 * |         | -5-> C  
	 * | <----7---------|
	 * | <-----5------------E
 	 * 
	 */
	@Test
	public void testSlideableDecreasing() {
		SingleDirection a = new SingleDirection(createNotifiable("A"), false);
		SingleDirection b = new SingleDirection(createNotifiable("B"), false);
		SingleDirection c = new SingleDirection(createNotifiable("C"), false);
		SingleDirection d = new SingleDirection(createNotifiable("D"), false);
		SingleDirection e = new SingleDirection(createNotifiable("D"), false);
		
		// a, b, c to start with
		a.addForwardConstraint(d, 20);
		b.addBackwardConstraint(c, 5);
		c.addForwardConstraint(d, 7);
		a.addForwardConstraint(b, 10);
		e.addForwardConstraint(d, 5);
		Assert.assertEquals(5, (int) a.minimumDistanceTo(c, 100));
		Assert.assertEquals(2, (int) b.minimumDistanceTo(d, 100));
		Assert.assertEquals(null, e.minimumDistanceTo(a, 100));
		
		
		a.increasePosition(100);
		Assert.assertEquals(100, (int) a.getPosition());
		Assert.assertEquals(90, (int) b.getPosition());
		Assert.assertEquals(95, (int) c.getPosition());
		Assert.assertEquals(80, (int) d.getPosition());
		Assert.assertEquals(2, (int) b.minimumDistanceTo(d, 40));
		
		Assert.assertEquals(0, d.getMaxDepth());
		Assert.assertEquals(3, a.getMaxDepth());
		Assert.assertEquals(1, c.getMaxDepth());
		Assert.assertEquals(1, e.getMaxDepth());

	}
	
	
}
