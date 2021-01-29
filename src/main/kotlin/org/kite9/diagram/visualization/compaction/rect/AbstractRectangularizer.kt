package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.common.Collections
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.objects.Rectangle
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Turn
import org.kite9.diagram.visualization.compaction.*
import org.kite9.diagram.visualization.compaction.rect.PrioritisedRectOption.TurnShape
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection

/**
 * This class is responsible for 'completing' a dart diagram by ensuring that
 * each face is subdivided by darts into rectangles.
 *
 * This is done because otherwise the algorithm for compaction cannot work out
 * exactly where to align the darts.
 *
 * The way this works is by tracing around each face looking for LRR or RRL
 * patterns of angles between the segments in the face.
 *
 * There are 5 [VertexTurn]s in a rectangularization. These are
 * labelled as follows:
 *
 * <pre>
 * |          <- (post - not changed)
 * ------    <- (meets)
 * |   <-  (link)
 * ---    <-  (par)
 * |       <- (extender)
 * |
</pre> *
 *
 * The naming is because the extender is extended to the meets item. par is
 * clearly parallel to meets, while link is linking par to meets.
 *
 * @author robmoffat
 */
abstract class AbstractRectangularizer(cd: CompleteDisplayer) : AbstractCompactionStep(cd) {

    var failfast = false

    /**
     * This ties off any loose ends in the diagram by extending the segments to
     * meet each other. This prevents overlapping of darts in the diagram.
     * overlapping.
     */
    override fun compact(c: Compaction, r: Embedding, rc: Compactor) {
        log.send("Rectangularizing: $r")
        val faces = r.dartFaces ?: return
        val orderedFaces = selectFacesToRectangularize(c, faces)
        log.send("Rectangularizing faces: ", orderedFaces)
        val stacks = setupDartFaceStacks(c, orderedFaces)
        performSecondarySizing(c, stacks)
        performFaceRectangularization(c, stacks)
        for (df in orderedFaces) {
            val theStack = stacks[df]
            if (theStack != null) {
                if (!df!!.outerFace) {
                    if (theStack.size != 4) {
                        throw LogicException("Rectangularization did not complete properly - stack > 4, face = $df")
                    }
                }
                for (i in theStack.indices) {
                    fixSize(c, getIthElementRotating(theStack, i), 0.0, !df.outerFace, false)
                }
                setSlideableFaceRectangle(c, df, theStack, df.outerFace)
            }
        }
    }

    open fun performSecondarySizing(c: Compaction, stacks: Map<DartFace, MutableList<VertexTurn>>) {

    }

    protected abstract fun selectFacesToRectangularize(c: Compaction, faces: List<DartFace>): List<DartFace>

    protected fun setupDartFaceStacks(c: Compaction, orderedFaces: List<DartFace>): MutableMap<DartFace, MutableList<VertexTurn>> {
        val stacks: MutableMap<DartFace, MutableList<VertexTurn>> = HashMap()
        for (df in orderedFaces) {
            log.send(if (log.go()) null else "Creating Face Stack for: $df")
            // first, add all the segments to the stack in unrectangularized
            // form
            val theStack: MutableList<VertexTurn> = ArrayList()
            val turns: List<DartDirection> = df.dartsInFace
            buildStack(df, theStack, turns, c)
            for (i in theStack.indices) {
                fixSize(c, getIthElementRotating(theStack, i), 0.0, isConcave(theStack, i), true)
            }
            stacks[df] = theStack
        }
        return stacks
    }

    private fun isConcave(theStack: List<VertexTurn?>, i: Int): Boolean {
        val prev = getIthElementRotating(theStack, i - 1)
        val next = getIthElementRotating(theStack, i + 1)
        return prev!!.direction !== next!!.direction
    }

