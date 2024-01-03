package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

interface SlideableSet<X : SlideableSet<X>> {

    fun getAll() : Set<C2Slideable>

    fun replace(s: C2RectangularSlideable, with: C2RectangularSlideable) : X

    fun replace(s: C2IntersectionSlideable, with: C2IntersectionSlideable) : X

    fun replaceGeneric(s: C2Slideable, with: C2Slideable) : X {
        return if ((s is C2RectangularSlideable) && (with is C2RectangularSlideable)) {
            replace(s, with)
        } else if ((s is C2IntersectionSlideable) && (with is C2IntersectionSlideable)) {
            replace(s, with)
        } else {
            throw LogicException("Can only replace slideable with same type $s $with")
        }
    }

    fun getRectangularsOnSide(s: Side) : Set<C2RectangularSlideable>

    fun getBufferSlideables() : Set<C2BufferSlideable>

    fun mergeWithOverlap(over: SlideableSet<*>, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun mergeWithGutter(after: SlideableSet<*>, c2: C2SlackOptimisation) :  RoutableSlideableSet

}
interface RectangularSlideableSet : SlideableSet<RectangularSlideableSet> {

    val d: Rectangular
    val l: C2RectangularSlideable
    val r: C2RectangularSlideable
    val c: C2IntersectionSlideable?

    fun getRectangularSlideables(): Collection<C2RectangularSlideable>

    override fun getBufferSlideables() : Set<C2BufferSlideable> = setOfNotNull(c)

    fun getRectangularOnSide(s: Side) : C2RectangularSlideable {
        if (getRectangularsOnSide(s).size == 1) {
            return getRectangularsOnSide(s).first()
        } else {
            throw LogicException("Was expecting single rectangular ")
        }
    }


    fun wrapInRoutable(so: C2SlackOptimisation) : RoutableSlideableSet

}

interface RoutableSlideableSet : SlideableSet<RoutableSlideableSet> {

    val c: C2IntersectionSlideable?
    val bl: C2OrbitSlideable
    val br: C2OrbitSlideable

    fun getRectangularSlideableSets() : Set<RectangularSlideableSet>

    fun replace(s: C2OrbitSlideable, with: C2OrbitSlideable) : RoutableSlideableSet

