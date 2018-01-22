package org.kite9.diagram.model.position;

public class RectangleRenderingInformationImpl extends AbstractRenderingInformationImpl implements RectangleRenderingInformation {
	
	public RectangleRenderingInformationImpl() {
		super();
	}
	
	public RectangleRenderingInformationImpl(Dimension2D pos, Dimension2D size, Direction orientation, boolean rendered) {
		setPosition(pos);
		setSize(size);
		setRendered(rendered);
	}

}
