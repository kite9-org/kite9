package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Turn
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.DartFace

/**
 * Looks through the stack of items to rectangularize, and on finding
 * candidates, prioritises them in order of the ones that will distort the final
 * diagram least.
 *
 * @author robmoffat
 */
abstract class PrioritizingRectangularizer(cd: CompleteDisplayer?) : AbstractRectangularizer(cd!!) {
    enum class Match {
        A, D
    }

    override fun performFaceRectangularization(
        c: Compaction,
        stacks: MutableMap<DartFace, MutableList<VertexTurn>>
    ) {
        val pq = PriorityQueue<RectOption>(500, null)
        val onStack: MutableSet<VertexTurn> = UnorderedSet()
        createInitialRectOptions(c, stacks, pq, onStack)
        while (pq.size() > 0) {
//			log.send("Horizontal Segments:", c.getHorizontalSegmentSlackOptimisation().getAllSlideables());
//			log.send("Vertical Segments:", c.getVerticalSegmentSlackOptimisation().getAllSlideables());
            val ro = pq.remove()!!
            val theStack: MutableList<VertexTurn> = ro.stack
            val action = checkRectOptionIsOk(onStack, ro, pq, c)
            when (action) {
                Action.OK -> performChange(c, pq, onStack, ro, theStack)
                Action.PUT_BACK -> {
                    log.send(if (log.go()) null else "Putting back: $ro")
                    ro.rescore()
                    pq.add(ro)
                }
                Action.DISCARD -> log.send(if (log.go()) null else "Discarding: $ro")
            }
        }
        createInitialRectOptions(c, stacks, pq, onStack)
        if (pq.size() > 0) {
            throw LogicException("Should have completed rectangularization - throwing options away")
        }
    }

    private fun createInitialRectOptions(
        c: Compaction,
        stacks: Map<DartFace, MutableList<VertexTurn>>,
        pq: PriorityQueue<RectOption>,
        onStack: MutableSet<VertexTurn>
    ) {
        for (theStack in stacks.values) {
            for (i in theStack.indices) {
                addNewRectOptions(c, theStack, pq, i)
                onStack.addAll(theStack)
            }
        }
    }

    fun performChange(
        c: Compaction,
        pq: PriorityQueue<RectOption>,
        onStack: MutableSet<VertexTurn>,
        ro: RectOption,
        theStack: MutableList<VertexTurn>
    ) {
        // log.send(log.go() ? null : "Queue Currently: ",pq);
        log.send(if (log.go()) null else "Change: $ro")
        if (ro!!.match == Match.A) {
            performRectangularizationA(
                theStack,
                c,
                ro.meets,
                ro.link,
                ro.par,
                ro.extender,
                (ro as PrioritisedRectOption?)!!.turnShape
            )
            onStack.remove(ro.link)
            onStack.remove(ro.par)
        } else {
            performRectangularizationD(
                theStack,
                c,
                ro.extender,
                ro.par,
                ro.link,
                ro.meets,
                (ro as PrioritisedRectOption?)!!.turnShape
            )
            onStack.remove(ro.link)
            onStack.remove(ro.par)
        }
        val fromIndex = theStack.indexOf(ro.vt1) - 4
        afterChange(c, pq, theStack, fromIndex)
    }

    protected fun afterChange(
        c: Compaction,
        pq: PriorityQueue<RectOption>,
        theStack: MutableList<VertexTurn>,
        fromIndex: Int
    ) {
        // find more matches
        for (i in fromIndex..fromIndex + 8) {
            addNewRectOptions(c, theStack, pq, i)
        }
    }

    private fun addNewRectOptions(
        c: Compaction, theStack: MutableList<VertexTurn>,
        pq: PriorityQueue<RectOption>, i: Int
    ) {
        val m = findPattern(theStack, c, i)
        if (m != null) {
            for (match in m) {
                val ro = createRectOption(theStack, i, match, c)
                pq.add(ro)
                log.send(if (log.go()) null else "Added option: $ro")
            }
        }
    }

