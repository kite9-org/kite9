package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.visualization.planarization.Face

class ConstraintGroup(private val fixedConstraints: Set<Edge>, private val faceCount: Int) {

    private val floatingConstraints: MutableSet<Edge> = DetHashSet()
    private val requiredRoutes: MutableList<Route> = ArrayList()

    fun getRequiredRoutes(): List<Route> {
        return requiredRoutes
    }

    private val constrainedFaces: Map<Face, List<Int>> by lazy {
        val out : MutableMap<Face, MutableList<Int>> = mutableMapOf()

        for (sr in requiredRoutes) {
            var rr : Route? = sr
            while (rr != null) {
                val ff = rr.face
                var constraintsForFace = out.get(ff)
                if (constraintsForFace == null) {
                    constraintsForFace = mutableListOf()
                    out[ff] = constraintsForFace
                }
                if (!constraintsForFace.contains(rr._in)) {
                    constraintsForFace.add(rr._in)
                }
                if (!constraintsForFace.contains(rr._out)) {
                    constraintsForFace.add(rr._out)
                }
                val e1 = rr.inEdge
                if (!fixedConstraints.contains(e1)) {
                    floatingConstraints.add(e1)
                }
                val e2 = rr.outEdge
                if (!fixedConstraints.contains(e2)) {
                    floatingConstraints.add(e2)
                }
                rr = rr.rest
            }
        }
        for ((_, value) in out) {
            value.sort()
        }

        out
    }

    /**
     * This is used in portion creation. It looks at which routes use this
     * face, and asks for just the constraints within those routes on that
     * face. This is to avoid breaking the face into too many unnecessary
     * portions.
     */
    fun getConstraintsRequiredForFace(f: Face): List<Int>? {
        return constrainedFaces[f]
    }

    fun isConstrained(e: Edge): Boolean {
        return fixedConstraints.contains(e) || floatingConstraints.contains(e)
    }

    override fun toString(): String {
        return fixedConstraints.toString()
    }

    fun addRoute(r: Route) {
        requiredRoutes.add(r)
    }
}