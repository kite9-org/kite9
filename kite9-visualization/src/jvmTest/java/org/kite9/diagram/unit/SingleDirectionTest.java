//package org.kite9.diagram.unit;
//
//import org.junit.Assert;
//import org.junit.jupiter.api.Test;
//import org.kite9.diagram.common.algorithms.so.PositionChangeNotifiable;
//import org.kite9.diagram.common.algorithms.so.SingleDirection;
//
//public class SingleDirectionTest {
//
//	private PositionChangeNotifiable createNotifiable(String name) {
//		return new PositionChangeNotifiable() {
//
//			@Override
//			public void changedPosition(int pos) {
//				System.out.println(name + " moved to " + pos);
//			}
//
//			public String toString() {
//				return name;
//			}
//		};
//	}
//
//	/**
//	 * A ---10---> B --5--> D
//	 * E ---5 ---> |
//	 * C <-5- | |
//	 * | -------8----> |
//	 *
//	 */
//	@Test
//	public void testSlideableIncreasing() {
//		SingleDirection a = new SingleDirection(createNotifiable("A"), true);
//		SingleDirection b = new SingleDirection(createNotifiable("B"), true);
//		SingleDirection c = new SingleDirection(createNotifiable("C"), true);
//		SingleDirection d = new SingleDirection(createNotifiable("D"), true);
//		SingleDirection e = new SingleDirection(createNotifiable("E"), true);
//
//		// a, b, c to start with
//		a.addForwardConstraint(b, 10,100);
//		b.addBackwardConstraint(c, 5,100);
//		a.increasePosition(0,100);
//		Assert.assertEquals(0, (int) a.getPosition());
//		Assert.assertEquals(10, (int) b.getPosition());
//		Assert.assertEquals(5, (int) c.getPosition());
//		Assert.assertEquals(0, (int) d.getPosition());
//
//		// check d
//		b.addForwardConstraint(d, 5,100);
//		c.addForwardConstraint(d, 8,100);
//		Assert.assertEquals(15, (int) d.getPosition());
//
//		// check e
//		e.addForwardConstraint(b, 5,100);
//		Assert.assertEquals(10, (int) b.getPosition());
//		Assert.assertEquals(5, e.minimumDistanceTo(b, 100,100));
//		Assert.assertEquals(10, e.minimumDistanceTo(d, 100,100));
//		Assert.assertEquals(null, e.minimumDistanceTo(a, 100,100));
//		Assert.assertEquals(null, a.minimumDistanceTo(e, 100,100));
//
//		e.increasePosition(0,100);
//		Assert.assertEquals(0, (int) e.getPosition());
//		Assert.assertEquals(10,  e.minimumDistanceTo(d, 100,100));
//		Assert.assertEquals(null, a.minimumDistanceTo(e, 100,100));
//		Assert.assertEquals(null, e.minimumDistanceTo(a, 100,100));
//
//		Assert.assertEquals(0, d.getMaxDepth());
//		Assert.assertEquals(3, a.getMaxDepth());
//		Assert.assertEquals(1, c.getMaxDepth());
//
//		// Assert.assertTrue(a.hasTransitiveForwardConstraintTo(d));
//		// Assert.assertFalse(c.hasTransitiveForwardConstraintTo(b));
//		// Assert.assertTrue(e.hasTransitiveForwardConstraintTo(b));
//		//
//	}
//
//	/**
//	 * D <------20--------- A
//	 * | B <---10-- A
//	 * | | -5-> C
//	 * | <----7---------|
//	 * | <-----5------------E
//	 *
//	 */
//	@Test
//	public void testSlideableDecreasing() {
//		SingleDirection a = new SingleDirection(createNotifiable("A"), false);
//		SingleDirection b = new SingleDirection(createNotifiable("B"), false);
//		SingleDirection c = new SingleDirection(createNotifiable("C"), false);
//		SingleDirection d = new SingleDirection(createNotifiable("D"), false);
//		SingleDirection e = new SingleDirection(createNotifiable("D"), false);
//
//		// a, b, c to start with
//		a.addForwardConstraint(d, 20,100);
//		b.addBackwardConstraint(c, 5,100);
//		c.addForwardConstraint(d, 7,100);
//		a.addForwardConstraint(b, 10,100);
//		e.addForwardConstraint(d, 5,100);
//		Assert.assertEquals(5, a.minimumDistanceTo(c, 100,100));
//		Assert.assertEquals(2, b.minimumDistanceTo(d, 100,100));
//		Assert.assertEquals(null, e.minimumDistanceTo(a, 100,100));
//
//		a.increasePosition(100,100);
//		Assert.assertEquals(100, (int) a.getPosition());
//		Assert.assertEquals(90, (int) b.getPosition());
//		Assert.assertEquals(95, (int) c.getPosition());
//		Assert.assertEquals(80, (int) d.getPosition());
//		Assert.assertEquals(2, b.minimumDistanceTo(d, 40,100));
//
//		Assert.assertEquals(0, d.getMaxDepth());
//		Assert.assertEquals(3, a.getMaxDepth());
//		Assert.assertEquals(1, c.getMaxDepth());
//		Assert.assertEquals(1, e.getMaxDepth());
//
//	}
//
//	/**
//	 * A <------- 10 ------ D
//	 * |--4->B---4--->C
//	 */
//	@Test
//	public void testCanInsertIncreasing() {
//		SingleDirection a = new SingleDirection(createNotifiable("A"), true);
//		SingleDirection b = new SingleDirection(createNotifiable("B"), true);
//		SingleDirection c = new SingleDirection(createNotifiable("C"), true);
//		SingleDirection d = new SingleDirection(createNotifiable("D"), true);
//		d.addBackwardConstraint(a, 10,100);
//		a.addForwardConstraint(b, 4,100);
//		b.addForwardConstraint(c, 4,100);
//
//		Assert.assertTrue(c.canAddForwardConstraint(d, 2,100));
//		Assert.assertTrue(a.canAddForwardConstraint(d, 5,100));
//		Assert.assertFalse(a.canAddForwardConstraint(d, 15,100));
//		Assert.assertFalse(c.canAddForwardConstraint(d, 3,100));
//		Assert.assertTrue(b.canAddForwardConstraint(c, 100,100));
//		Assert.assertTrue(c.canAddForwardConstraint(d, 1,100));
//	}
//
//	/**
//	 * D -------- 10 -----> A
//	 * | C<--4--- B <-4-|
//	 */
//	@Test
//	public void testCanInsertDecreasing() {
//		SingleDirection a = new SingleDirection(createNotifiable("A"), false);
//		SingleDirection b = new SingleDirection(createNotifiable("B"), false);
//		SingleDirection c = new SingleDirection(createNotifiable("C"), false);
//		SingleDirection d = new SingleDirection(createNotifiable("D"), false);
//		d.addBackwardConstraint(a, 10,100);
//		a.addForwardConstraint(b, 4,100);
//		b.addForwardConstraint(c, 4,100);
//
//		Assert.assertTrue(b.canAddForwardConstraint(c, 100,100));
//		Assert.assertFalse(a.canAddForwardConstraint(d, 15,100));
//		Assert.assertTrue(c.canAddForwardConstraint(d, 2,100));
//		Assert.assertTrue(a.canAddForwardConstraint(d, 5,100));
//		Assert.assertFalse(c.canAddForwardConstraint(d, 3,100));
//		Assert.assertTrue(c.canAddForwardConstraint(d, 1,100));
//	}
//}
