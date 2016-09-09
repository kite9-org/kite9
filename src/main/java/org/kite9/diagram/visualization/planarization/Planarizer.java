package org.kite9.diagram.visualization.planarization;

import org.kite9.diagram.adl.Diagram;

/**
 * Interface for creating a Planarization of a set of diagram attr suitable for further layout.
 * 
 * @author robmoffat
 *
 */
public interface Planarizer {

	public Planarization planarize(Diagram d);
	
}
