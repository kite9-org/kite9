package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.planarization.ordering.BasicVertexEdgeOrdering
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering

/**
 * Utility functions for manipulating the Planarization
 *
 * @author robmoffat
 */
class Tools : Logable {
    var log = Kite9Log.instance(this)
    var elementNo = 0
    override val prefix: String
        get() = "PLNT"
    override val isLoggingEnabled: Boolean
        get() = true

    /**
     * This inserts the new EdgeCrossingVertex into an edge to break it in two
     */
    fun breakEdge(e: PlanarizationEdge, pln: Planarization, split: Vertex): Vertex {
        val faces = pln.edgeFaceMap[e]
        val from = e.getFrom()
        val to = e.getTo()
        val fromEdgeOrdering = pln.edgeOrderings[from] as VertexEdgeOrdering?
        val toEdgeOrdering = pln.edgeOrderings[to] as VertexEdgeOrdering?
        log.send(if (log.go()) null else "Original edge order around $from = $fromEdgeOrdering")
        log.send(if (log.go()) null else "Original edge order around $to = $toEdgeOrdering")

        // split the existing edge to create two edges		
        val newEdges = splitEdge(e, split, pln)

        // new edges will have same faces
        pln.edgeFaceMap.remove(e)
        pln.edgeFaceMap.put(newEdges.a, ArrayList(faces))
        pln.edgeFaceMap.put(newEdges.b, ArrayList(faces))

        // new vertex will have same faces as edge
        pln.vertexFaceMap.put(split, faces!!.filterNotNull().toMutableList() )

        // add to the edge ordering map. since there are only 2 edges, order not
        // important yet.
        val edges: MutableList<PlanarizationEdge> = ArrayList()
        edges.add(newEdges.a)
        edges.add(newEdges.b)
        val splitEdgeOrdering = BasicVertexEdgeOrdering(edges, split)
        pln.edgeOrderings.put(split, splitEdgeOrdering)

        // update the from/to edge ordering
        fromEdgeOrdering!!.replace(e, if (newEdges.a.meets(from)) newEdges.a else newEdges.b)
        toEdgeOrdering!!.replace(e, if (newEdges.b.meets(to)) newEdges.b else newEdges.a)
        log.send(if (log.go()) null else "New edge order around $from = $fromEdgeOrdering")
        log.send(if (log.go()) null else "New edge order around $to = $toEdgeOrdering")
        log.send(if (log.go()) null else "New edge order around $split = $splitEdgeOrdering")

        // now repair the face. there are two edges instead of the original one
        for (face in faces!!) {
            face!!.checkFaceIntegrity()
            var indexes = face.indexOf(e)
            while (indexes.size > 0) {
                val i = indexes[0]
                if (face.getCorner(i) === from && face.getCorner(i + 1) === to) {
                    face.remove(i)
                    face.add(i, from, newEdges.a)
                    face.add(i + 1, split, newEdges.b)
                } else if (face.getCorner(i) === to && face.getCorner(i + 1) === from) {
                    face.remove(i)
                    face.add(i, to, newEdges.b)
                    face.add(i + 1, split, newEdges.a)
                } else {
                    throw LogicException("Should be one way around or the other")
                }
                indexes = face.indexOf(e)
            }
            face.checkFaceIntegrity()
        }
        return split
    }

    /**
     * Ensures that the edges that used to be adjacent to face f are now
     * adjacent to f2. Used when breaking / merging faces.
     *
     * @param part
     */
    private fun fixEdgeFaceMap(
        pln: Planarization,
        f: Face,
        movedFace: Iterable<PlanarizationEdge>,
        f2: Face,
        part: PlanarizationEdge?
    ) {
        for (edge in movedFace) {
            if (edge !== part) {
                val faces: MutableList<Face?>? = pln.edgeFaceMap[edge]
                faces!!.remove(f)
                faces.add(f2)
                if (faces.size != 2) {
                    throw LogicException("Should be exactly 2 faces for each edge$edge")
                }
            }
        }
    }

