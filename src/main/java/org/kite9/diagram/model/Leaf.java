package org.kite9.diagram.model;

import java.awt.geom.Rectangle2D;

/**
 * An item which is not a container of further connected items. (i.e. not a container)
 * 
 * e.g. Text, static svg or scalable shapes.
 * 
 * @see Container
 * 
 * @author robmoffat
 *
 */
public interface Leaf extends Rectangular {
	
	public Rectangle2D getBounds();

}
