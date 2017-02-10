package org.kite9.diagram.visualization.batik;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Stack;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.DOMTreeManager;
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
public class ElementManagingSVGGraphics2D extends SVGGraphics2D implements GroupManagement {

	static final class ExposingDOMGroupManager extends DOMGroupManager {

		public ExposingDOMGroupManager(GraphicContext gc, DOMTreeManager domTreeManager) {
			super(gc, domTreeManager);
		}
		
		public Element getCurrentGroup() {
			return currentGroup;
		}
	}
	
	private Element realTopLevelGroup;
	private Deque<Element> currentSubgroup = new ArrayDeque<>();

	public ElementManagingSVGGraphics2D(Document doc) {
		super(doc);
		this.realTopLevelGroup = getTopLevelGroup();
	}
	

	@Override
	public void createGroup(String id) {
		Element newGroup = realTopLevelGroup.getOwnerDocument().createElement("g");
		realTopLevelGroup.appendChild(newGroup);
		newGroup.setAttribute("id", id);
		this.currentSubgroup.push(newGroup);
		setTopLevelGroup(newGroup);
	}
	
	public void finishGroups() {
		
	}


	public void finishGroup(String id) {
		Element top = currentSubgroup.pop();
		if (!id.equals(top.getAttribute("id"))) {
			throw new Kite9ProcessingException("Was expecting current group with id: "+id);
		}
		
		if (currentSubgroup.size() == 0) {
			setTopLevelGroup(realTopLevelGroup);
		} else {
			setTopLevelGroup(currentSubgroup.peek());
		}
		
	}
	
}
