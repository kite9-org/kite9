package org.kite9.diagram.visualization.compaction.insertion

import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.common.elements.factory.TemporaryConnectedRectangular
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.*
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace
import kotlin.math.min

/**
 * This step requires that all the subgraphs being inserted are already
 * rectangularized, as this removes any concave edges in the diagram.
 *
 * This step effectively joins all the various inner and outer faces back
 * together again, to form a single diagram.
 *
 * @author robmoffat
 */
class SubGraphInsertionCompactionStep(cd: CompleteDisplayer) : AbstractCompactionStep(cd), CompactionStep, Logable {

    override fun compact(c: Compaction, r: Embedding, rc: Compactor) {
        log.send("Subgraph Insertion: $r")
        val done: MutableCollection<DartFace?> = HashSet()

        // next, recurse through to go bottom up on the insertions
        for (dartFace in r.dartFaces) {
            insertSubFaces(dartFace, done, c)
        }
    }

    private fun insertSubFaces(dartFace: DartFace, done: MutableCollection<DartFace?>, c: Compaction) {
        if (dartFace == null) {
            throw LogicException("Planarization error: dart face not present")
        }
        if (dartFace.containedFaces.isEmpty()) {
            return  // nothing embedded
        }
        val border = c.getFaceSpace(dartFace)
        if (border == null || border === Compaction.DONE) {
            // not been rectangularized or already done.
            return
        }

        // get space for the darts to be inserted - this must be an empty
        // rectangle in the
        // rectangularization
        var top = border.a.main
        val right = border.b.main
        val bottom = border.c.main
        var left = border.d.main
        var directionOfInsertion: Direction? = null
        val faceInsertionOrder: MutableMap<Int, DartFace> = HashMap()
        for (df in dartFace.containedFaces) {
            val returned = addLowestContainmentIndex(df, faceInsertionOrder)
            if (directionOfInsertion == null) {
                directionOfInsertion = returned
            } else if (directionOfInsertion !== returned) {
                throw LogicException("Containment problem for $df")
            }
        }
        val order: MutableList<Int> = ArrayList(faceInsertionOrder.keys)
        order.sort()
        if (directionOfInsertion === Direction.LEFT || directionOfInsertion === Direction.UP) {
            order.reverse()
        }
        var addedSomething = false
        for (i in order) {
            val embeddedDartFace = faceInsertionOrder[i]
            if (!done.contains(embeddedDartFace)) {
                log.send(if (log.go()) null else "Inserting: \n\t\t $embeddedDartFace\n     into: \n\t\t$dartFace")

                // find the segment border of the subgraph being inserted
                val limits = c.getFaceSpace(embeddedDartFace!!)
                val uLimit = limits!!.a
                val rLimit = limits.b
                val dLimit = limits.c
                val lLimit = limits.d
                if (directionOfInsertion == null || directionOfInsertion === Direction.RIGHT
                    || directionOfInsertion === Direction.LEFT
                ) {
                    separate(top, uLimit)
                    separate(dLimit, bottom!!)
                    separate(left, lLimit)
                    left = rLimit.main
                } else {
                    separate(top, uLimit)
                    separate(left, lLimit)
                    separate(rLimit, right!!)
                    top = dLimit.main
                }
                addedSomething = true
                done.add(embeddedDartFace)
            }
        }
        if (addedSomething) {
            if (directionOfInsertion === Direction.DOWN || directionOfInsertion === Direction.UP) {
                separate(top, bottom)
            } else {
                separate(left, right)
            }
        }
        c.setFaceSpaceToDone(dartFace)
    }

    /**
     * Used for populating the faceInsertionOrder map, and working out the
     * direction in which faces are inserted
     */
    private fun addLowestContainmentIndex(ef: DartFace, faceInsertionOrder: MutableMap<Int, DartFace>): Direction? {
        var out = Int.MAX_VALUE
        var outDir: Direction? = null
        for ((dart) in ef.dartsInFace) {
            for (de in dart.getDiagramElements().keys) {
                if (de is Connected) {
                    val c = (de as Connected).getContainer()
                    if (c != null) {
                        val content: List<DiagramElement> = c.getContents()
                        // since the collection is ordered, position is important
                        val index = content.indexOf(de)
                        if (index != -1) {
                            out = min(out, index)
                            outDir = getDirectionOfInsertion(c.getLayout())
                        } else if (de is TemporaryConnectedRectangular) {
                            out = 1
                            outDir = Direction.DOWN // doesn't matter since there will only be one.
                        } else {
                            throw LogicException("The contained object is not contained in the face or something?")
                        }
                    }
                }
            }
        }
        faceInsertionOrder[out] = ef
        return outDir
    }

    /**
     * Returns all segments at the extreme <direction> edge within the face.
    </direction> */
    protected fun getLimits(
        df: DartFace,
        map: Map<Vertex?, SegmentSlideable>,
        direction: Direction
    ): Set<SegmentSlideable> {
        val out: MutableSet<SegmentSlideable> = LinkedHashSet(4)
        for ((d) in df.dartsInFace) {
            val from = d.getFrom()
            val to = d.getTo()
            val fs = map[from]!!
            val ts = map[to]!!
            if (!out.contains(fs) && testSegment(direction, fs)) {
                out.add(fs)
            }
            if (!out.contains(ts) && testSegment(direction, ts)) {
                out.add(ts)
            }
        }
        if (out.size == 0) {
            throw LogicException("Could not find far-edge segment?? ")
        }
        return out
    }

    /**
     * Tests that the segment has no darts in the direction given.
     */
    protected fun testSegment(dir: Direction, possible: ElementSlideable): Boolean {
        for (v in possible.verticesOnSlideable) {
            for (e in v.getEdges()) {
                if (e is Dart) {
                    if (e.getDrawDirectionFrom(v) === dir) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun getDirectionOfInsertion(layoutDirection: Layout?): Direction? {
        return if (layoutDirection == null) null else when (layoutDirection) {
            Layout.HORIZONTAL, Layout.RIGHT -> Direction.RIGHT
            Layout.LEFT -> Direction.LEFT
            Layout.VERTICAL, Layout.DOWN, Layout.GRID -> Direction.DOWN
            Layout.UP -> Direction.UP
            else -> throw LogicException("Wasn't expecting this direction: $layoutDirection")
        }
    }

    override val prefix: String
        get() = "SGI "
    override val isLoggingEnabled: Boolean
        get() = true
}