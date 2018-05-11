package org.kite9.diagram.dom.model;

import java.io.Serializable;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;

/**
 * Encapsulates an {@link StyledKite9SVGElement} as a {@link DiagramElement}.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractDOMDiagramElement extends AbstractDiagramElement implements Serializable {
	
	protected StyledKite9SVGElement theElement;
	
	private boolean initialized = false;

	protected abstract void initialize();
	
	protected void ensureInitialized() {
		if (!initialized) {
			if (parent instanceof AbstractDOMDiagramElement) {
				((AbstractDOMDiagramElement)parent).ensureInitialized();
			}
			this.initialized = true;
			initialize();
		}
	}

	
	public StyledKite9SVGElement getTheElement() {
		return theElement;
	}

	public AbstractDOMDiagramElement(StyledKite9SVGElement el, DiagramElement parent) {
		super(parent);
		this.theElement = el;
	}

	public Value getCSSStyleProperty(String prop) {
		return theElement.getCSSStyleProperty(prop);
	}
	
	@Override
	public String getID() {
		return theElement.getID();
	}

	public Diagram getDiagram() {
		if (this instanceof Diagram) {
			return (Diagram) this;
		} else {
			return ((AbstractDOMDiagramElement)getParent()).getDiagram();
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