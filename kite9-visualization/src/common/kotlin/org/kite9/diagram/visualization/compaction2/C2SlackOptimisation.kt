package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

interface SlideableSet<X : SlideableSet<X>> {

    fun getAll() : Set<C2Slideable>

    fun replace(s: C2RectangularSlideable, with: C2RectangularSlideable) : X

    fun replace(s: C2BufferSlideable, with: C2BufferSlideable) : X

    fun replaceGeneric(s: C2Slideable, with: C2Slideable) : X {
        return if ((s is C2RectangularSlideable) && (with is C2RectangularSlideable)) {
            replace(s, with)
        } else if ((s is C2BufferSlideable) && (with is C2BufferSlideable)) {
            replace(s, with)
        } else {
            throw LogicException("Can only replace slideable with same type $s $with")
        }
    }
}
interface RectangularSlideableSet : SlideableSet<RectangularSlideableSet> {

    val d: Rectangular
    val l: C2RectangularSlideable
    val r: C2RectangularSlideable
    val c: C2BufferSlideable

    fun getRectangularSlideables(): Collection<C2RectangularSlideable>

    fun getRectangularOnSide(s: Side) : C2RectangularSlideable = getRectangularSlideables()
        .first { rs ->  rs.anchors.firstOrNull { (it.s == s) && (it.e == d) } != null }

}

interface RoutableSlideableSet : SlideableSet<RoutableSlideableSet> {

    val c: C2BufferSlideable?
    val bl: C2BufferSlideable
    val br: C2BufferSlideable

