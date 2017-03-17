package org.kite9.diagram.position;

public class RectangleRenderingInformationImpl extends AbstractRenderingInformationImpl implements RectangleRenderingInformation {
	
	public RectangleRenderingInformationImpl() {
		super();
	}
	
	public RectangleRenderingInformationImpl(Dimension2D pos, Dimension2D size, HPos hj, VPos vj, boolean rendered) {
		setPosition(pos);
		setSize(size);
		setHorizontalJustification(hj);
		setVerticalJustification(vj);
		setRendered(rendered);
	}

	public

	boolean multipleHorizontalLinks;
	boolean multipleVerticalLinks;

	public boolean isMultipleHorizontalLinks() {
		return multipleHorizontalLinks;
	}

	public void setMultipleHorizontalLinks(boolean multipleHorizontalLinks) {
		this.multipleHorizontalLinks = multipleHorizontalLinks;
	}

	public boolean isMultipleVerticalLinks() {
		return multipleVerticalLinks;
	}

	public void setMultipleVerticalLinks(boolean multipleVerticalLinks) {
		this.multipleVerticalLinks = multipleVerticalLinks;
	}
	
	private HPos hpos;
	private VPos vpos;
	
	public HPos getHorizontalJustification() {
		return hpos;
	}

	public void setHorizontalJustification(HPos horizontalJustification) {
		this.hpos = horizontalJustification;
	}

	public VPos getVerticalJustification() {
		return vpos;
	}

	public void setVerticalJustification(VPos verticalJustification) {
		this.vpos = verticalJustification;
	}

}
