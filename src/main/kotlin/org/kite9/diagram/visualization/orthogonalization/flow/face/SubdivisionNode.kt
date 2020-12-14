package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.fg.SimpleNode
import org.kite9.diagram.logging.LogicException

/**
 * Used for modelling faces and parts of faces.  A subdivision node represents a corner constraint in the following ways:
 *
 *  1. To begin with, it is used to represent a face.  A face has 4 corners leaving it, and consists of 1 or more portions
 * determined by the constraint system.
 *  1. In the nudging phase, the sources and sinks are linked to subdivisions.  The subdivision ensures that each face still has
 * only four corners leaving it, but these faces can be subdivided so that you could say, portion 1 + 2 must have only 2 corners
 * leaving them.
 *  1. As more constrains are added to the system, the existing face constraints are subdivided further and further, until
 * all constraints are added.
 *
 *
 *
 * @author robmoffat
 */
internal class SubdivisionNode(id: String, supply: Int) : SimpleNode(id, supply, null) {

    var subdivision = ""

    /**
     * Splits the face in two, with the different arcs leading to the portions described in
     * a and b.
     */
    fun split(aParts: List<PortionNode>, bParts: List<PortionNode>, constraintNo: Int): SubdivisionNode {
        var aflow = 0
        var bflow = 0
        val nodeb = SubdivisionNode(getID() + ":" + constraintNo, 0)
        val iterator = arcs.iterator()
        while (iterator.hasNext()) {
            val a = iterator.next()
            val to = a.otherEnd(this)
            if (bParts.contains(to)) {
                // need to migrate
                bflow += a.getFlowFrom(this)
                if (a.from === this) {
                    a.from = nodeb
                } else if (a.to === this) {
                    a.to = nodeb
                } else {
                    throw LogicException("Arc doesn't meet with")
                }
                nodeb.arcs.add(a)
                iterator.remove()
            } else if (!aParts.contains(to)) {
                throw LogicException("A parts should contain this!")
            } else {
                aflow += a.getFlowFrom(this)
            }
        }


        // fix up flow info
        supply = -aflow
        flow = aflow
        nodeb.supply = -bflow
        nodeb.flow = bflow
        return nodeb
    }

    fun meets(portions: Collection<PortionNode>): Boolean {
        for (a in arcs) {
            val otherEnd = a.otherEnd(this)
            if (portions.contains(otherEnd)) return true
        }
        return false
    }

    init {
        type = ConstrainedFaceFlowOrthogonalizer.FACE_SUBDIVISION_NODE
    }
}