package org.kite9.diagram.visualization.display.components;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.ContainerProperty;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.BasicRenderingInformation;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.primitives.CompositionalDiagramElement;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.TextContainingDiagramElement;
import org.kite9.diagram.style.StyledDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.io.StaticStyle;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;

/**
 * Extends the idea of the content box model to allow content to be displayed in 
 * a grid.  This works out the size of each column in the grid, and then arranges the content 
 * correctly within that.
 * 
 * @author robmoffat
 *
 */
public class KeyDisplayer extends AbstractTextWithContentBoxModelDisplayer {
	
	public KeyDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}
		
	private SizeSorter sorter = new SmoothingSizeSorter();
	
	@Override
	public Dimension2D sizeContent(DiagramElement de, Dimension2D within) {
		return sizeOrDrawContent(0, 0, de, within.getWidth(), within.getHeight(), false);
	}

	@Override
	protected boolean applyBoxContentCentering() {
		return false;
	}

	@Override
	public void drawContent(double x, double y, DiagramElement de, Dimension2D dim) {
		sizeOrDrawContent(x, y, de, dim.getWidth(), dim.getHeight(), true);
	}
	
	public CostedDimension sizeOrDrawContent(double x, double y, DiagramElement element, double width, double height, boolean draw) {
		List<CompositionalDiagramElement> contents = getContents(element);
		Dimension2D within = new Dimension2D(width, height);

		if ((contents!=null) && (contents.size()>0)) {

			List<CostedDimension> symSizes = new ArrayList<CostedDimension>(contents.size());
			double maxWidth = 0;
			double totalHeight = 0;
			for (DiagramElement s : contents) {
				CostedDimension symSize = parent.size(s, within);
				maxWidth = Math.max(symSize.getWidth(), maxWidth);
				symSizes.add(symSize);
				totalHeight += symSize.getHeight();
			}
		
			int maxColumns = getMaxColumnCount(maxWidth, width, StaticStyle.getKeyInternalSpacing(), contents.size());
			maxColumns = Math.min(contents.size(), maxColumns);
			
			if (draw) {
				double availHeight = height;
				int currentColumn = 0;
				double pos = 0;
				maxColumns = getColumnsUsed(symSizes, availHeight);
				double actualColumnWidth = getColumnWidth(width, maxColumns, StaticStyle.getKeyInternalSpacing());
				
				for (int i = 0; i < symSizes.size(); i++) {
					double currentHeight = symSizes.get(i).getHeight();
					
					if (currentHeight  + pos > height ) {
						currentColumn ++;
						pos = 0;
					}
					
					Dimension2D from = new Dimension2D(x + (currentColumn * ( actualColumnWidth + StaticStyle.getKeyInternalSpacing() ) ), y + pos);
					Dimension2D size = new Dimension2D(actualColumnWidth, currentHeight);
					parent.draw(contents.get(i), new BasicRenderingInformation(from, size, null, null, true));
					
					pos += currentHeight;
				}
				return null;
			} else {
				CostedDimension columnSize = getOptimalColumnSize(maxColumns, symSizes, totalHeight, width);
				double height2 = columnSize.getHeight() > 0 ? columnSize.getHeight() : 0;
				CostedDimension symCost = new CostedDimension(maxWidth  * maxColumns + (StaticStyle.getKeyInternalSpacing() * (maxColumns -1)), height2, 0);
				return symCost;
			}
		} else {
			return CostedDimension.ZERO;
		}
	}
	
	private int getColumnsUsed(List<CostedDimension> symSizes, double availHeight) {
		double pos = 0;
		int currentColumn = 0;
		for (int i = 0; i < symSizes.size(); i++) {
			double currentHeight = symSizes.get(i).getHeight();
			
			if (currentHeight  + pos > availHeight ) {
				currentColumn ++;
				pos = 0;
			}
			
			pos += currentHeight;
		}
		
		return currentColumn+1;
	}
		
	private double getColumnWidth(double columnsWithin, int maxColumns, int internalPadding) {
		return (columnsWithin - ((maxColumns - 1) * internalPadding)) / maxColumns;
	}

	private CostedDimension getOptimalColumnSize(int maxColumns,
			List<CostedDimension> symSizes, double totalHeight, double width) {
		return sorter.getOptimalColumnSize(maxColumns, symSizes, width);
	}

	private int getMaxColumnCount(double maxWidth, double within, int internalPadding, int maxCols) {
		int cols = 1;
		while (((maxWidth * (cols+1)) + (cols * internalPadding) < within) && (cols < maxCols)) {
			cols ++;
		}
		
		return cols;
	}
	
	
	public boolean canDisplay(DiagramElement element) {
		if (shadow)
			return false;
		return element instanceof Key;
	}

	/**
	 * This supports both collections of symbols (legacy) and collections of text-lines
	 */
	@Override
	public List<CompositionalDiagramElement> getContents(DiagramElement de) {
		@SuppressWarnings("rawtypes")
		ContainerProperty<TextLine> syms = ((Key)de).getSymbols();
		List<CompositionalDiagramElement> out = new ArrayList<CompositionalDiagramElement>(syms == null ? 0 : syms.size());
		if (syms != null) {
			for (TextLine s : syms) {
				out.add(s);
			}
		}
		
		return out;
	}
	
	@Override
	public TextContainingDiagramElement getLabel(DiagramElement de) {
		return ((Key)de).getBodyText();
	}

	@Override
	public ContainerProperty<Symbol> getSymbols(DiagramElement de) {
		return null;
	}

	@Override
	public TextContainingDiagramElement getStereotype(DiagramElement de) {
		return ((Key)de).getBoldText();
	}

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle((StyledDiagramElement) de);
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return new RoundedRectFlexibleShape(0, 0, 0);
	}
	
};
