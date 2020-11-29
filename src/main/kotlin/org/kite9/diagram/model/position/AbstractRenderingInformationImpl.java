package org.kite9.diagram.model.position;

import java.util.List;

import org.w3c.dom.Element;

public abstract class AbstractRenderingInformationImpl implements RenderingInformation {
	
	private List<Element> displayData;
	private boolean rendered = true;
	private Dimension2D position;
	private Dimension2D size;
	
	public List<Element> getDisplayData() {
		return displayData;
	}

	public void setDisplayData(List<Element> displayData) {
		this.displayData = displayData;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean r) {
		this.rendered = r;
	}
	
	public Dimension2D getPosition() {
		return this.position;
	}

	public void setPosition(Dimension2D position) {
		this.position = position;
	}

	public Dimension2D getSize() {
		return this.size;
	}

	public void setSize(Dimension2D size) {
		this.size = size;
	}
}