    fun fixVertexFaceMap(pln: Planarization, a: Face, toConsider: Iterable<Vertex?>, faceIsDeleted: Boolean) {
        for (vertex in toConsider) {
            val faces: MutableList<Face> = pln.vertexFaceMap.get(vertex)!!
            if (faceIsDeleted) {
                faces!!.remove(a)
            } else if (a.contains(vertex!!)) {
                if (!faces!!.contains(a)) {
                    faces.add(a)
                }
            } else {
                if (faces!!.contains(a)) {
                    faces.remove(a)
                }
            }
        }
    }

    fun updateVertexFaceMap(pln: Planarization, v: Vertex?) {
        val faces: MutableList<Face>? = pln.vertexFaceMap.get(v)
        val iterator = faces!!.iterator()
        while (iterator.hasNext()) {
            if (!iterator.next().contains(v!!)) {
                iterator.remove()
            }
        }
        if (faces.size == 0) {
            pln.vertexFaceMap.remove(v)
        }
    }

    /**
     * This is used when a temporary edge is removed, leaving behind 'island'
     * vertices within a face. This creates a new outer face containing those
     * vertices.
     */
    private fun splitFaces(toRemove: PlanarizationEdge, pln: Planarization) {
        val faces = pln.edgeFaceMap[toRemove]
        val original = faces!![0]!!
        val newFace = original.split(toRemove)
        val from = toRemove.getFrom()
        val to = toRemove.getTo()
        (pln.edgeOrderings[from] as VertexEdgeOrdering?)!!.remove(toRemove)
        (pln.edgeOrderings[to] as VertexEdgeOrdering?)!!.remove(toRemove)
        from.removeEdge(toRemove)
        to.removeEdge(toRemove)
        pln.edgeFaceMap.remove(toRemove)
        fixEdgeFaceMap(pln, original, newFace.edgeIterator(), newFace, null)

        // make sure the faces all refer to each other 
        val containedFaces: Collection<Face> = original.containedFaces
        newFace.containedFaces.addAll(containedFaces)
        for (face in containedFaces) {
            face.containedFaces.add(newFace)
        }
        original.containedFaces.add(newFace)
        newFace.containedFaces.add(original)
        pln.vertexFaceMap[from]!!.add(newFace)
        pln.vertexFaceMap[to]!!.add(newFace)
        newFace.partOf = original.partOf
        log.send(if (log.go()) null else "Removed" + toRemove + " splitting into " + original.getID() + " and " + newFace.getID())
        log.send(if (log.go()) null else "Original:$original")
        log.send(if (log.go()) null else "NewFace:$newFace")
        fixVertexFaceMap(pln, original, original.cornerIterator(), false)
        fixVertexFaceMap(pln, original, newFace.cornerIterator(), false)
        fixVertexFaceMap(pln, newFace, original.cornerIterator(), false)
        fixVertexFaceMap(pln, newFace, newFace.cornerIterator(), false)
    }

    /**
     * Removes the edge from the planarization, preserving the new state of the
     * remaining faces, whatever that may be.
     */
    fun removeEdge(toRemove: PlanarizationEdge, pln: Planarization) {
        val faces = pln.edgeFaceMap[toRemove]
        val from = toRemove.getFrom()
        val to = toRemove.getTo()
        if (toRemove is BiDirectionalPlanarizationEdge) {
            val underlying = toRemove.getOriginalUnderlying()
            if (underlying != null) {
                val el = pln.edgeMappings[underlying]
                el!!.remove(toRemove)
                log.send("Route for $underlying is now ", el.edges)
            }
        }
        if (faces!![0] === faces!![1]) {
            splitFaces(toRemove, pln)
        } else {
            mergeFace(toRemove, pln)
        }
        checkRemoveVertex(pln, from)
        checkRemoveVertex(pln, to)
    }

