package org.kite9.diagram.common.algorithms.so

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException

/**
 * Holds a bunch of [Slideable]s and can return them in position order, which is not exact, because
 * sometimes they can overlap one another.
 *
 * @author robmoffat
 */
abstract class AbstractSlackOptimisation : Logable, SlackOptimisation {

    override val log by lazy { Kite9Log.instance(this) }

    override var pushCount = 0

    protected val slideables: MutableCollection<Slideable> = LinkedHashSet()

    override fun getSize(): Int {
        return slideables.size
    }

    override fun getAllSlideables(): Collection<Slideable> {
        return slideables
    }

    override fun ensureMinimumDistance(left: Slideable, right: Slideable, minLength: Int) {
        if (left.so !== right.so) {
            throw LogicException("Mixing dimensions")
        }

        try {
            left.addMinimumForwardConstraint(right, minLength)
            right.addMaximumForwardConstraint(left, minLength)
            log.send(if (log.go()) null else "Updated min distance to $minLength for $left to $right")
        } catch (e: LogicException) {
            debugOutput(true)
        }
    }

    private fun debugOutput(minimums: Boolean) {
        val alreadyDone: MutableSet<Slideable> = HashSet()
        for (slideable in slideables) {
            if (!alreadyDone.contains(slideable)) {
                debugOutputSlideable(minimums, slideable, alreadyDone, 0)
            }
        }
    }

    private fun debugOutputSlideable(
        minimums: Boolean,
        slideable: Slideable,
        alreadyDone: MutableSet<Slideable>,
        indent: Int
    ) {
        log.send(indent, slideable.toString())
        if (!alreadyDone.contains(slideable)) {
            alreadyDone.add(slideable)
            for (s2 in slideable.getForwardSlideables(minimums)) {
                debugOutputSlideable(minimums, s2, alreadyDone, indent + 2)
            }
        }
    }

    override fun ensureMaximumDistance(left: Slideable, right: Slideable, maxLength: Int) {
        if (left.so !== right.so) {
            throw LogicException("Mixing dimensions")
        }
        try {
            log.send(if (log.go()) null else "Updating max distance to $maxLength for $left to $right")
            right.addMinimumBackwardConstraint(left, maxLength)
            left.addMaximumBackwardConstraint(right, maxLength)
        } catch (e: LogicException) {
            debugOutput(false)
        }
    }


    override val prefix: String
        get() = "ASO "

    override val isLoggingEnabled: Boolean
        get() = true

    abstract fun initialiseSlackOptimisation()
}