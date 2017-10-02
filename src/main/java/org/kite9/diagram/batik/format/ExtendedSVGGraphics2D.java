package org.kite9.diagram.batik.format;

import java.awt.geom.Ellipse2D;
import java.util.Map;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.ImageHandlerBase64Encoder;
import org.apache.batik.svggen.SVGGraphics2D;
import org.kite9.diagram.batik.element.Templater;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Since we are constructing an SVG Document using a Graphics2D object, this
 * class allows us to add extra details in the XML that otherwise wouldn't be
 * part of the SVG.
 * 
 * @author robmoffat
 *
 */
public class ExtendedSVGGraphics2D extends SVGGraphics2D implements ExtendedSVG, Logable {

	private Element currentSubgroup;
	private Kite9Log log = new Kite9Log(this);
	private Templater templater;
	private BridgeContext ctx;

	public ExtendedSVGGraphics2D(Document doc, Templater templater, BridgeContext ctx) {
		super(doc,
			new ImageHandlerBase64Encoder(),
			new GradientExtensionHandlerBatik(), 
				false);
		this.currentSubgroup = getTopLevelGroup();
		this.templater = templater;
		this.ctx = ctx;
	}

	@Override
	public void createGroup(String id) {
		if (id != null) {
			Element newGroup = currentSubgroup.getOwnerDocument().createElement("g");
			currentSubgroup.appendChild(newGroup);
			newGroup.setAttribute("id", id);
			this.currentSubgroup = newGroup;
			setTopLevelGroup(newGroup);
			log.send("Started. Current group: "+this.currentSubgroup.getAttribute("id"));
		}
	}
	
	public void finishGroup(String id) {
		if (id != null) {
			if (!id.equals(currentSubgroup.getAttribute("id"))) {
				throw new Kite9ProcessingException("Was expecting current group with id: "+id);
			}
			
			Element parent = (Element) currentSubgroup.getParentNode();
			setTopLevelGroup(parent);
			this.currentSubgroup = parent;
			log.send("Finished.  Current group: "+this.currentSubgroup.getAttribute("id"));
		}
	}

	@Override
	public String getPrefix() {
		return "GRUP";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	@Override
	public void transcribeXML(Element e) {
		Element copy = (Element) e.cloneNode(true);
		Document thisDoc = getGeneratorContext().getDOMFactory();
		copy.setPrefix("");
		thisDoc.adoptNode(copy);
		
		domGroupManager.addElement(copy, DOMGroupManager.FILL);
	}
}
