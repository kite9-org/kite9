package org.kite9.diagram.visualization.batik.node;

import org.apache.batik.gvt.GraphicsNode;

/**
 * Marker interface to say that the element inside is managed and sized by Kite9 
 * diagram layout.
 * 
 * @author robmoffat
 *
 */
public interface Kite9SizedGraphicsNode extends GraphicsNode {

	/**
	 * This is called to set the size that this element should have, by Kite9.
	 * Ordinarily, each array will be 1-length, but for grids they will be larger. 
	 *
	 */
	public void setSize(double[] x, double[] y);
}