    private fun buildStack(
        df: DartFace,
        theStack: MutableList<VertexTurn>,
        turns: List<DartDirection>,
        c: Compaction
    ) {
        if (df.dartsInFace.size > 2) {
            var startPoint = 0
            for (i in turns.indices) {
                val last = turns[(turns.size - 1 + i) % turns.size]
                val current = turns[i]
                if (last.direction !== current.direction) {
                    startPoint = i
                    break
                }
            }
            val turnCopy: MutableList<DartDirection> = ArrayList(turns)
            java.util.Collections.rotate(turnCopy, -startPoint)

            val segments = turnCopy.map { c.getSegmentForDart( it.dart ) }

            val directions = turnCopy.map { it.direction }
            val uniqueSegments: MutableList<Segment> = ArrayList()
            val uniqueDirections: MutableList<Direction> = ArrayList()
            for (i in segments.indices) {
                val segment = segments[i]
                val d = directions[i]
                if (i == 0 || segment != uniqueSegments[uniqueSegments.size - 1]) {
                    uniqueSegments.add(segment)
                    uniqueDirections.add(d)
                }
            }

            // convert to VertexTurns
            val us = uniqueSegments.size
            for (i in 0 until us) {
                val last = uniqueSegments[(i - 1 + us) % us]
                val current = uniqueSegments[i]
                val next = uniqueSegments[(i + 1) % us]
                val d = uniqueDirections[i]
                val t = VertexTurn(i, c, current.slideable!!, d, last.slideable!!, next.slideable!!, df.partOf)
                theStack.add(t)
            }
            log.send("Stack for face $df", theStack)
        }
    }

    protected abstract fun performFaceRectangularization(c: Compaction, stacks: MutableMap<DartFace, MutableList<VertexTurn>>)

    private fun setSlideableFaceRectangle(c: Compaction, df: DartFace, theStack: List<VertexTurn>, outer: Boolean) {
        val r = Rectangle(
            getSlideableInDirection(theStack, if (outer) Direction.LEFT else Direction.RIGHT, outer),
            getSlideableInDirection(theStack, if (outer) Direction.UP else Direction.DOWN, outer),
            getSlideableInDirection(theStack, if (outer) Direction.RIGHT else Direction.LEFT, outer),
            getSlideableInDirection(theStack, if (outer) Direction.DOWN else Direction.UP, outer)
        )
        c.createFaceSpace(df, r)
    }

    private fun getSlideableInDirection(vt: List<VertexTurn>, d: Direction, outer: Boolean): FaceSide {
        val others: MutableSet<Slideable<Segment>> = HashSet()
        var main: Slideable<Segment>? = null
        for (i in vt.indices) {
            val prev = vt[(i + vt.size - 1) % vt.size]
            val curr = vt[i]
            val next = vt[(i + 1) % vt.size]
            if (curr.direction === d) {
                if (prev.direction !== next.direction) {
                    main = curr.slideable
                }
                if (outer == true) {
                    others.add(curr.slideable)
                }
            }
        }
        if (main == null) {
            throw LogicException("No turn in that direction")
        }
        return FaceSide(main, others)
    }

    protected fun performRectangularizationD(
        stack: MutableList<VertexTurn>, c: Compaction, ext: VertexTurn,
        par: VertexTurn, link: VertexTurn, meets: VertexTurn, shape: TurnShape
    ) {
        // logRectangularizationContext(ext, par, link, meets);
        val first: Slideable<Segment> = ext.endsWith
        val to: Slideable<Segment> = meets.slideable
        performRectangularization(c, meets, link, par, ext, first, to, shape)
        cutRectangleCorner(stack, par, link)
    }

    /**
     * Given that fixing is in a rectangle, with sides of before and after,
     * there may need to be a minimum length set on fixing.
     *
     * This errs on the side of too large right now
     * @param initialSetting
     */
    protected fun fixSize(
        c: Compaction?,
        link: VertexTurn?,
        externalMin: Double,
        concave: Boolean,
        initialSetting: Boolean
    ) {
        val early = if (link!!.increasingDirection()) link.startsWith else link.endsWith
        val late = if (link.increasingDirection()) link.endsWith else link.startsWith
        val early1 = early.underlying
        val late1 = late.underlying
        log.send(if (log.go()) null else " Early: $early late: $late")
        val along = if (initialSetting) link.segment else null
        val minDistance = getMinimumDistance(early1, late1, along, concave)
        link.ensureMinLength(Math.max(minDistance, externalMin))
        log.send(if (log.go()) null else "Fixed: $link min length $minDistance")
        return
    }

