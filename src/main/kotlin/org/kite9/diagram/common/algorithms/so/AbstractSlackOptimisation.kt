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
abstract class AbstractSlackOptimisation<X> : Logable {
    val log = Kite9Log(this)
    var pushCount = 0

    protected var _allSlideables: MutableCollection<Slideable<X>> = LinkedHashSet()

    abstract fun getIdentifier(underneath: Any?): String?

    fun getSize(): Int {
        return _allSlideables.size
    }

    fun getAllSlideables(): Collection<Slideable<X>> {
        return _allSlideables
    }

    fun ensureMinimumDistance(left: Slideable<X>, right: Slideable<X>, minLength: Int) {
        if (left.slackOptimisation !== right.slackOptimisation) {
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
        val alreadyDone: MutableSet<Slideable<X>> = HashSet()
        for (slideable in _allSlideables) {
            if (!alreadyDone.contains(slideable)) {
                debugOutputSlideable(minimums, slideable, alreadyDone, 0)
            }
        }
    }

    private fun debugOutputSlideable(
        minimums: Boolean,
        slideable: Slideable<X>,
        alreadyDone: MutableSet<Slideable<X>>,
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

    fun ensureMaximumDistance(left: Slideable<X>, right: Slideable<X>, maxLength: Int) {
        if (left.slackOptimisation !== right.slackOptimisation) {
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

//    open fun addSlideables(s: Collection<Slideable<X>>) {
//        for (slideable in s) {
//            _allSlideables.add(slideable)
//        }
//        for (slideable in s) {
//            addedSlideable(slideable)
//        }
//    }
//
//    open fun addSlideables(vararg s: Slideable<X>) {
//        for (slideable in s) {
//            _allSlideables.add(slideable)
//        }
//        for (slideable in s) {
//            addedSlideable(slideable)
//        }
//    }

    //protected abstract fun addedSlideable(s: Slideable<X>?)

    override val prefix: String
        get() = "ASO "

    override val isLoggingEnabled: Boolean
        get() = false

    fun getSelf(): AbstractSlackOptimisation<X> {
        return this
    }

    abstract fun initialiseSlackOptimisation()
}