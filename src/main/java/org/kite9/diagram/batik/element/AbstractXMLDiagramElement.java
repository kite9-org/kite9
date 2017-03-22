package org.kite9.diagram.batik.element;

import java.io.Serializable;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * Encapsulates an {@link StyledKite9SVGElement} as a {@link DiagramElement}.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractXMLDiagramElement extends AbstractDiagramElement implements DiagramElement, Serializable {
	
	protected StyledKite9SVGElement theElement;
	protected Kite9BridgeContext ctx;
	
	private boolean initialized = false;

	protected abstract void initialize();
	
	protected void ensureInitialized() {
		if (!initialized) {
			if (parent instanceof AbstractXMLDiagramElement) {
				((AbstractXMLDiagramElement)parent).ensureInitialized();
			}
			this.initialized = true;
			initialize();
		}
	}

	
	public StyledKite9SVGElement getTheElement() {
		return theElement;
	}

	public AbstractXMLDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(parent);
		this.theElement = el;
		this.ctx = ctx;
	}

	protected Value getCSSStyleProperty(String prop) {
		return theElement.getCSSStyleProperty(prop);
	}
	
	@Override
	public String getID() {
		return theElement.getID();
	}

	public String getShapeName() {
		String out = theElement.getAttribute("shape");
//		if (out.length() == 0) {
//			out = getCSSStyleProperty(CSSConstants.SHAPE_PROPERTY).getStringValue();
//		}
		
		return out;
	}

	public Diagram getDiagram() {
		if (this instanceof Diagram) {
			return (Diagram) this;
		} else {
			return ((AbstractXMLDiagramElement)getParent()).getDiagram();
		}
	}

	@Override
	public String toString() {
		String className = this.getClass().getName();
		className = className.substring(className.lastIndexOf(".")+1);
		return "["+theElement.getTagName()+":'"+getID()+"':"+className+"]";
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	
}