package org.kite9.diagram.visualization.display.sizesorter;

import java.util.List;

import org.kite9.diagram.model.position.CostedDimension;

public interface SizeSorter {

	public CostedDimension getOptimalColumnSize(int maxColumns,
			List<CostedDimension> symSizes, double width);
}
