package org.kite9.diagram.common.algorithms.so

import org.kite9.diagram.logging.LogicException
import kotlin.math.max

open class Slideable(val so: SlackOptimisation) : PositionChangeNotifiable {
    protected val minimum = SingleDirection(this, true)
    protected val maximum = SingleDirection(this, false)
    private var hasBackwardConstraints = false
    var minimumPosition: Int
        get() = minimum.position
        set(i) {
            minimum.increasePosition(i!!)
        }
    var maximumPosition: Int?
        get() = maximum.position
        set(i) {
            maximum.increasePosition(i!!)
        }

    /**
     * Works out how much closer the current slideable can get to s.
     * This works by fixing the position for s (temporarily, using the cache)
     * and then taking the max position for *this*.
     *
     * We can't move the element any further than the max position without breaking other
     * constraints.
     */
    fun minimumDistanceTo(s: Slideable): Int {
        return try {
            var maxSet = maximumPosition
            maxSet = maxSet ?: 20000 //
            val slack1 = minimum.minimumDistanceTo(s.minimum, maxSet)
            so.log.send("Calculating minimum distance from $this to $s $slack1")
            val slack2 = s.maximum.minimumDistanceTo(maximum, s.minimumPosition)
            so.log.send("Calculating minimum distance from $s to $this $slack2")
            if (slack2 == null) {
                slack1 ?: 0
            } else if (slack1 == null) {
                slack2
            } else {
                max(slack1, slack2)
            }
        } catch (e: NullPointerException) {
            throw LogicException("Some maximum size not set")
        }
    }

    override fun changedPosition(pos: Int) {
        so.pushCount++
        val min = minimumPosition
        val max = maximumPosition
        if ((max != null) && (min > max)) {
            throw LogicException("Min $min > Max $max: $this")
        }
    }

    fun canAddMinimumForwardConstraint(to: Slideable, dist: Int): Boolean {
        return minimum.canAddForwardConstraint(to.minimum, dist)
    }

    fun addMinimumForwardConstraint(to: Slideable, dist: Int) {
        try {
            minimum.addForwardConstraint(to.minimum, dist)
        } catch (e: RuntimeException) {
            throw SlideableException("addMinimumForwardConstraint: $this to $to dist: $dist", e)
        }
    }

    fun addMinimumBackwardConstraint(to: Slideable, dist: Int) {
        try {
            minimum.addBackwardConstraint(to.minimum, dist)
            hasBackwardConstraints = true
        } catch (e: RuntimeException) {
            throw SlideableException("addMinimumBackwardConstraint: $this to $to dist: $dist", e)
        }
    }

    fun addMaximumForwardConstraint(to: Slideable, dist: Int) {
        try {
            maximum.addForwardConstraint(to.maximum, dist)
        } catch (e: RuntimeException) {
            throw SlideableException("addMaximumForwardConstraint: $this to $to dist: $dist", e)
        }
    }

    fun addMaximumBackwardConstraint(to: Slideable, dist: Int) {
        try {
            maximum.addBackwardConstraint(to.maximum, dist)
            hasBackwardConstraints = true
        } catch (e: RuntimeException) {
            throw SlideableException("addMaximumBackwardConstraint: $this to $to dist: $dist", e)
        }
    }

    fun getForwardSlideables(increasing: Boolean): Set<Slideable> {
        val out: MutableSet<Slideable> = HashSet()
        if (increasing) {
            for (sd in minimum.forward.keys) {
                out.add(sd.owner as Slideable)
            }
        } else {
            for (sd in maximum.forward.keys) {
                out.add(sd.owner as Slideable)
            }
        }
        return out
    }
}