package org.kite9.diagram.visualization.planarization.transform

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.isMin
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.EdgeMapping
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization

/**
 * Works out which faces are outer faces, and identfies them as such.
 */
class OuterFaceIdentificationTransform : PlanarizationTransform, Logable {

    private val log = Kite9Log(this)

    override fun transform(pln: Planarization) {
        val d = pln.diagram
        val em = pln.edgeMappings[d]
        val outerFace = getDiagramOuterFace(em, pln)
        val done: MutableSet<Face> = UnorderedSet(pln.faces.size * 2)
        handleOuterFace(outerFace, null, pln, 0, done)
    }

    private fun getDiagramOuterFace(em: EdgeMapping?, pln: Planarization): Face {
        val v = (pln as MGTPlanarization).vertexOrder[0]
        val faces = pln.vertexFaceMap[v]
        for (face in faces!!) {
            if (face.containedFaces.size == 0 && isAntiClockwise(face)) {
                return face
            }
        }
        throw LogicException("Couldn't find the outer face")
    }

    private fun isAntiClockwise(f: Face): Boolean {
        for (i in 0 until f.size()) {
            val v = f.getCorner(i)
            val e: Edge = f.getBoundary(i)
            val cv = v as MultiCornerVertex
            if (isMin(cv.xOrdinal) && isMin(cv.yOrdinal)) {
                return e.getDrawDirectionFrom(v) === Direction.DOWN
            }
        }
        throw LogicException("outer face not anti-clockwise")
    }

    private fun handleOuterFace(
        outerFace: Face,
        inside: Face?,
        pln: Planarization,
        level: Int,
        done: MutableSet<Face>
    ) {
        if (done.contains(outerFace)) {
            return
        }
        done.add(outerFace)
        log.send(level(level) + "Outer face: " + outerFace.getID())
        outerFace.isOuterFace = true
        outerFace.containedBy = inside
        outerFace.containedFaces.clear()
        traverseInnerFaces(outerFace, pln, level + 1, done)
    }

    private fun traverseInnerFaces(face: Face, pln: Planarization, level: Int, done: MutableSet<Face>) {
        for (i in 0 until face.size()) {
            val e: Edge = face.getBoundary(i)
            val faces = pln.edgeFaceMap[e]
            for (f in faces!!) {
                if (f !== face) {
                    handleInnerFace(f, pln, level, done)
                }
            }
        }
    }

    private fun level(level: Int): String {
        val sb = StringBuilder(level)
        for (i in 0 until level) {
            sb.append(" ")
        }
        return sb.toString()
    }

    private fun handleInnerFace(innerFace: Face, pln: Planarization, level: Int, done: MutableSet<Face>) {
        if (done.contains(innerFace)) {
            return
        }
        done.add(innerFace)
        log.send(level(level) + "Inner face: " + innerFace.getID())
        for (f in innerFace.containedFaces) {
            handleOuterFace(f, innerFace, pln, level + 1, done)
        }
        traverseInnerFaces(innerFace, pln, level, done)
    }

    override val prefix: String
        get() = "OFIT"

    override val isLoggingEnabled: Boolean
        get() = true
}