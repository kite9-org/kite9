package org.kite9.diagram.dom.painter;

import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


/**
 * Base class for painter implementations
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class AbstractPainter implements Painter {

	private StyledKite9XMLElement theElement;
	protected DiagramElement r;
	private boolean performedPreprocess = false;
	private XMLProcessor processor;

	public AbstractPainter(StyledKite9XMLElement theElement, XMLProcessor processor) {
		super();
		this.theElement = theElement;
		this.processor = processor;
	}

	@Override
	public void setDiagramElement(DiagramElement de) {
		this.r = de;
	}

	protected void addAttributes(StyledKite9XMLElement toUse, Element out) {
		copyAttributes(toUse, out);
		
		String id = r.getID();
		if (id.length() > 0) {
			out.setAttribute("id", id);
		}
		
		out.setAttribute("kite9-elem", toUse.getTagName());
		out.setAttribute("class", toUse.getAttribute("class")+" kite9-"+toUse.getTagName());

		addDebugAttributes(out);
	}
	
	public void copyAttributes(Element from, Element to) {
        NamedNodeMap attributes = from.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Attr node = (Attr) attributes.item(i);
            if (!node.getName().equals("xmlns")) {
	            Attr copy = to.getOwnerDocument().createAttribute(node.getName());
	            copy.setNodeValue(node.getNodeValue());
	            to.setAttributeNode(copy);
            }
        }
    }

	private void addDebugAttributes(Element out) {
		StringBuilder debug = new StringBuilder();
		if (r instanceof SizedRectangular) {
			debug.append("positioning: "+ ((SizedRectangular) r).getContainerPosition().toString()+"; ");
		} 
		if (r instanceof AlignedRectangular) {
			debug.append("horiz: "+ ((AlignedRectangular) r).getHorizontalAlignment()+"; ");
			debug.append("vert: "+ ((AlignedRectangular) r).getVerticalAlignment()+"; ");
		}
		if (r instanceof Container) {
			debug.append("sizing: " + ((Container) r).getSizing() + "; ");
			debug.append("layout: " + ((Container) r).getLayout() + "; ");
		}
		if (r instanceof Terminator) {
			Connection link = ((Terminator) r).getConnection();
			boolean from = link.getDecorationForEnd(link.getFrom()) == r;
			debug.append("terminates: "+link.getID()+"; ");
			debug.append("terminates-at: "+(from ? link.getFrom().getID() : link.getTo().getID())+"; ");
			debug.append("end: "+(from ? "from; " : "to; ")); 
		}
		
		if (r instanceof Connection) {
			Connection link = ((Connection) r);
			debug.append("link: "+link.getFrom().getID()+" "+link.getTo().getID()+"; ");
			debug.append("direction: "+((Connection)r).getDrawDirection()+"; ");
		}
		
		if (r instanceof Connected) {
			debug.append("connect: tbc; ");
		}
			
		if (r instanceof Rectangular) {
			RectangleRenderingInformation rri = ((Rectangular) r).getRenderingInformation();
			debug.append("d-bounds: "+rri.getPosition()+" "+rri.getSize()+ "; "); 
		}
		
		
		out.setAttribute("debug", debug.toString());
	}

	/**
	 * Use this method to decorate the contents before processing.
	 */
	public StyledKite9XMLElement getContents() {		
		if (theElement == null) {
			throw new Kite9ProcessingException("Painter xml element not set");
		}
		
		if (r == null) {
			throw new Kite9ProcessingException("Painter diagram element not set");
		}
		
		if (performedPreprocess) {
			return theElement;
		}
		
		setupElementXML(theElement);
		processor.processContents(theElement);
		performedPreprocess = true;
		
		return theElement;
	}

	/**
	 * Ensures that the element has the correct contents before the pre-processor is called.
	 */
	protected void setupElementXML(StyledKite9XMLElement e) {
	}
	
}