package org.kite9.diagram.dom.painter;

import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class AbstractPainter implements Painter {

	private StyledKite9SVGElement theElement;
	protected DiagramElement r;
	private boolean performedPreprocess = false;
	private XMLProcessor preProcessor;

	public AbstractPainter(StyledKite9SVGElement theElement, XMLProcessor preProcessor) {
		super();
		this.theElement = theElement;
		this.preProcessor = preProcessor;
	}

	@Override
	public void setDiagramElement(DiagramElement de) {
		this.r = de;
	}

	protected void setGroupAttributes(StyledKite9SVGElement toUse, Element out) {
		String id = r.getID();
		if (id.length() > 0) {
			out.setAttribute("id", id);
		}
		out.setAttribute("kite9-elem", toUse.getTagName());
		addSizeAttributes(out);
	}

	private void addSizeAttributes(Element out) {
		if (r instanceof Rectangular) {
			RectangleRenderingInformation rri = ((SizedRectangular) r).getRenderingInformation();
			
			out.setAttribute("x", ""+rri.getPosition().x());
			out.setAttribute("y", ""+rri.getPosition().y());
			out.setAttribute("width", ""+rri.getSize().getWidth());
			out.setAttribute("height", ""+rri.getSize().getHeight());

		} 
		if (r instanceof AlignedRectangular) {
			out.setAttribute("horizontal-align",""+ ((AlignedRectangular) r).getHorizontalAlignment());
			out.setAttribute("vertical-align", ""+((AlignedRectangular) r).getVerticalAlignment());
		}
		if (r instanceof Container) {
			out.setAttribute("sizing", "" + ((Container) r).getSizing());
			out.setAttribute("layout",""+ ((Container) r).getLayout());
		}
		if (r instanceof Connected) {
			Container p = (Container) r.getParent();
			if ((p!= null) && (p.getLayout() == Layout.GRID)) {
				out.setAttribute("grid-x", ((Connected) r).getRenderingInformation().gridXPosition().toString());
				out.setAttribute("grid-y", ((Connected) r).getRenderingInformation().gridYPosition().toString());
			}
		}
		
		Dimension2D position = getRenderedRelativePosition(r);
		if (!position.equals(CostedDimension.ZERO)) {
			out.setAttribute("transform", "translate(" + position.x() + "," + position.y() + ")");
		}
	}

	/**
	 * Use this method to decorate the contents before processing.
	 */
	public StyledKite9SVGElement getContents() {		
		if (theElement == null) {
			throw new Kite9ProcessingException("Painter xml element not set");
		}
		
		if (r == null) {
			throw new Kite9ProcessingException("Painter diagram element not set");
		}
		
		if (performedPreprocess) {
			return theElement;
		}
		
		preProcessor.processContents(theElement);
		performedPreprocess = true;
		
		return theElement;
	}
	
	/**
	 * Returns the position as an offset from the nearest rectangular parent container. Useful for
	 * translate.
	 */
	protected Dimension2D getRenderedRelativePosition(DiagramElement de) {
		if (de instanceof Rectangular) {
			Dimension2D position = getOriginPosition();
			Dimension2D parentPosition = getParentOriginPosition();
			Dimension2D out = new Dimension2D(position.x() - parentPosition.x(), position.y() - parentPosition.y());
			return out;
		} else {
			return CostedDimension.ZERO;
		}
	}

	protected Dimension2D getOriginPosition() {
		if (r instanceof Rectangular) {
			RectangleRenderingInformation rri = ((Rectangular) r).getRenderingInformation();
			return rri.getPosition();
		}

		return CostedDimension.ZERO;
	}
	
	protected Dimension2D getParentOriginPosition() {
		DiagramElement parent = r.getParent();
		while ((parent != null) && (!(parent instanceof Rectangular))) {
			parent = parent.getParent();
		}
		
		if (parent instanceof Rectangular) {
			RectangleRenderingInformation rri = ((Rectangular) parent).getRenderingInformation();
			return rri.getPosition();
		}

		return CostedDimension.ZERO;
	}
	
}