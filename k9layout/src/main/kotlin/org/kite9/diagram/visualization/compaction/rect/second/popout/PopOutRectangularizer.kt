package org.kite9.diagram.visualization.compaction.rect.second.popout

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.rect.VertexTurn
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.compaction.rect.second.NonEmbeddedFaceRectangularizer
import org.kite9.diagram.visualization.compaction.rect.second.prioritised.PrioritisedRectOption
import org.kite9.diagram.visualization.compaction.rect.second.prioritised.RectOption
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable

/**
 * HAndles the case where we want to 'pop out' the corner to avoid rectangularizing two
 * minimized sides against each other (causes sizing issues).
 *
 * @author robmoffat
 */
class PopOutRectangularizer(cd: CompleteDisplayer) : NonEmbeddedFaceRectangularizer(cd) {

    override fun performChange(
        c: Compaction,
        pq: PriorityQueue<RectOption>,
        onStack: MutableSet<VertexTurn>,
        ro: RectOption,
        theStack: MutableList<VertexTurn>
    ) {
        val pro = ro as PrioritisedRectOption
        if (pro.turnShape == PrioritisedRectOption.TurnShape.U) {

            if ((pro.vt2.turnPriority == VertexTurn.TurnPriority.MINIMIZE_RECTANGULAR) &&
                (pro.vt4.turnPriority == VertexTurn.TurnPriority.MINIMIZE_RECTANGULAR)
            ) {
                // change the slideable on meets, extender to bufferSlideable
                val dimension = pro.vt5.slideable.dimension
                val oldSlideables = listOf(pro.vt1.slideable, pro.vt5.slideable)
                val sso = pro.vt5.slideable.so
                val bufferSlideable = BufferSlideable(sso, dimension, oldSlideables)
                sso.updateMaps(bufferSlideable);

                // make sure everything is the right distance from the buffer
                if (pro.vt2.increasingDirection()) {
                    sso.ensureMinimumDistance(bufferSlideable, pro.vt1.slideable, 0)
                    sso.ensureMinimumDistance(bufferSlideable, pro.vt5.slideable, 0)
                } else {
                    sso.ensureMinimumDistance(pro.vt1.slideable, bufferSlideable, 0)
                    sso.ensureMinimumDistance(pro.vt5.slideable, bufferSlideable, 0)
                }

                val dist = getMinimumDistance(pro.vt2.slideable, pro.vt4.slideable, pro.vt3.slideable, false )
                fixSize(c, pro.vt3, dist, false, false)


                // we need to remove all 5 vertex turns and replace with the buffer slideable
                val endsWith = otherEndOf(pro.vt5, pro.vt4.slideable)
                val startsWith = otherEndOf(pro.vt1, pro.vt2.slideable)
                val vtNew = VertexTurn(nextTurnNumber++, c, bufferSlideable, pro.vt3.direction, startsWith, endsWith, null)

                onStack.remove(pro.vt1)
                onStack.remove(pro.vt2)
                onStack.remove(pro.vt3)
                onStack.remove(pro.vt4)
                onStack.remove(pro.vt5)
                onStack.add(vtNew)

                // fix ends of turns before / after
                var firstIndex = theStack.indexOf(pro.vt1)
                var prevIndex = if (firstIndex == 0) theStack.size - 1 else firstIndex - 1
                val vt0 = theStack.get(prevIndex)
                vt0.resetEndsWith(bufferSlideable, vt0.turnPriority, vt0.getLength(false))

                var lastIndex = theStack.indexOf(pro.vt5)
                var nextIndex = if (lastIndex == theStack.size - 1) 0 else lastIndex + 1
                val vt6 = theStack.get(nextIndex)
                vt6.resetStartsWith(bufferSlideable, vt6.turnPriority, vt6.getLength(false))

                // tidy up the stack
                theStack.remove(pro.vt1)
                theStack.remove(pro.vt2)
                theStack.remove(pro.vt4)
                theStack.remove(pro.vt5)
                val point = theStack.indexOf(pro.vt3)
                theStack[point] = vtNew

                afterChange(c, pq, theStack, point-4)
                return
            }
        }

        super.performChange(c, pq, onStack, ro, theStack);
    }

    private fun otherEndOf(vt: VertexTurn, end: Slideable) : ElementSlideable {
        if (vt.endsWith == end) {
            return vt.startsWith
        } else if (vt.startsWith == end) {
            return vt.endsWith
        } else {
            throw LogicException("Couldn't find end")
        }
    }
}