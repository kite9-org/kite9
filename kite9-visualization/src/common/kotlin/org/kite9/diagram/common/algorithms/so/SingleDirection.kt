package org.kite9.diagram.common.algorithms.so

import org.kite9.diagram.logging.LogicException
import kotlin.math.abs

/**
 * Handles the constraints for a [SegmentSlideable] in a single direction (e.g.
 * increasing or decreasing).
 *
 * This is a DAG, and we should be able to prove it.
 *
 * @author robmoffat
 */
class SingleDirection(
    val owner: PositionChangeNotifiable,
    private val increasing: Boolean) {

    var position: Int = if (increasing) 0 else 1000000
        private set

    var forward: MutableMap<SingleDirection, Int> = LinkedHashMap()
    var backward: MutableMap<SingleDirection, Int> = LinkedHashMap()

    private var cachePosition: Int? = null
    private var cacheItem: Any? = null

    internal class QuitOnChange {
        var on: SingleDirection? = null
    }

    private fun update(newPos: Int, ci: Any?, changedConstraints: Boolean): Boolean {
        return try {
            if (cacheItem === ci && ci is QuitOnChange && ci.on === this) {
                // we've visited here before - return false if we move
                return if (increasing) cachePosition!! >= newPos else cachePosition!! <= newPos
            }
            if (cacheItem !== ci) {
                cacheItem = ci
                cachePosition = position
            }
            val moved = cachePosition == null || if (increasing) cachePosition!! < newPos else cachePosition!! > newPos
            var ok = true
            if (moved || changedConstraints) {
//				System.out.println("moving: "+this+" to "+newPos);
                cachePosition = newPos
                //				System.out.println("(fwd)");
                for (fwd in forward.keys) {
                    val dist = forward[fwd]!!
                    val newPositionFwd = if (increasing) cachePosition!! + dist else cachePosition!! - dist
                    ok = ok && fwd.update(newPositionFwd, ci, false)
                }

//				System.out.println("(bck)");
                for (bck in backward.keys) {
                    val dist = backward[bck]
                    val newPositionBck = if (increasing) cachePosition!! - dist!! else cachePosition!! + dist!!
                    ok = ok && bck.update(newPositionBck, ci, false)
                }
                //				System.out.println("(done)");
                if (ci == null) {
                    position = cachePosition!!
                    owner.changedPosition(position)
                }
            }
            ok
        } catch (e: Exception) {
            throw LogicException("Couldn't adjust (SO): $this pos: $position cachePos: $cachePosition")
        }
    }

    fun increasePosition(pos: Int) {
        update(pos, null, false)
    }

    /**
     * Works out minimum distance to ci, given that our item is in a certain start position.
     * Returns null if the elements aren't connected.
     */
    fun minimumDistanceTo(ci: SingleDirection, startPosition: Int): Int? {
        val cacheMarker = Any()
        update(startPosition, cacheMarker, false)
        return if (ci.cacheItem !== cacheMarker) {
            // the two elements are independent, one doesn't push the other.
            null
        } else abs(ci.cachePosition!! - startPosition)
    }

    fun canAddForwardConstraint(to: SingleDirection, distance: Int): Boolean {
        val existing = forward[to]
        val qoc = QuitOnChange()
        qoc.on = this
        if (existing == null || existing < distance) {
            val curPos: Int
            curPos = position
            update(curPos, qoc, false)
            val newPos = if (increasing) curPos + distance else curPos - distance
            return to.update(newPos, qoc, true)
        }
        return true
    }

    fun addForwardConstraint(to: SingleDirection, distance: Int) {
        val existing = forward[to]
        if (existing == null || existing < distance) {
            forward[to] = distance
            update(position, null, true)
        }
    }

    fun addBackwardConstraint(to: SingleDirection, distance: Int) {
        val existing = backward[to]
        if (existing == null || existing > distance) {
            backward[to] = distance
            update(position, null, true)
        }
    }

    override fun toString(): String {
        return owner.toString()
    }

    fun merge(sd: SingleDirection) {
        sd.forward.forEach { (k, v) -> this.addForwardConstraint(k, v) }
        sd.backward.forEach { (k, v) -> this.addBackwardConstraint(k, v) }
    }

    val maxDepth: Int
        get() {
            var depth = 0
            for (fwd in forward.keys) {
                depth = depth.coerceAtLeast(fwd.maxDepth + 1)
            }
            for (back in backward.keys) {
                depth = depth.coerceAtLeast(back.maxDepth + 1)
            }
            return depth
        }
}