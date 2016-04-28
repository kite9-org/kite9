package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.util.List;

import org.kite9.diagram.position.CostedDimension;

public class BasicSizeSorter implements SizeSorter {

	@Override
	public CostedDimension getOptimalColumnSize(int maxColumns,
			List<CostedDimension> symSizes, double width) {
		
		int[] splits = new int[maxColumns+1];

		splitColumnContents(maxColumns, symSizes, splits);
		
		
		return getMaxHeight(maxColumns, symSizes, width, splits);
	}
	
	protected CostedDimension getMaxHeight(int maxColumns,
			List<CostedDimension> symSizes, double width, int[] splits) {
		float newMaxHeight = 0;
		for (int i = 0; i < maxColumns; i++) {
			int colHeight = sumHeight(splits[i], splits[i+1], symSizes);
			//System.out.println("Col "+i+" = "+colHeight);
			newMaxHeight = Math.max(newMaxHeight, colHeight);
		}
		
		return new CostedDimension(width, newMaxHeight, 0);
	}

	protected void splitColumnContents(int maxColumns,
			List<CostedDimension> symSizes, int[] splits) {
		//System.out.println(" --- ");

		float itemsPerColumn = ((float) symSizes.size())  / ((float) maxColumns);
		
		float maxHeight = 0;
		float colCount = 0;
		float currentHeight = 0;
		//int itemCount = 0;
		int colNo = 1;
		splits[0] = 0;
		
		for (int i = 0; i < symSizes.size(); i++) {
			float rem = colCount + 1f - itemsPerColumn;
			if (rem > .5) {
				colCount = rem - 1;
				//System.out.println(itemCount+ " " +currentHeight);
				currentHeight = 0;
				splits[colNo] = i; 
				colNo ++;
			} 
			
			//itemCount ++;
			colCount ++;
			currentHeight += symSizes.get(i).getHeight();
			
			maxHeight = currentHeight > maxHeight ? currentHeight : maxHeight;
		}
		
		splits[colNo] = symSizes.size();
		//System.out.println(itemCount+ " " +currentHeight);
	}
	
	protected int sumHeight(int from, int to, List<CostedDimension> symSizes) {
		int out = 0;
		for (int i = from; i < to; i++) {
			out += symSizes.get(i).getHeight();
		}
		
		return out;
	}

}
