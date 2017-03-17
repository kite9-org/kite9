package org.kite9.diagram.visualization.planarization;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Diagram;

/**
 * Creates an initial {@link Planarization} of the graph, upon which transforms are applied.
 * 
 * @author robmoffat
 * 
 */
public interface PlanarizationBuilder {

    /**
     * Planarization of a hierarchically contained arrangement of {@link Connected}s
     */
    public abstract Planarization planarize(Diagram c);

}