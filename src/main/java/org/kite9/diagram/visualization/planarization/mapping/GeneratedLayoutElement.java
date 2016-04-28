package org.kite9.diagram.visualization.planarization.mapping;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.AbstractBiDirectional;
import org.kite9.diagram.primitives.Connected;
import org.kite9.framework.logging.LogicException;

/**
 * This connection is used with a {@link ContainerLayoutEdge} and is used to create a layout between
 * two elements of a container.  
 */
public class GeneratedLayoutElement extends AbstractBiDirectional<Connected> {
	
	public GeneratedLayoutElement(Connected from, Connected to, Direction drawDirection) {
		super(from, to, drawDirection);
	}

	private static final long serialVersionUID = 4502186318625717675L;

	
	@Override
	public void setRenderingInformation(RenderingInformation ri) {
		throw new LogicException("Can't be rendered");
	}
	
	@Override
	public RenderingInformation getRenderingInformation() {
		return null;
	}

	@Override
	public String toString() {
		return "cle-"+getID();
	}
}