    override fun replaceGeneric(s: C2Slideable, with: C2Slideable) : RoutableSlideableSet {
        return if ((s is C2OrbitSlideable) && (with is C2OrbitSlideable)) {
            replace(s, with)
        } else {
            super.replaceGeneric(s, with)
        }
    }

}

data class RectangularSlideableSetImpl(
    override val d: Rectangular,
    override val l: C2RectangularSlideable,
    override val r: C2RectangularSlideable,
    override val c: C2IntersectionSlideable?) : RectangularSlideableSet {

    override fun replace(s: C2RectangularSlideable, with: C2RectangularSlideable): RectangularSlideableSet {
        return RectangularSlideableSetImpl(
            d,
            if (l == s) with else l,
            if (r == s) with else r,
            c
        )
    }

    override fun replace(s: C2IntersectionSlideable, with: C2IntersectionSlideable): RectangularSlideableSet {
        return RectangularSlideableSetImpl(
            d,
            l,
            r,
            if (c == s) with else c
        )
    }

    override fun mergeWithOverlap(over: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (over is RoutableSlideableSet) {
            val allRs = setOf(setOf(this), over.getRectangularSlideableSets()).flatten().toSet()
            val allBs = setOf(getBufferSlideables(), over.getBufferSlideables()).flatten().toSet()
            val newC = c2.mergeSlideables(over.c, c)
            RoutableSlideableSetImpl(allRs, allBs, newC, over.bl, over.br)
        } else {
            throw LogicException("unsupported")
        }
    }

    override fun mergeWithGutter(after: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (after is RoutableSlideableSet) {
            val allRs = setOf(setOf(this), after.getRectangularSlideableSets()).flatten().toSet()
            val allBs = setOf(getBufferSlideables(), after.getBufferSlideables()).flatten().toSet()
            val newC = c2.mergeSlideables(this.c, c)
            RoutableSlideableSetImpl(allRs, allBs, newC, after.bl, after.br)
        } else {
            throw LogicException("unsupported")
        }
    }

    override fun getRectangularSlideables(): Collection<C2RectangularSlideable> {
        return setOf(l, r)
    }

    override fun getAll(): Set<C2Slideable> {
        return setOfNotNull(l, r, c)
    }

    companion object {
        fun <X> replaceIfPresent(s: Set<X>, o: X, n: X): Set<X> {
            return if (s.contains(o)) {
                s.minus(o).plus(n)
            } else {
                s
            }
        }
    }

    override fun getRectangularsOnSide(s: Side): Set<C2RectangularSlideable> {
        return when (s) {
            Side.START -> setOf(l)
            Side.END -> setOf(r)
            else -> setOf()
        }
    }

    override fun wrapInRoutable(so: C2SlackOptimisation): RoutableSlideableSet {
        val bl = C2OrbitSlideable(so, l.dimension, setOf(d))
        val br = C2OrbitSlideable(so, l.dimension, setOf(d))

        so.ensureMinimumDistance(bl, l, 0)
        so.ensureMinimumDistance(r, br, 0)

        return RoutableSlideableSetImpl(
            setOf(this),
            setOfNotNull(bl, c, br),
            c,
            bl,
            br)
    }
}


data class RoutableSlideableSetImpl(val rs: Set<RectangularSlideableSet>,
                                    val bs: Set<C2BufferSlideable>,
                                    override val c: C2IntersectionSlideable?,
                                    override val bl: C2OrbitSlideable,
                                    override val br: C2OrbitSlideable,
                                    ) : RoutableSlideableSet {

    override fun mergeWithGutter(after: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return when (after) {

            is RoutableSlideableSet -> {
                val allRs = setOf(rs, after.getRectangularSlideableSets()).flatten().toSet()
                val newOrbit = c2.mergeSlideables(br, after.bl)
                val allBs = setOf(bs, after.getBufferSlideables()).asSequence().flatten().minus(br).minus(after.bl).plus(newOrbit).toSet()
                RoutableSlideableSetImpl(allRs, allBs, null, bl, after.br)
            }

            is RectangularSlideableSet -> {
                val allRs = this.getRectangularSlideableSets().plus(after)
                RoutableSlideableSetImpl(allRs, bs, c, bl, br)
            }
            else -> throw LogicException("unsupported")
        }
    }

    override fun mergeWithOverlap(over: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return when (over) {

            is RoutableSlideableSet -> {
                val allRs = setOf(rs, over.getRectangularSlideableSets()).flatten().toSet()
                val newL = c2.mergeSlideables(over.bl, bl)
                val newR = c2.mergeSlideables(over.br, br)
                val newC = c2.mergeSlideables(over.c, c)
                RoutableSlideableSetImpl(allRs, setOfNotNull(newL, newR, newC), newC, newL, newR)
            }

            is RectangularSlideableSet -> {
                val allRs = this.getRectangularSlideableSets().plus(over)
                val newC = c2.mergeSlideables(over.c, c)
                RoutableSlideableSetImpl(allRs, bs, newC, bl, br)
            }

            else -> throw LogicException("unsupported")
        }
    }

    override fun getBufferSlideables(): Set<C2BufferSlideable> {
        return bs
    }

    override fun getRectangularSlideableSets(): Set<RectangularSlideableSet> {
        return rs
    }

    override fun getAll(): Set<C2Slideable> {
        return rs.flatMap { it.getRectangularSlideables() }.plus(bs).toSet()
    }

    override fun replace(s: C2RectangularSlideable, with: C2RectangularSlideable): RoutableSlideableSetImpl {
        return RoutableSlideableSetImpl(
            rs.map { k -> k.replace(s, with) }.toSet(),
            bs,
            c,
            bl,
            br)
    }

    override fun replace(s: C2OrbitSlideable, with: C2OrbitSlideable): RoutableSlideableSetImpl {
        return RoutableSlideableSetImpl(
            rs,
            RectangularSlideableSetImpl.replaceIfPresent(bs, s, with),
            c,
            if (bl == s) with else bl,
            if (br == s) with else br)
    }

    override fun replace(s: C2IntersectionSlideable, with: C2IntersectionSlideable): RoutableSlideableSetImpl {
        return RoutableSlideableSetImpl(
            rs,
            RectangularSlideableSetImpl.replaceIfPresent(bs, s, with),
            if (c == s) with else c,
            bl,
            br)
    }

    override fun getRectangularsOnSide(s: Side) : Set<C2RectangularSlideable> = getRectangularSlideableSets()
        .flatMap { it.getRectangularsOnSide(s) }
        .toSet()

}



/**
 * Augments SlackOptimisation to keep track of diagram elements underlying the slideables.
 * @author robmoffat
 */
class C2SlackOptimisation : AbstractSlackOptimisation(), Logable {

    private val elementMap: MutableMap<DiagramElement, RectangularSlideableSet> = HashMap()
    private val groupMap: MutableMap<Group, RoutableSlideableSet> = HashMap()
    private val slideableMap: MutableMap<C2Slideable, MutableSet<SlideableSet<*>>> = HashMap()

