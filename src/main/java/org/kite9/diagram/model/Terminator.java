package org.kite9.diagram.model;

/**
 * Describes what's at the end of a {@link Connection}.
 * 
 * Terminators don't get involved in the usual compaction process (like Decals).
 * They can overlap Connecteds and Connections that they are part of.
 * 
 * @author robmoffat
 *
 */
public interface Terminator extends SizedRectangular {

	/**
	 * Amount of length along the axis of the link that the terminator will take up.
	 */
	double getReservedLength();
		
	/**
	 * The part of the connection, from the end inwards, that doesn't need to be 
	 * drawn because the marker will draw it instead.
	 */
	double getMarkerReserve();
}
