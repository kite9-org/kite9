package org.kite9.diagram.visualization.compaction;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.style.impl.AbstractDiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;

/**
 * Just keeps track of Segments related to grid contents.
 */
public class GridContentsDiagramElement extends AbstractDiagramElement implements DiagramElement {

	public GridContentsDiagramElement(Container parent) {
		super(parent);
	}

	@Override
	public String getID() {
		return parent.getID()+"-grid-contents";
	}

	@Override
	public RenderingInformation getRenderingInformation() {
		return null;
	}

	@Override
	public Value getCSSStyleProperty(String prop) {
		return parent.getCSSStyleProperty(prop);
	}

	@Override
	public void setRenderingInformation(RenderingInformation ri) {
		throw new Kite9ProcessingException();
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	@Override
	public String getShapeName() {
		return null;
	}

	@Override
	public String toString() {
		return "[GridContentsDiagramElement:"+getID()+"]";
	}

	
}
