package org.kite9.diagram.visualization.batik.format;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Since we are constructing an SVG Document using a Graphics2D object, this
 * class allows us to add extra details in the XML that otherwise wouldn't be
 * part of the SVG.
 * 
 * @author robmoffat
 *
 */
public class GroupManagingSVGGraphics2D extends SVGGraphics2D implements GroupManagement {

	private Element currentSubgroup;

	public GroupManagingSVGGraphics2D(Document doc) {
		super(SVGGeneratorContext.createDefault(doc), false);
		this.currentSubgroup = getTopLevelGroup();
	}

	@Override
	public void createGroup(String id) {
		if (id != null) {
			Element newGroup = currentSubgroup.getOwnerDocument().createElement("g");
			currentSubgroup.appendChild(newGroup);
			newGroup.setAttribute("id", id);
			this.currentSubgroup = newGroup;
			setTopLevelGroup(newGroup);
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
		}
	}
	
}
