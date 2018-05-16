package org.kite9.diagram.dom.painter;

import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.Layout;
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
	private XMLProcessor processor;

	public AbstractPainter(StyledKite9SVGElement theElement, XMLProcessor processor) {
		super();
		this.theElement = theElement;
		this.processor = processor;
	}

	@Override
	public void setDiagramElement(DiagramElement de) {
		this.r = de;
	}

	protected void addAttributes(StyledKite9SVGElement toUse, Element out) {
		String id = r.getID();
		if (id.length() > 0) {
			out.setAttribute("id", id);
		}
		out.setAttribute("kite9-elem", toUse.getTagName());
		addDebugAttributes(out);
	}

	private void addDebugAttributes(Element out) {
		StringBuilder debug = new StringBuilder();
		if (r instanceof SizedRectangular) {
			debug.append("position: "+ ((SizedRectangular) r).getContainerPosition().toString()+"; ");
		} 
		if (r instanceof AlignedRectangular) {
			debug.append("horiz: "+ ((AlignedRectangular) r).getHorizontalAlignment()+"; ");
			debug.append("vert: "+ ((AlignedRectangular) r).getVerticalAlignment()+"; ");
		}
		if (r instanceof Container) {
			debug.append("sizing: " + ((Container) r).getSizing() + "; ");
			debug.append("layout: " + ((Container) r).getLayout() + "; ");

			if (((Container) r).getLayout() == Layout.GRID) {
				debug.append("grid-x: " + ((Container) r).getGridColumns() + "; ");
				debug.append("grid-y: " + ((Container) r).getGridRows() + "; ");
			}
		}
		
		out.setAttribute("debug", debug.toString());
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
		
		processor.processContents(theElement);
		performedPreprocess = true;
		
		return theElement;
	}
	
}