    /**
     * Merges 2 faces together by removing toRemove.
     */
    private fun mergeFace(toRemove: PlanarizationEdge, pln: Planarization) {
        val faces = pln.edgeFaceMap[toRemove]
        val newCorners: MutableList<Vertex> = ArrayList()
        val newBoundary: MutableList<PlanarizationEdge> = ArrayList()
        var face = 0
        // this is the number of vertices that should be in the merged face
        val vertexCount = faces!![0]!!.vertexCount() + faces[1]!!.vertexCount() - 2
        var vertexNo = 0
        do {
            var currentFace = faces[face]!!
            val currentVertex = currentFace.getCorner(vertexNo)
            val boundEdge = currentFace.getBoundary(vertexNo)
            if (boundEdge !== toRemove) {
                newCorners.add(currentVertex)
                newBoundary.add(boundEdge)
            } else {
                // move onto the other face
                face = if (face == 0) 1 else 0
                currentFace = faces[face]!!
                vertexNo = currentFace.indexOf(toRemove.otherEnd(currentVertex), toRemove)
            }
            vertexNo = (vertexNo + 1) % currentFace.vertexCount()
        } while (newCorners.size < vertexCount)
        val a = faces[0]!!
        val b = faces[1]!! // removing this one
        a.reset(newBoundary, newCorners)
        pln.faces.remove(b)
        if (a.partOf == null) {
            a.partOf = b.partOf
        } else {
            if (b.partOf != null && b.partOf !== a.partOf) {
                throw LogicException("PartOf set wrongly")
            }
        }


        // move all references from b to a
        fixEdgeFaceMap(pln, b, b.edgeIterator(), a, null)
        pln.edgeFaceMap.remove(toRemove)

        // fix vertex maps
        fixVertexFaceMap(pln, a, b.cornerIterator(), false)
        fixVertexFaceMap(pln, a, a.cornerIterator(), false)
        fixVertexFaceMap(pln, b, b.cornerIterator(), true)
        fixVertexFaceMap(pln, b, a.cornerIterator(), true)

        // remove the edge from the planarization
        val set1 = pln.edgeOrderings[toRemove.getFrom()] as VertexEdgeOrdering?
        set1!!.remove(toRemove)
        val set2 = pln.edgeOrderings[toRemove.getTo()] as VertexEdgeOrdering?
        set2!!.remove(toRemove)
        toRemove.remove()

        // check integrity of created face
        a.checkFaceIntegrity()

        // just tidying up, shouldn't be needed
        pln.removeEdge(toRemove)
        log.send(
            if (log.go()) null else """Removed $toRemove merging ${a.getID()} and ${b.getID()} gives ${a.getID()} with ${a.cornerIterator()} 
 ${a.edgeIterator()}"""
        )

        // tidy up face hierarchy
        for (inside in b.containedFaces) {
            a.containedFaces.add(inside)
            inside.containedFaces.add(a)
            inside.containedFaces.remove(b)
        }
    }

