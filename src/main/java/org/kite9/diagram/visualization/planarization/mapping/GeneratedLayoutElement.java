package org.kite9.diagram.visualization.planarization.mapping;

import org.kite9.diagram.common.elements.AbstractBiDirectional;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.framework.logging.LogicException;

/**
 * This connection is used with a {@link ContainerLayoutEdge} and is used to create a layout between
 * two elements of a container.  
 */
public class GeneratedLayoutElement extends AbstractBiDirectional<Connected> implements DiagramElement {
	
	public GeneratedLayoutElement(Connected from, Connected to, Direction drawDirection) {
		super(from, to, drawDirection);
	}

	@Override
	public String toString() {
		return "cle-"+getID();
	}

	@Override
	public int compareTo(DiagramElement o) {
		if (o instanceof AbstractBiDirectional<?>) {
			return this.getID().compareTo(((AbstractBiDirectional<?>) o).getID());
		} else {
			return -1;
		}
	}
}