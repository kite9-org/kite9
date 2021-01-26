package org.kite9.diagram.visualization.orthogonalization.vertex;

import org.kite9.diagram.visualization.orthogonalization.DartFace;

import java.util.List;

/**
 * Contains part of the overall vertex construction, between one incoming vertex and the next.
 *
 * @author robmoffat
 */
class Boundary {

    public Boundary(ExternalVertex from, ExternalVertex to, List<DartFace.DartDirection> toInsert) {
        super();
        this.from = from;
        this.to = to;
        this.toInsert = toInsert;
    }

    final ExternalVertex from, to;
    final List<DartFace.DartDirection> toInsert;

    public String toString() {
        return "Boundary [from=" + from + ", to=" + to + "]";
    }
}
