package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.util.List;

import org.kite9.diagram.position.CostedDimension;

public interface SizeSorter {

	public CostedDimension getOptimalColumnSize(int maxColumns,
			List<CostedDimension> symSizes, double width);
}
