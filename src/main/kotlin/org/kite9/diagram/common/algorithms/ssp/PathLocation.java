/**
 * 
 */
package org.kite9.diagram.common.algorithms.ssp;

public interface PathLocation<X extends PathLocation<X>> extends Comparable<X> {
	
	public Object getLocation();
	
	public boolean isActive();
	
	public void setActive(boolean a);
	
}