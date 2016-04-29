package org.kite9.diagram.visualization.display;


/**
 * This is an interface for something that knows how to display all the components
 * in the diagram.  i.e. a top-level displayer.
 * 
 * @author robmoffat
 *
 */
public interface CompleteDisplayer extends Displayer, DiagramSpacer {

	/**
	 * Called at the end to complete rendering and return a result, if any
	 */
	public void finish();
	
}
