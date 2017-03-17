package org.kite9.diagram.position;

import java.util.List;

import org.w3c.dom.Element;

public abstract class AbstractRenderingInformationImpl implements RenderingInformation {
	
	private List<Element> displayData;
	private boolean rendered = true;
	private Dimension2D position;
	private Dimension2D size;
	private Dimension2D internalSize;
	
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
	
	public Dimension2D getInternalSize() {
		return this.internalSize;
	}

	public void setInternalSize(Dimension2D size) {
		this.internalSize = size;
	}

}
