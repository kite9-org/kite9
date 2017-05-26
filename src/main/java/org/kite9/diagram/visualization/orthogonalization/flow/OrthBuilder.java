package org.kite9.diagram.visualization.orthogonalization.flow;

import org.kite9.diagram.visualization.orthogonalization.OrthogonalizationImpl;
import org.kite9.diagram.visualization.planarization.Planarization;

/**
 * Converts an optimised flow graph and planarization into an orthogonal model.
 * 
 * @author robmoffat
 *
 */
public interface OrthBuilder {

	public abstract OrthogonalizationImpl build(Planarization pln);

}