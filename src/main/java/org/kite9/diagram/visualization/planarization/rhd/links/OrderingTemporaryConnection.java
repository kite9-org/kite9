package org.kite9.diagram.visualization.planarization.rhd.links;

import org.kite9.diagram.common.elements.AbstractBiDirectional;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.Container;

/**
 * Used for enforcing container ordering.
 * 
 * @author robmoffat
 *
 */
public class OrderingTemporaryConnection extends AbstractBiDirectional<Connected> {
	
	/**
	 * Container we are doing the ordering for
	 */
	Container c;

	public Container getContainerBeingOrdered() {
		return c;
	}

	public OrderingTemporaryConnection(Connected from, Connected to, Direction drawDirection, Container c) {
		super(from, to, drawDirection);
		this.c = c;
	}
	
	boolean required;

	public boolean isRequired() {
		return required;
	}

	/**
	 * Set required to false if we find a link that will do the same job
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

}