    enum class Action {
        DISCARD, PUT_BACK, OK
    }

    protected open fun checkRectOptionIsOk(
        onStack: Set<VertexTurn>,
        ro: RectOption,
        pq: PriorityQueue<RectOption>,
        c: Compaction
    ): Action {
        val allThere = (onStack.contains(ro!!.extender) && onStack.contains(ro.meets) && onStack.contains(
            ro.par
        )
                && onStack.contains(ro.link) && onStack.contains(ro.post))
        if (!allThere) {
            log.send(if (log.go()) null else "Discarding: $ro")
            return Action.DISCARD
        }
        if ((ro as PrioritisedRectOption?)!!.type !== (ro as PrioritisedRectOption?)!!.calculateType()) {
            return Action.PUT_BACK
        }
        if (ro.calculateScore() != ro.initialScore) {
            // change it and throw it back in
            return Action.PUT_BACK
        }
        if (pq.size() > 0) {
            val top = pq.peek()
            if (ro.compareTo(top!!) == 1) {
                return Action.PUT_BACK
            }
        }
        val m = matchTurns(
            ro.vt1, ro.vt2, ro.vt3, ro.vt4, ro.vt5
        )
        return if (!m.contains(ro.match)) {
            Action.DISCARD
        } else Action.OK
    }

    /**
     * Examines a particular rotation pattern on the stack and returns a
     * RectOption for it if it can be rectangularized.
     */
    protected fun findPattern(stack: List<VertexTurn>, c: Compaction, index: Int): Set<Match>? {
        if (stack.size < 4) return null

        // get top four items up to the index on the stack
        val vt5 = getItemRotating(stack, index)
        val vt4 = getItemRotating(stack, index - 1)
        val vt3 = getItemRotating(stack, index - 2)
        val vt2 = getItemRotating(stack, index - 3)
        val vt1 = getItemRotating(stack, index - 4)
        //		log.send(log.go() ? null : "Checking turns at index ending " + index);
        return matchTurns(vt1, vt2, vt3, vt4, vt5)
    }

    private fun matchTurns(
        vt1: VertexTurn,
        vt2: VertexTurn,
        vt3: VertexTurn,
        vt4: VertexTurn,
        vt5: VertexTurn
    ): Set<Match> {
        val out = mutableSetOf<Match>()
        val turns: MutableList<Turn> = ArrayList()
        turns.add(getTurn(vt1, vt2))
        turns.add(getTurn(vt2, vt3))
        turns.add(getTurn(vt3, vt4))
        turns.add(getTurn(vt4, vt5))
        if (turns.toString().contains("STRAIGHT")) {
            throw LogicException("You cannot connect two disparate segments with a straight line")
        } else if (turns.toString().contains("BACK")) {
            throw LogicException("You cannot connect two disparate segments with a back line")
        }
        if (turnMatch(turns[0], turns[1], turns[2], patternA)) {
            out.add(Match.A)
        }
        if (turnMatch(turns[1], turns[2], turns[3], patternD)) {
            out.add(Match.D)
        }
        return out
    }

    fun turnMatch(t1: Turn, t2: Turn, t3: Turn, turns: List<Turn>): Boolean {
        return turns[0] == t1 && turns[1] == t2 && turns[2] == t3
    }

    var rectOptionNo = 0
    fun createRectOption(stack: MutableList<VertexTurn>, index: Int, m: Match, c: Compaction): RectOption {
        val vt5 = getItemRotating(stack, index)
        val vt4 = getItemRotating(stack, index - 1)
        val vt3 = getItemRotating(stack, index - 2)
        val vt2 = getItemRotating(stack, index - 3)
        val vt1 = getItemRotating(stack, index - 4)
        return PrioritisedRectOption(rectOptionNo++, vt1, vt2, vt3, vt4, vt5, m, stack, this)
    }
}