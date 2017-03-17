package org.kite9.diagram.unit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.visualization.display.sizesorter.BasicSizeSorter;
import org.kite9.diagram.visualization.display.sizesorter.SizeSorter;
import org.kite9.diagram.visualization.display.sizesorter.SmoothingSizeSorter;

public class TestSizeSorter {

	@Test
	public void testSizeSorter() {
		int width = 20;
		List<CostedDimension> items = new ArrayList<CostedDimension>();
		items.add(new CostedDimension(width, 2, 0));
		items.add(new CostedDimension(width, 5, 0));
		items.add(new CostedDimension(width, 1, 0));
		items.add(new CostedDimension(width, 1, 0));
		items.add(new CostedDimension(width, 7, 0));
		items.add(new CostedDimension(width, 3, 0));
		items.add(new CostedDimension(width, 4, 0));
		items.add(new CostedDimension(width, 2, 0));
		items.add(new CostedDimension(width, 6, 0));
		items.add(new CostedDimension(width, 1, 0));
		items.add(new CostedDimension(width, 1, 0));
		items.add(new CostedDimension(width, 4, 0));
		
		SizeSorter ss = new BasicSizeSorter();
		
		Assert.assertEquals(12, (int) ss.getOptimalColumnSize(4, items, 10).getHeight());
		Assert.assertEquals(9, (int) ss.getOptimalColumnSize(5, items, 10).getHeight());
		
		SizeSorter sss = new SmoothingSizeSorter();

		Assert.assertEquals(12, (int) sss.getOptimalColumnSize(4, items, 10).getHeight());
		Assert.assertEquals(8, (int) sss.getOptimalColumnSize(5, items, 10).getHeight());

	}
	
}
