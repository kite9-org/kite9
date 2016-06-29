package org.kite9.diagram.visualization.display.components;

import java.util.List;

import org.kite9.diagram.position.BasicRenderingInformation;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.primitives.CompositionalDiagramElement;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.format.GraphicsLayer;

/**
 * Extends the notion of a Text Box Model to have heading text and then some content below it
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractTextWithContentBoxModelDisplayer extends AbstractTextBoxModelDisplayer {

	public AbstractTextWithContentBoxModelDisplayer(CompleteDisplayer parent, GraphicsLayer g2, Stylesheet ss, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	public AbstractTextWithContentBoxModelDisplayer(GraphicsLayer g2, Stylesheet ss) {
		super(g2, ss);
	}
	
	public abstract List<CompositionalDiagramElement> getContents(DiagramElement de);
	
	
	
	@Override
	public void drawBoxContents(DiagramElement element, RectangleRenderingInformation r) {
		// do top half of box
		super.drawBoxContents(element, r);
		
		List<CompositionalDiagramElement> contents = getContents(element);
		
		if ((contents!=null) && (contents.size()>0)) {
			Dimension2D size = super.sizeBoxContents(element, new Dimension2D(r.getSize().getWidth(), r.getSize().getHeight()));
			drawContent(r.getPosition().x(), r.getPosition().y() + size.getHeight(), element, new Dimension2D(r.getSize().getWidth(), r.getSize().getHeight() - size.getHeight()));
		}
	}

	
	public Dimension2D sizeContent(DiagramElement de, Dimension2D within) { 
		List<CompositionalDiagramElement> tl = getContents(de);
		Dimension2D result = new CostedDimension();
		if ((tl!= null) && (tl.size()>0)) {
			for (DiagramElement line : tl) {
				Dimension2D out = parent.size(line, CostedDimension.UNBOUNDED);
				result = arrangeVertically(result, out);
			}
		}
		
		return result;
	}
	
	public void drawContent(double x, double y, DiagramElement t, Dimension2D dim) { 
		for (DiagramElement line : getContents(t)) {
			Dimension2D size = parent.size(line, dim);
			RectangleRenderingInformation ri = new BasicRenderingInformation(
					new Dimension2D(x, y), 
					new Dimension2D(dim.getWidth(), size.getHeight()),
					null, null, true);
			parent.draw(line, ri);
			y += size.getHeight();
		}
	}
	
	
	@Override
	protected Dimension2D sizeBoxContents(DiagramElement e, Dimension2D within) {
		Dimension2D topPart = super.sizeBoxContents(e, within);
		Dimension2D bottomPart = sizeContent(e, new Dimension2D(Math.max(within.getWidth(), topPart.getWidth()), within.getHeight() - topPart.getHeight()));
		
		if (bottomPart.getHeight() > 0) {
			return arrangeVertically(topPart, bottomPart);
		} else {
			return topPart;
		}
	}

}
