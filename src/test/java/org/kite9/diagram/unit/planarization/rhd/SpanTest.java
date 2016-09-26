package org.kite9.diagram.unit.planarization.rhd;

import org.junit.Assert;
import org.junit.Test;
import org.kite9.diagram.visualization.planarization.Span;

public class SpanTest {

	@Test
	public void testSpanClass() {
		Span a = new Span(1, 4);
		Span b = new Span(3, 4);
		Assert.assertEquals(-1, a.compareTo(b));
		Assert.assertEquals(1, b.compareTo(a));
		Assert.assertEquals(0, a.compareTo(a));
		
		// compound spans
		Span c = new Span(2, 7, b);
		Assert.assertEquals(1, c.compareTo(b));
		Assert.assertEquals(-1, b.compareTo(c));
		Assert.assertEquals(0, c.compareTo(c));
		
		// different compounds
		Span d = new Span(4, 8, a);
		Assert.assertEquals(1, c.compareTo(d));
		
		// converting
		Assert.assertEquals(75, (int) b.convert(100f));
		Assert.assertEquals(48, (int) d.convert(128f));
	}
}
