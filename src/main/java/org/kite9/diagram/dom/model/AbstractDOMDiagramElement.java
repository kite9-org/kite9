package org.kite9.diagram.dom.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates an {@link StyledKite9XMLElement} as a {@link DiagramElement}.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractDOMDiagramElement extends AbstractDiagramElement implements Serializable, HasSVGRepresentation {
	
	private StyledKite9XMLElement theElement;
	
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
	
	public abstract Painter getPainter();

	public AbstractDOMDiagramElement(StyledKite9XMLElement el, DiagramElement parent) {
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

	@Override
	public Element output(Document d) {
		if (getRenderingInformation().isRendered()) {
			ensureInitialized();
			Element out = paintElementToDocument(d);
			return out;
		} else {
			return null;
		}
	}

	protected abstract Element paintElementToDocument(Document d);


	/**
	 * For elements which are containers, call this method as part of initialize.
	 */
	protected List<DiagramElement> initContents() {
		List<DiagramElement> contents = new ArrayList<>();
		for (Kite9XMLElement xmlElement : getPainter().getContents()) {
			DiagramElement de = xmlElement.getDiagramElement();			
			if (de instanceof Connection) {
				// doesn't get added.
			} else if (de != null) { 
				contents.add(de);
			} 
		}
		
		return contents;
	}
}