    protected fun performRectangularizationA(
        stack: MutableList<VertexTurn>, c: Compaction, meets: VertexTurn,
        link: VertexTurn, par: VertexTurn, ext: VertexTurn, shape: TurnShape
    ) {
        // logRectangularizationContext(meets, link, par, ext);
        val first: Slideable<Segment> = ext.startsWith
        val to: Slideable<Segment> = meets.slideable
        performRectangularization(c, meets, link, par, ext, first, to, shape)
        cutRectangleCorner(stack, link, par)
    }

    protected fun performRectangularization(
        c: Compaction, meets: VertexTurn, link: VertexTurn,
        par: VertexTurn, extender: VertexTurn, from: Slideable<Segment>, to: Slideable<Segment>, shape: TurnShape
    ) {
        val newExtenderLength = extender.getLength(false) + link.getLength(false)
        if (extender.startsWith == from) {
            extender.resetEndsWith(to, link.turnPriority, newExtenderLength)
        } else {
            extender.resetStartsWith(to, link.turnPriority, newExtenderLength)
        }

        // update meets
        if (meets.startsWith == link.slideable) {
            meets.resetStartsWith(extender.slideable, meets.turnPriority, 0.0)
        } else {
            meets.resetEndsWith(extender.slideable, meets.turnPriority, 0.0)
        }
        fixSize(c, meets, meets.getLength(false), shape === TurnShape.G, false)
    }

    private fun logRectangularizationContext(vt4: VertexTurn, vt3: VertexTurn, vt2: VertexTurn, vt1: VertexTurn) {
        log.send(if (log.go()) null else "Context:")
        log.send(if (log.go()) null else "vt4 $vt4")
        log.send(if (log.go()) null else "vt3 $vt3")
        log.send(if (log.go()) null else "vt2 $vt2")
        log.send(if (log.go()) null else "vt1 $vt1")
    }

    /**
     * After the new darts are added to create the rectangle, this snips off the
     * old rectangle from the stack
     */
    private fun cutRectangleCorner(stack: MutableList<VertexTurn>, remove1: VertexTurn, remove2: VertexTurn) {
        stack.remove(remove1)
        stack.remove(remove2)
        log.send(if (log.go()) null else "Removed: $remove1")
        log.send(if (log.go()) null else "Removed: $remove2")
    }

    override val prefix: String
        get() = "ARec"
    override val isLoggingEnabled: Boolean
        get() = true

    companion object {
        /**
         * Works out the direction of turn between one segment and the next
         */
		@JvmStatic
		fun getTurn(t1: VertexTurn, t2: VertexTurn): Turn {
            val thisDirection = t1.direction
            val nextDirection = t2.direction
            return thisDirection.getDirectionChange(nextDirection)
        }

        fun <X> getIthElementRotating(items: List<X>, i: Int): X {
            return items[(i + items.size) % items.size]
        }

        @JvmStatic
		protected fun getItemRotating(stack: List<VertexTurn>, index: Int): VertexTurn {
            var index = index
            while (index < 0) index += stack.size
            index = index % stack.size
            return stack[index]
        }

        @JvmField
		var patternA = createList(Turn.LEFT, Turn.RIGHT, Turn.RIGHT)
        @JvmField
		var patternD = createList(Turn.RIGHT, Turn.RIGHT, Turn.LEFT)
        private fun createList(vararg turns: Turn): List<Turn> {
            val out: MutableList<Turn> = ArrayList()
            for (t in turns) {
                out.add(t)
            }
            return out
        }
    }
}