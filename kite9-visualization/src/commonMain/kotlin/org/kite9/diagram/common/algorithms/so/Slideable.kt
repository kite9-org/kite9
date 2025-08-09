package org.kite9.diagram.common.algorithms.so

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.compaction2.C2Slideable
import kotlin.math.max

abstract class Slideable(val so: SlackOptimisation,
    ) : PositionChangeNotifiable {

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

    fun getMinimumForwardConstraintTo(d: Slideable) : Int? {
        return this.minimum.forward.get(d.minimum)
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
        } catch (e: Throwable) {
            throw SlideableException("addMinimumForwardConstraint: $this to $to dist: $dist", e)
        }
    }

    fun addMinimumBackwardConstraint(to: Slideable, dist: Int) {
        try {
            minimum.addBackwardConstraint(to.minimum, dist)
            hasBackwardConstraints = true
        } catch (e: Throwable) {
            throw SlideableException("addMinimumBackwardConstraint: $this to $to dist: $dist", e)
        }
    }

    fun replaceConstraint(was: Slideable, now: Slideable) {
        minimum.replaceForwardConstraint(was.minimum, now.minimum)
        minimum.replaceBackwardConstraint(was.minimum, now.minimum)
        maximum.replaceForwardConstraint(was.maximum, now.maximum)
        maximum.replaceBackwardConstraint(was.maximum, now.maximum)
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

    fun getForwardSlideables(increasing: Boolean): Set<C2Slideable> {
        val out: MutableSet<C2Slideable> = HashSet()
        if (increasing) {
            for (sd in minimum.forward.keys) {
                out.add(sd.owner as C2Slideable)
            }
        } else {
            for (sd in maximum.forward.keys) {
                out.add(sd.owner as C2Slideable)
            }
        }
        return out
    }

    fun outputConstraints() {
        println("maximum: \n${maximum}\nminimum: ${minimum}")
    }
}