package org.kite9.diagram.dom.painter;

import org.kite9.diagram.common.fraction.LongFraction;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.dom.model.HasSVGRepresentation;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.*;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.Arrays;


/**
 * Base class for painter implementations
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractPainter implements Painter {

	protected DiagramElement r;

	@Override
	public void setDiagramElement(DiagramElement de) {
		this.r = de;
	}

	protected void addAttributes(Element toUse, Element out) {
		copyAttributes(toUse, out);
		
		String id = r.getID();
		if (id.length() > 0) {
			out.setAttribute("id", id);
		}
	
		addInfoAttributes(out);
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

	protected void addInfoAttributes(Element out) {
		StringBuilder debug = new StringBuilder();
		if (r instanceof Rectangular) {
			debug.append("position: "+ ((Rectangular) r).getContainerPosition()+"; ");
		} 
		if (r instanceof SizedRectangular) {
			SizedRectangular sr = (SizedRectangular) r;
			debug.append("margin: "+sr.getMargin(Direction.UP)+" "+sr.getMargin(Direction.RIGHT)+" "
					+sr.getMargin(Direction.DOWN)+" "+sr.getMargin(Direction.LEFT)+"; ");
			debug.append("padding: "+sr.getPadding(Direction.UP)+" "+sr.getPadding(Direction.RIGHT)+" "
					+sr.getPadding(Direction.DOWN)+" "+sr.getPadding(Direction.LEFT)+"; ");
			debug.append("sizing: " + ((SizedRectangular) r).getSizing(false) + " " + ((SizedRectangular) r).getSizing(true) + "; ");
			
		}
		if (r instanceof AlignedRectangular) {
			debug.append("horiz: "+ ((AlignedRectangular) r).getHorizontalAlignment()+"; ");
			debug.append("vert: "+ ((AlignedRectangular) r).getVerticalAlignment()+"; ");
		}
		if (r instanceof Container) {
			debug.append("layout: " + ((Container) r).getLayout() + "; ");
			if (((Container)r).getLayout() == Layout.GRID) {
				RectangleRenderingInformation rri = (RectangleRenderingInformation) r.getRenderingInformation();
				debug.append("grid-size: ["+rri.gridXSize()+", "+rri.gridYSize()+"]; ");
				debug.append("cell-xs: ["+commaIntList(rri.getCellXPositions())+"]; ");
				debug.append("cell-ys: ["+commaIntList(rri.getCellYPositions())+"]; ");
			}
		}
		
		if (r instanceof Connected) {
			RectangleRenderingInformation rri = (RectangleRenderingInformation) r.getRenderingInformation();
			if (r.getParent() instanceof Container) {
				Container parent = (Container) r.getParent();
				RectangleRenderingInformation prri = parent.getRenderingInformation();
				Layout l = parent.getLayout();
				if (l == Layout.GRID) {
					Pair<Integer> scaledX = scale(rri.gridXPosition(), prri.gridXSize());
					Pair<Integer> scaledY = scale(rri.gridYPosition(), prri.gridYSize());
					debug.append("grid-x: "+scaledX+"; "); 
					debug.append("grid-y: "+scaledY+"; "); 
				}
			}
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
			debug.append("link: ['"+link.getFrom().getID()+"','"+link.getTo().getID()+"']; ");
			debug.append("direction: "+((Connection)r).getDrawDirection()+"; ");
			if (((Connection)r).getRenderingInformation().isContradicting()) {
				debug.append("contradicting: yes; ");
			}
		}
		
		if (r instanceof Rectangular) {
			String usage = getUsage((Rectangular) r);
			debug.append("rectangular: "+usage+"; ");
		}
			
		if (r instanceof Rectangular) {
			RectangleRenderingInformation rri = ((Rectangular) r).getRenderingInformation();
			debug.append("rect-pos: "+rri.getPosition()+"; ");
			debug.append("rect-size: "+rri.getSize()+ "; "); 
		}
		
		if (r instanceof Temporary) {
			debug.append("temporary: true; ");
		}
		
		out.setAttribute("k9-info", debug.toString());
	}

	private String commaIntList(double[] p) {
		return Arrays.stream(p).mapToObj(d -> "" + ((int) d)).reduce((a, b) -> a+", "+b).orElse("");
	}

	private Pair<Integer> scale(OPair<LongFraction> p, int s) {
		LongFraction a = p.getA();
		LongFraction as = a.multiply(s);
		LongFraction b = p.getB();
		LongFraction bs = b.multiply(s);
		return new Pair<Integer>(as.intValue(), bs.intValue());
	}

	private String getUsage(Rectangular rect) {
		if (rect instanceof Diagram) {			
			return "diagram";
		}else if (rect instanceof Decal) {
			return "decal";
		} else if (rect instanceof Connected) {
			return "connected";
		} else if (rect instanceof Label) {
			return "label";
		} else if (rect instanceof Terminator) {
			return "terminator";
		} else {
			return "unknown";
		}
	}

	/**
	 * Outputs any SVG-renderable temporary elements to the output.
	 */
	protected void handleTemporaryElements(Element out, Document d, XMLProcessor postProcessor) {
		if (r instanceof Container) {
			((Container) r).getContents().stream()
				.filter(c -> c instanceof Temporary)
				.filter(c -> c instanceof HasSVGRepresentation) 
				.forEach(c -> {
					Element e = ((HasSVGRepresentation)c).output(d, postProcessor);
					if (e != null) {
						e.setAttribute("k9-elem", "_temporary");
						e.setAttribute("id", c.getID());
						out.appendChild(e);
					}
				});
		}
	}

}