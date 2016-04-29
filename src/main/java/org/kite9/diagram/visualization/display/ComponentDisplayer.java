package org.kite9.diagram.visualization.display;

import org.kite9.diagram.visualization.format.GraphicsLayer;

/**
 * Defines a pluggable framework for visualizing diagram attr, given a
 * width-constrained drawing system, and for getting the sizes of the diagrams
 * given these constraints.
 * 
 * @author robmoffat
 *
 */
public interface ComponentDisplayer extends Displayer {
	
	public GraphicsLayer getLayer();
}
