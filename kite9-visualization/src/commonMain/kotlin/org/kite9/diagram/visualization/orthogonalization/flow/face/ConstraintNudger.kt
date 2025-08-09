package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.orthogonalization.flow.face.ConstraintGroup
import org.kite9.diagram.visualization.orthogonalization.flow.face.SubdivisionNode

/**
 * Nudges the flow graph so that extra constraints are met.
 *
 * Adds the constraints to the flow graph one at a time. This is done by
 * subdividing the faces (which are modelled using [SubdivisionNode]'s
 * further and further down, to model the fact that we want to constrain the
 * number of edges within a portion or sets of portions.
 *
 * A face starts off with 4 (inner face) or -4 corners from the main
 * [AbstractFlowOrthogonalizer] run.  To introduce a constraint to the
 * face, the portions in the face are divided into a clockwise and
 * anti-clockwise pair, both with a known number of edges.
 *
 * We compare the number of edges with the number required, and then pump new
 * edges into one face-side and out of the other.
 *
 * The way the faces are split is very important: a face subdivision can only be
 * subdivided further if it contains both clockwise and anti-clockwise portions.
 * Otherwise, it is constant for that nudge.
 *
 * One important consideration is how many corners to pump into a subdivision:
 * clearly to create a certain angle between two edges you can add or reduce the
 * number of corners. So for any given constraint, you increase or decrease. For
 * n constraints this means 2^n potential paths. To reduce this, we tackle
 * constraints in order, dealing with single corners and face-bound routes.
 *
 * Should it be impossible to nudge in one direction (e.g. adding corners), we
 * try to nudge in the other (e.g. remove them).
 *
 * @author robmoffat
 */
interface ConstraintNudger {

    fun processNudges(fg: MappedFlowGraph, constraints: ConstraintGroup, faces: MutableCollection<SubdivisionNode>)
}