    override fun initialiseSlackOptimisation() {

    }

    fun getAllElements() : Set<DiagramElement> {
        return elementMap.keys
    }

    fun getSlideablesFor(de: DiagramElement): RectangularSlideableSet? {
        return elementMap[de]
    }

    fun getSlideablesFor(group: Group) : RoutableSlideableSet? {
        return groupMap[group]
    }

    fun mergeSlideables(s1: C2IntersectionSlideable?, s2: C2IntersectionSlideable?) : C2IntersectionSlideable? {
        return mergeSlideablesInner(s1, s2) as C2IntersectionSlideable?
    }

    fun mergeSlideables(s1: C2OrbitSlideable, s2: C2OrbitSlideable) : C2OrbitSlideable {
        return mergeSlideablesInner(s1, s2) as C2OrbitSlideable
    }

    private fun mergeSlideablesInner(s1: C2BufferSlideable?, s2: C2BufferSlideable?) : C2BufferSlideable? {
        if (s1 == null) {
            return s2
        } else if (s2 == null) {
            return s1
        } else {
            val sNew = s1.merge(s2)
            // now we need to replace s1 and s2 in their containers
            val containsS1 = slideableMap.remove(s1)
            val containsS2 = slideableMap.remove(s2)
            containsS1!!.addAll(containsS2!!)
            updateMaps(containsS1, s1, s2, sNew)

            slideables.add(sNew)
            slideables.remove(s1)
            slideables.remove(s2)

            slideables.forEach {
                it.replaceConstraint(s1, sNew)
                it.replaceConstraint(s2, sNew)
            }

            log.send("Merging: \n\t$s1\n\t$s2\nAdded: $sNew")

            return sNew
        }
    }

    fun mergeSlideables(s1: C2RectangularSlideable?, s2: C2RectangularSlideable?) : C2RectangularSlideable {
        if (s1 == null) {
            return s2!!
        } else if (s2 == null) {
            return s1
        } else {
            val sNew = s1.merge(s2)
            // now we need to replace s1 and s2 in their containers
            val containsS1 = slideableMap.remove(s1)
            val containsS2 = slideableMap.remove(s2)
            containsS1!!.addAll(containsS2!!)

            updateMaps(containsS1, s1, s2, sNew)

            slideables.add(sNew)
            slideables.remove(s1)
            slideables.remove(s2)

            slideables.forEach {
                it.replaceConstraint(s1, sNew)
                it.replaceConstraint(s2, sNew)
            }

            log.send("Merging: \n\t$s1\n\t$s2\nAdded: $sNew")

            return sNew
        }
    }

    private fun updateMaps(
        contains: Set<SlideableSet<*>>?,
        sOld1: C2Slideable,
        sOld2: C2Slideable,
        sNew: C2Slideable
    ) {
        contains?.forEach {
            val ssNew : SlideableSet<*> = it.replaceGeneric(sOld1, sNew).replaceGeneric(sOld2, sNew)
            updateSlideableMap(ssNew)

            if (ssNew is RectangularSlideableSet) {
                val toReplaceDiagramElements = elementMap.filter { (_, v) -> v == it }.keys
                toReplaceDiagramElements.forEach { d -> elementMap[d] = ssNew }
            }

            if (ssNew is RoutableSlideableSet) {
                val toReplaceGroups = groupMap.filter { (_, v) -> v == it }.keys
                toReplaceGroups.forEach { g -> groupMap[g] = ssNew }
            }
        }
    }

    fun add(de: DiagramElement, ss: RectangularSlideableSet) {
        elementMap[de] = ss
        updateSlideableMap(ss)
    }

    private fun updateSlideableMap(ss: SlideableSet<*>) {
        ss.getAll().forEach {
            val set = slideableMap.getOrElse(it) { mutableSetOf() }
            set.add(ss)
            slideableMap[it] = set
            slideables.add(it)
        }
    }

    fun add(g: Group, ss: RoutableSlideableSet) {
        groupMap[g] = ss
        updateSlideableMap(ss)
    }

    private fun checkValid(s: C2Slideable, k: Any) {
        if (!slideables.contains(s)) {
            throw LogicException("Wasn't expecting reference to $s for key $k")
        }
    }

    fun checkConsistency() {
        slideables.removeAll { it is C2Slideable && it.done }

        elementMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        groupMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        slideables.flatMap { it.getForwardSlideables(true) }
            .forEach { checkValid(it as C2Slideable, "*") }

        slideables.flatMap { it.getForwardSlideables(false) }
            .forEach { checkValid(it as C2Slideable, "*") }

        log.send("Consistent")
    }

    fun remove(g: Group) {
        groupMap.remove(g)
    }
}

