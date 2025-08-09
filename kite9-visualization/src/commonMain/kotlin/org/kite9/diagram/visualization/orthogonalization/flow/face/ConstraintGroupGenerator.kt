package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer.Companion.isConstrained
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization

/**
 * This class looks at the constraints set in the diagram and works out how to
 * join all of the constraints together so they are not separated by faces.
 *
 * Note that since we now have unconnected graphs, there can be several
 * constraint groups (i.e. it is not possible to get from one constraint to
 * another via faces and over edges).
 *
 * ConstraintGroups are a necessary evil when there are constrained edges within
 * a graph that are not on the same face - you need to know the route between
 * them in order to ensure that the direction between them is good.
 *
 * There is a problem with constraint groups that it is possible to "box" yourself in, and
 * create constraints that when applied first stop other ones from working.
 * To avoid this, we depth-first search through the faces to find another constrained face,
 * and make a route to it. Then, we use that as a hub to look for another face.
 *
 * @author robmoffat
 */
class ConstraintGroupGenerator : Logable {

    private val log = Kite9Log.instance(this)

    fun getAllFloatingAndFixedConstraints(pln: Planarization): ConstraintGroup {
        val constrainedEdges = gatherConstrainedEdges(pln)
        val out = ConstraintGroup(constrainedEdges, pln.faces.size)
        while (constrainedEdges.size > 1) {
            val startEdge = constrainedEdges.iterator().next()
            constrainedEdges.remove(startEdge)
            val visitedFaces: MutableSet<Face> = UnorderedSet()
            val firstFace = pln.edgeFaceMap[startEdge]!![0]!!
            visitFace(
                firstFace,
                firstFace.indexOf(startEdge).iterator().next(),
                visitedFaces,
                out,
                null,
                constrainedEdges,
                pln,
                0,
                true
            )
        }
        log.send(if (log.go()) null else "Constraint group: $out")
        return out
    }

    /**
     * Returns true if a constraint was found
     */
    private fun visitFace(
        face: Face,
        `in`: Int,
        visitedFaces: MutableSet<Face>,
        theGroup: ConstraintGroup,
        openRoute: Route?,
        constrainedEdges: MutableSet<Edge>,
        pln: Planarization,
        depth: Int,
        constraintFound: Boolean
    ): Boolean {
        var `in` = `in`
        var openRoute = openRoute
        var constraintFound = constraintFound
        log.send(pad(depth) + "Visiting Face: " + face.getID() + " start at " + `in`)
        visitedFaces.add(face)
        var start = `in`

        // create all constraints for the current face
        for (i in 0 until face.edgeCount()) {
            val out = (i + start) % face.edgeCount()
            val currentEdge: Edge = face.getBoundary(out)
            log.send(if (log.go()) null else pad(depth + 1) + "checking: " + currentEdge + " at " + out)

            // check to see if we have completed a constraint
            if (constrainedEdges.contains(currentEdge)) {
                constrainedEdges.remove(currentEdge)
                openRoute = Route(face, `in`, out, openRoute)
                log.send(if (log.go()) null else pad(depth) + "Route added: " + openRoute)
                theGroup.addRoute(openRoute)
                openRoute = null
                constraintFound = true
                `in` = out
            }
        }
        start = `in`

        // ok, start moving off to other faces
        for (i in 0 until face.edgeCount()) {
            val out = (i + start) % face.edgeCount()
            val currentEdge: Edge = face.getBoundary(out)
            // check for traverse face
            val meetingFaces: List<Face?> = pln.edgeFaceMap[currentEdge]!!
            val otherFace = if (meetingFaces[0] === face) meetingFaces[1] else meetingFaces[0]
            if (!visitedFaces.contains(otherFace)) {
                // go to the new face
                val toUse = if (`in` != out) {
                    Route(face, `in`, out, openRoute)
                } else {
                    openRoute
                }
                log.send(pad(depth) + "Using route: " + toUse)
                val found = visitFace(
                    otherFace!!,
                    otherFace!!.indexOf(currentEdge).iterator().next(),
                    visitedFaces,
                    theGroup,
                    toUse,
                    constrainedEdges,
                    pln,
                    depth + 1,
                    out == `in`
                )
                if (found && !constraintFound) {
                    // you can only visit one new face unless you find another constraint
                    log.send(pad(depth) + "Finished Face: " + face.getID())
                    return found
                }
            }
        }
        log.send(pad(depth) + "Finished Face: " + face.getID())
        return constraintFound
    }

    private fun pad(depth: Int): String {
        val sb = StringBuilder(depth)
        for (i in 0 until depth) {
            sb.append(' ')
        }
        return sb.toString()
    }

    /**
     * Creates one constraint group from each constrained edge in the diagram
     */
    private fun gatherConstrainedEdges(pln: Planarization): MutableSet<Edge> {
        val constrainedEdges: MutableSet<Edge> = DetHashSet()
        for (f in pln.faces) {
            for (edge in f.edgeIterator()) {
                if (isConstrained(edge) && !constrainedEdges.contains(edge)) {
                    constrainedEdges.add(edge)
                }
            }
        }
        log.send(if (log.go()) null else "initial constrained edges: " + listConstraints(constrainedEdges))
        return constrainedEdges
    }

    private fun listConstraints(constrainedEdges: Set<Edge>): String {
        val sb = StringBuilder()
        for (edge in constrainedEdges) {
            sb.append(edge.toString())
            sb.append(" ")
            sb.append(edge.getDrawDirection())
            sb.append("(")
            sb.append((edge as PlanarizationEdge).getDiagramElements())
            sb.append(")")
            sb.append(",")
        }
        return sb.toString()
    }

    override val prefix: String
        get() = "COGG"
    override val isLoggingEnabled: Boolean
        get() = true
}