    fun merge(after: SlideableSet<*>, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun mergeWithAxis(after: SlideableSet<*>, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun getBufferSlideables() : Set<C2BufferSlideable>

    fun getRectangularSlideableSets() : Set<RectangularSlideableSet>

    fun getRectangularsOnSide(s: Side) : Collection<C2RectangularSlideable> = getRectangularSlideableSets()
        .map { it.getRectangularOnSide(s) }

}

data class RectangularSlideableSetImpl(
    override val d: Rectangular,
    override val l: C2RectangularSlideable,
    override val r: C2RectangularSlideable,
    override val c: C2BufferSlideable) : RectangularSlideableSet {

    override fun replace(s: C2RectangularSlideable, with: C2RectangularSlideable): RectangularSlideableSet {
        return RectangularSlideableSetImpl(
            d,
            if (l == s) with else l,
            if (r == s) with else r,
            c
        )
    }

    override fun replace(s: C2BufferSlideable, with: C2BufferSlideable): RectangularSlideableSet {
        return RectangularSlideableSetImpl(
            d,
            l,
            r,
            if (c == s) with else c
        )
    }

    override fun getRectangularSlideables(): Collection<C2RectangularSlideable> {
        return setOf(l, r)
    }

    override fun getAll(): Set<C2Slideable> {
        return setOf(l, r, c)
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
}


data class RoutableSlideableSetImpl(val rs: Set<RectangularSlideableSet>,
                                    val bs: Set<C2BufferSlideable>,
                                    override val c: C2BufferSlideable?,
                                    override val bl: C2BufferSlideable,
                                    override val br: C2BufferSlideable,

    ) : RoutableSlideableSet {

    override fun merge(after: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (after is RoutableSlideableSet) {
            val allRs = setOf(rs, after.getRectangularSlideableSets()).flatten().toSet()
            val allBs = setOf(bs, after.getBufferSlideables()).flatten().toSet()
            val newC = c2.mergeSlideables(br, after.bl)
            RoutableSlideableSetImpl(allRs, allBs, newC, bl, after.br)
        } else if (after is RectangularSlideableSet) {
            val allRs = this.getRectangularSlideableSets().plus(after)
            RoutableSlideableSetImpl(allRs, bs, c, bl, br)
        } else {
            throw LogicException("unsupported")
        }
    }

    override fun mergeWithAxis(ss: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (ss is RoutableSlideableSet) {
            val allRs = setOf(rs, ss.getRectangularSlideableSets()).flatten().toSet()
            val allBs = setOf(bs, ss.getBufferSlideables()).flatten().toSet()
            val newL = c2.mergeSlideables(ss.bl, bl)
            val newR = c2.mergeSlideables(ss.br, br)
            val newC = c2.mergeSlideables(ss.c, c)
            RoutableSlideableSetImpl(allRs, allBs, newC, newL, newR)
        } else if (ss is RectangularSlideableSet) {
            val allRs = this.getRectangularSlideableSets().plus(ss)
            val newC = c2.mergeSlideables(ss.c, c)
            RoutableSlideableSetImpl(allRs, bs, newC, bl, br)
        } else {
            throw LogicException("unsupported")
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

    override fun replace(s: C2BufferSlideable, with: C2BufferSlideable): RoutableSlideableSetImpl {
        return RoutableSlideableSetImpl(
            rs,
            RectangularSlideableSetImpl.replaceIfPresent(bs, s, with),
            if (c == s) with else c,
            if (bl == s)with else bl,
            if (br == s)with else br)
    }
}



/**
 * Augments SlackOptimisation to keep track of diagram elements underlying the slideables.
 * @author robmoffat
 */
class C2SlackOptimisation(private val theDiagram: Diagram) : AbstractSlackOptimisation(), Logable {
    private val elementMap: MutableMap<DiagramElement, RectangularSlideableSet> = HashMap()
    private val groupMap: MutableMap<Group, SlideableSet<*>> = HashMap()
    private val slideableMap: MutableMap<C2Slideable, MutableSet<SlideableSet<*>>> = HashMap()

    private fun isRectangular(underlying: DiagramElement): Boolean {
        return underlying is Rectangular
    }

    private fun isConnected(underlying: DiagramElement): Boolean {
        return underlying is Connected
    }

    override fun initialiseSlackOptimisation() {

    }

    fun getAllElements() : Set<DiagramElement> {
        return elementMap.keys
    }

    fun getSlideablesFor(de: DiagramElement): RectangularSlideableSet? {
        return elementMap[de]
    }

    fun getSlideablesFor(group: Group) : SlideableSet<*>? {
        return groupMap[group]
    }

    fun mergeSlideables(s1: C2BufferSlideable?, s2: C2BufferSlideable?) : C2BufferSlideable {
        if (s1 == null) {
            return s2!!
        } else if (s2 == null) {
            return s1
        } else {
            val sNew = s1.merge(s2)
            // now we need to replace s1 and s2 in their containers
            val containsS1 = slideableMap.remove(s1)
            val containsS2 = slideableMap.remove(s2)

            updateMaps(containsS1, s1, sNew)
            updateMaps(containsS2, s2, sNew)

            slideables.add(sNew)
            slideables.remove(s1)
            slideables.remove(s2)

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

            updateMaps(containsS1, s1, sNew)
            updateMaps(containsS2, s2, sNew)

            slideables.add(sNew)
            slideables.remove(s1)
            slideables.remove(s2)

            log.send("Merging: \n\t$s1\n\t$s2\nAdded: $sNew")

            return sNew
        }
    }

    private fun updateMaps(
        contains: MutableSet<SlideableSet<*>>?,
        sOld: C2Slideable,
        sNew: C2Slideable
    ) {
        contains?.forEach {
            val ssNew : SlideableSet<*> = it.replaceGeneric(sOld, sNew)
            updateSlideableMap(ssNew)

            val toReplaceDiagramElements = elementMap.filter { (_, v) -> v == it }.keys
            toReplaceDiagramElements.forEach { d -> elementMap.put(d, ssNew as RectangularSlideableSet) }

            val toReplaceGroups = groupMap.filter { (_, v) -> v == it }.keys
            toReplaceGroups.forEach { g -> groupMap.put(g, ssNew as RoutableSlideableSet) }

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

    fun add(g: Group, ss: SlideableSet<*>) {
        groupMap[g] = ss
        updateSlideableMap(ss)
    }

    fun checkValid(s: C2Slideable, k: Any) {
        if (!slideables.contains(s)) {
            throw LogicException("Wasn't expecting reference to $s for key $k")
        }
    }

    fun checkConsistency() {
        elementMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        groupMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        slideables.flatMap { it.getForwardSlideables(true) }
            .forEach { checkValid(it as C2Slideable, "*") }

        slideables.flatMap { it.getForwardSlideables(false) }
            .forEach { checkValid(it as C2Slideable, "*") }

        log.send("Consistent")
    }
}

