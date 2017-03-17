package org.kite9.diagram.visualization.display.sizesorter;

import java.util.List;

import org.kite9.diagram.model.position.CostedDimension;

public class SmoothingSizeSorter extends BasicSizeSorter {

	@Override
	public CostedDimension getOptimalColumnSize(int maxColumns,
			List<CostedDimension> symSizes, double width) {

		int[] splits = new int[maxColumns+1];

		splitColumnContents(maxColumns, symSizes, splits);
		boolean moved = false;
		
		do {
			moved = false;
			for (int i = 0; i < maxColumns-1; i++) {
				moved =  considerMove(i, i+1, symSizes, splits) || moved;
			}
		} while (moved);
		
		return getMaxHeight(maxColumns, symSizes, width, splits);
	}

	protected boolean considerMove(int a, int b, List<CostedDimension> symSizes, int[] splits) {
		int initialA = sumHeight(splits[a], splits[a+1], symSizes);
		int initialB = sumHeight(splits[b], splits[b+1], symSizes);
		int initM = Math.max(initialA, initialB);
		//System.out.println("checking "+a+" ("+initialA+") "+b+" ("+initialB+")");
		
		// increase a
		int incAA = sumHeight(splits[a], splits[a+1]+1, symSizes);
		int incAB = sumHeight(splits[b]+1, splits[b+1], symSizes);
		int incAM = Math.max(incAA, incAB);
		//System.out.println("inc A "+a+" ("+incAA+") "+b+" ("+incAB+")");

		
		if (incAM < initM) {
			splits[a+1] ++;
			return true;
		}
		
		// increase b
		int incBA = sumHeight(splits[a], splits[a+1]-1, symSizes);
		int incBB = sumHeight(splits[b]-1, splits[b+1], symSizes);
		int incBM = Math.max(incBA, incBB);
		//System.out.println("inc B "+a+" ("+incBA+") "+b+" ("+incBB+")");

		
		if (incBM < initM) {
			splits[b] --;
			return true;
		}
	
		return false;
	}
	
}