    private fun removeVertex(toGo: Vertex, pln: Planarization) {
        if (toGo.getEdgeCount() != 2) {
            throw LogicException("Can't remove a vertex with anything other than 2 edges")
        }
        val it = toGo.getEdges().iterator()
        val a = it.next()
        val b = it.next()
        val farB = b.otherEnd(toGo)
        log.send(if (log.go()) null else "Removing: " + toGo + "involving " + a + " and " + b)
        val loopback = a.meets(farB)
        if (loopback) {
            log.send(if (log.go()) null else "Cannot introduce loopback, finishing")
            return
        }
        if (a is ConnectionEdge) {
            val otherEnd =
                if (b.getFrom() === toGo) (b as ConnectionEdge).getToConnected() else (b as ConnectionEdge).getFromConnected()
            if (a.getFrom() === toGo) {
                a.setFromConnected(otherEnd)
            } else if (a.getTo() === toGo) {
                a.setToConnected(otherEnd)
            } else {
                throw LogicException("Couldn't find end")
            }
        }
        if (a.getFrom() === toGo) {
            (a as PlanarizationEdge).setFrom(farB)
        } else {
            (a as PlanarizationEdge).setTo(farB)
        }
        toGo.removeEdge(a)
        toGo.removeEdge(b)
        farB.removeEdge(b)
        farB.addEdge(a)
        log.send(if (log.go()) null else "Created: $a")
        log.send(if (log.go()) null else "removed: $b")

        // remove toGo from the faces
        for (f in pln.vertexFaceMap[toGo]!!) {
            for (i in 0 until f.edgeCount()) {
                val v = f.getCorner(i)
                if (v === toGo) {
                    f.remove(i)
                }
            }

            // make sure we are always keeping the correct edge
            f.replaceEdge((b as PlanarizationEdge), a)
            f.checkFaceIntegrity()
        }
        pln.vertexFaceMap.remove(toGo)
        pln.edgeOrderings.remove(toGo)
        pln.edgeFaceMap.remove(b)
        a.getDiagramElements().keys.stream().forEach { underlying: DiagramElement ->
            val list = pln.edgeMappings[underlying]
            //log.send("Edge Mapping before: "+list);
            if (list != null) {
                list.remove(b)
                log.send("Route for $underlying is now ", list.edges)
            }
        }

        // fix up vertex edge ordering - only works if vertex is a connected item
        val orderingOfTo = pln.edgeOrderings[farB] as VertexEdgeOrdering?
        orderingOfTo!!.replace(b as PlanarizationEdge, a)
    }

    /**
     * Splits an edge into two parts, preserving the original intact. First edge
     * in the array is the from end, second is to end
     */
    fun splitEdge(parent: PlanarizationEdge, toIntroduce: Vertex?, pln: Planarization): Pair<PlanarizationEdge> {
        log.send(if (log.go()) null else "Splitting: $parent")
        val out = parent.split(toIntroduce!!)
        for (de in parent.getDiagramElements().keys) {
            val list = pln.edgeMappings[de]
            list?.replace(parent, out.a, out.b)
        }
        parent.getFrom().removeEdge(parent)
        parent.getTo().removeEdge(parent)
        pln.edgeFaceMap.remove(parent)
        log.send(
            if (log.go()) null else """Made: 
	${out.a}
	${out.b}"""
        )
        return out
    }

    fun checkRemoveVertex(pln: Planarization, v: Vertex) {
        if (canBeRemoved(v)) {
            val linkCount = v.getEdgeCount()
            if (linkCount == 2) {
                val edges = v.getEdges().iterator()
                val a = edges.next()
                val b = edges.next()
                if (a.getDrawDirectionFrom(v) === reverse(b.getDrawDirectionFrom(v))) {
                    removeVertex(v, pln)
                }
            }
        }
    }

    private fun canBeRemoved(v: Vertex): Boolean {
        return v.hasDimension() == false
    }

    companion object {

		fun setUnderlyingContradiction(c: BiDirectional<*>?, state: Boolean) {
            if (c is BiDirectionalPlanarizationEdge) {
                val underlying = c.getOriginalUnderlying()
                if (underlying is Connection) {
                    setConnectionContradiction(underlying, state, true)
                } else {
                    throw LogicException("Wasn't expecting to set contradiction on $underlying")
                }
            }
        }


		fun isUnderlyingContradicting(c2: BiDirectional<*>?): Boolean {
            return if (c2 is BiDirectionalPlanarizationEdge) {
                val underlying = c2.getOriginalUnderlying()
                if (underlying is Connection) {
                    isConnectionContradicting(underlying)
                } else false
            } else {
                false
            }
        }


		fun isConnectionContradicting(c: Connection): Boolean {
            val rri = c.getRenderingInformation()
            return rri.isContradicting
        }


		fun isConnectionRendered(c: Connection): Boolean {
            val rri = c.getRenderingInformation()
            return rri.rendered
        }


		fun setConnectionContradiction(c: Connection, contradicting: Boolean, rendering: Boolean) {
            val rri = c.getRenderingInformation()
            if (c.getDrawDirection() != null) {
                rri.isContradicting = contradicting
            }
            rri.rendered = rendering
        }
    }
}