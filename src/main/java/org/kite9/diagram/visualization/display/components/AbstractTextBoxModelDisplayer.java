package org.kite9.diagram.visualization.display.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.kite9.diagram.adl.ContainerProperty;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.primitives.Connection;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.TextContainingDiagramElement;
import org.kite9.diagram.style.StyledDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.FixedShape;
import org.kite9.diagram.visualization.display.style.TextStyle;
import org.kite9.diagram.visualization.display.style.io.StaticStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;

/**
 * Handles rendering and sizing of text objects within the diagram. 
 * 
 * Pieces of text generally have a label, symbols and a type.  Any of those pieces are optional.
 * 
 * These can be used as labels for containers or
 * connections, so there are various subclasses for those.
 * 
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractTextBoxModelDisplayer extends AbstractBoxModelDisplayer {

	public AbstractTextBoxModelDisplayer(GraphicsLayer g2) {
		super(g2);
	}

	public AbstractTextBoxModelDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	@Override
	protected void drawBoxContents(DiagramElement element, RectangleRenderingInformation r) {
		Rectangle2D ri = getDrawingRectangle(element, r);
//		g2.setColor(Color.GREEN);
//		g2.draw(ri);
		ContainerProperty<Symbol> symbols = getSymbols(element);
		String stereo = safeGetText(getStereotype(element));
		String label = safeGetText(getLabel(element));
		boolean syms = (symbols != null) && (symbols.size() > 0);
		double xStart = ri.getMinX();
		double yStart = ri.getMinY();
		TextStyle symStyle = syms ? getSymbolTextStyle(symbols.iterator().next()) : null;
		double symWidth = syms ? StaticStyle.getSymbolWidth() : 0;
		CostedDimension cd = CostedDimension.ZERO;
		
		// draw stereotype, and symbols next to it if there are any
		TextStyle ts = getTypeStyle(element);
		if (ts != null) {
			double baseline = Math.max(getBaseline(ts.getFont(), g2, stereo), syms ? getSymbolBaseline(ts.getFont(), symStyle.getFont(), g2) : 0);
			cd = arrangeString(ts.getFont(), ts.getColor(), stereo, new Dimension2D(xStart, yStart), new Dimension2D(ri.getWidth(), ri.getHeight()), true, syms ? Justification.LEFT : ts.getJust(), baseline);
			if (syms && (!cd.equals(CostedDimension.ZERO))) {
				drawSymbols(symbols, xStart, yStart, ri.getWidth(), baseline);
				yStart += Math.max(cd.getHeight(), symWidth);
				syms = false;
			} else {
				yStart += cd.getHeight();
			}
		}
		// draw label, and syms if they haven't already been done
		TextStyle ls = getLabelStyle(element);
		double baseline2 = Math.max(getBaseline(ls.getFont(), g2, label), syms ? getSymbolBaseline(ls.getFont(), symStyle.getFont(), g2) : 0);
//		g2.setColor(Color.RED);
//		g2.drawRect((int) xStart, (int) (yStart+baseline), 100, 1);
		cd = arrangeString(ls.getFont(), ls.getColor(), label, new Dimension2D(xStart, yStart), new Dimension2D(ri.getWidth(), ri.getHeight()), true, syms ? Justification.LEFT : ls.getJust(), baseline2);
		if (syms) {
			drawSymbols(symbols, xStart, yStart, ri.getWidth(), baseline2);
			yStart += Math.max(cd.getHeight(), symWidth) ;
		} else {
			yStart += cd.getHeight();
		}
	}
	
	/**
	 * @TODO: fix this.
	 */
	private TextStyle getSymbolTextStyle(Symbol symbol) {
		return new TextStyle(symbol);
	}

	private void drawSymbols(ContainerProperty<Symbol> syms, double x, double y, double w, double baseline) {
		Iterator<Symbol> it = syms.iterator();
		for (int i = 0; i < syms.size(); i++) {
			Symbol sym = it.next();
			FixedShape shape = new FixedShape(sym);
			drawSymbol(""+sym.getChar(), g2, x + w - ((i+1) * StaticStyle.getSymbolWidth()), y, shape, getSymbolTextStyle(sym).getFont(), baseline, Color.RED);
		}
	}
	
	public abstract TextContainingDiagramElement getLabel(DiagramElement de);
	
	public abstract ContainerProperty<Symbol> getSymbols(DiagramElement de);
	
	public abstract TextContainingDiagramElement getStereotype(DiagramElement de);
	
	public boolean hasContent(DiagramElement de) {
		ContainerProperty<Symbol> symbols = getSymbols(de);
		String stereo = safeGetText(getStereotype(de));
		boolean syms = (symbols != null) && (symbols.size() > 0);
		String label = safeGetText(getLabel(de));
		boolean lab = (label != null) && (label.length() > 0);
		boolean ster = (stereo != null) && (stereo.length() > 0);
		return syms || lab || ster;
	}
	
	public TextStyle getLabelStyle(DiagramElement de) {
		StyledDiagramElement sde = getLabel(de);
		return sde == null ? null : new TextStyle(sde);
	}
	
	public TextStyle getTypeStyle(DiagramElement de) {
		StyledDiagramElement sde = getStereotype(de);
		return sde == null ? null : new TextStyle(sde);
	}
	
	
	public boolean canDisplay(DiagramElement element) {
		return ((element instanceof TextLine) && ((TextLine)element).getParent() instanceof Connection);
	}

	@Override
	protected Dimension2D sizeBoxContents(DiagramElement e, Dimension2D within) {		
		//if (hasContent(e)) {
			TextStyle ts = getTypeStyle(e);
			TextStyle ls = getLabelStyle(e);
			
			double symbolWidth = StaticStyle.getSymbolWidth();
			CostedDimension typeSize = ts != null ? arrangeString(ts.getFont(), ts.getColor(), safeGetText(getStereotype(e)), 
					CostedDimension.ZERO, CostedDimension.UNBOUNDED, false, Justification.CENTER, 0) : CostedDimension.ZERO;
			CostedDimension nameSize = ls != null ? arrangeString(ls.getFont(), ls.getColor(), safeGetText(getLabel(e)), 
					CostedDimension.ZERO, CostedDimension.UNBOUNDED, false, Justification.CENTER, 0) : CostedDimension.ZERO;
			
			int syms = getSymbols(e)==null ? 0 : getSymbols(e).size();
			if (syms > 0) {
				CostedDimension symd = new CostedDimension(StaticStyle.getInterSymbolPadding() + (syms * symbolWidth), symbolWidth, 0);
				if (!typeSize.equals(CostedDimension.ZERO)) {
					FontMetrics fm = g2.getFontMetrics(ts.getFont());
					double descent = fm.getMaxDescent();
					typeSize = arrangeHorizontally(arrangeVertically(symd, new Dimension2D(0, descent)), typeSize);
				} else {
					FontMetrics fm = g2.getFontMetrics(ls.getFont());
					double descent = fm.getMaxDescent();
					nameSize = arrangeHorizontally(arrangeVertically(symd, new Dimension2D(0, descent)), nameSize);
				}
			
			}
			 
			if ((typeSize == CostedDimension.NOT_DISPLAYABLE) || (nameSize == CostedDimension.NOT_DISPLAYABLE))
				return CostedDimension.NOT_DISPLAYABLE;
			
			CostedDimension fullSize = arrangeVertically(typeSize, nameSize);
			return fullSize;
//		} else {
//			return CostedDimension.ZERO;
//		}
	}

	private String safeGetText(TextContainingDiagramElement st) {
		return st == null ? null : st.getText();
	}
	
	
	
}
