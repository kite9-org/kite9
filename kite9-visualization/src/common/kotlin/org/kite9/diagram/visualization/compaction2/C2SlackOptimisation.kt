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

interface SlideableSet {

    fun getAll() : Collection<C2Slideable>

    fun merge(ss: SlideableSet, c2: C2SlackOptimisation) : SlideableSet

    fun replace(s: C2Slideable, with: C2Slideable) : SlideableSet
}
interface RectangularSlideableSet : SlideableSet {
    fun getRectangularSlideables(): Collection<C2Slideable>

    fun getRectangularsOnSide(s: Side) : Collection<C2Slideable> = getRectangularSlideables()
        .filter { rs ->  rs.anchors.filter { it.s == s }.firstOrNull() != null }
}

interface RoutableSlideableSet : RectangularSlideableSet {

    val c: C2Slideable?
    val bl: C2Slideable
    val br: C2Slideable

    override fun merge(after: SlideableSet, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun mergeWithAxis(after: SlideableSet, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun getBufferSlideables() : Collection<C2Slideable>
}

data class RectangularSlideableSetImpl(private val all: Collection<C2Slideable>) : RectangularSlideableSet {

    override fun merge(ss: SlideableSet, c2: C2SlackOptimisation): SlideableSet {
        val newAll = setOf(all, ss.getAll()).flatten()
        return RectangularSlideableSetImpl(newAll)
    }

    override fun replace(s: C2Slideable, with: C2Slideable): SlideableSet {
        val newColl = replaceIfPresent(all, s, with)
        return RectangularSlideableSetImpl(newColl)
    }

    override fun getRectangularSlideables(): Collection<C2Slideable> {
        return all
    }

    override fun getAll(): Collection<C2Slideable> {
        return all
    }

    companion object {
        fun <X> replaceIfPresent(s: Collection<X>, o: X, n: X): Collection<X> {
            if (s.contains(o)) {
                return s.minus(o).plus(n)
            } else {
                return s
            }
        }
    }
}


data class RoutableSlideableSetImpl(val rs: Collection<C2Slideable>,
                                    val bs: Collection<C2Slideable>,
                                    override val c: C2Slideable?,
                                    override val bl: C2Slideable,
                                    override val br: C2Slideable,

    ) : RectangularSlideableSet, RoutableSlideableSet {

    override fun merge(after: SlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (after is RoutableSlideableSet) {
            val allRs = setOf(rs, after.getRectangularSlideables()).flatten()
            val allBs = setOf(bs, after.getBufferSlideables()).flatten()
            val newC = c2.mergeSlideables(br, after.bl)
            RoutableSlideableSetImpl(allRs, allBs, newC, bl, after.br)
        } else {
            val allRs = setOf(rs, after.getAll()).flatten()
            RoutableSlideableSetImpl(allRs, bs, c, bl, br)
        }
    }

    override fun mergeWithAxis(ss: SlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (ss is RoutableSlideableSet) {
            val allRs = setOf(rs, ss.getRectangularSlideables()).flatten()
            val allBs = setOf(bs, ss.getBufferSlideables()).flatten()
            val newL = c2.mergeSlideables(ss.bl, bl)
            val newR = c2.mergeSlideables(ss.br,br)
            val newC = c2.mergeSlideables(ss.c, c)
            RoutableSlideableSetImpl(allRs, allBs, newC, newL, newR)
        } else {
            val allRs = setOf(rs, ss.getAll()).flatten()
            RoutableSlideableSetImpl(allRs, bs, c, bl, br)
        }
    }

    override fun getBufferSlideables(): Collection<C2Slideable> {
        return bs
    }

    override fun getRectangularSlideables(): Collection<C2Slideable> {
        return rs
    }

    override fun getAll(): Collection<C2Slideable> {
        return listOf(rs, bs).flatten()
    }

    override fun replace(s: C2Slideable, with: C2Slideable): RoutableSlideableSetImpl {
        return RoutableSlideableSetImpl(
            RectangularSlideableSetImpl.replaceIfPresent(rs, s, with),
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
    private val elementMap: MutableMap<DiagramElement, SlideableSet> = HashMap()
    private val groupMap: MutableMap<Group, SlideableSet> = HashMap()
    private val slideableMap: MutableMap<C2Slideable, MutableSet<SlideableSet>> = HashMap()

    private fun isRectangular(underlying: DiagramElement): Boolean {
        return underlying is Rectangular
    }

    private fun isConnected(underlying: DiagramElement): Boolean {
        return underlying is Connected
    }

    override fun initialiseSlackOptimisation() {

    }

    fun getSlideablesFor(de: DiagramElement): SlideableSet? {
        return elementMap[de]
    }

    fun getSlideablesFor(group: Group) : SlideableSet? {
        return groupMap[group]
    }

    fun mergeSlideables(s1: C2Slideable?, s2: C2Slideable?) : C2Slideable {
        if (s1 == null) {
            return s2!!;
        } else if (s2 == null) {
            return s1;
        } else if (s1.purpose == s2.purpose) {
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
        } else {
            throw LogicException("Can't merge slideables $s1 $s2")
        }
    }

    private fun updateMaps(
        contains: MutableSet<SlideableSet>?,
        sOld: C2Slideable,
        sNew: C2Slideable
    ) {
        contains?.forEach {
            val ssNew = it.replace(sOld, sNew)
            updateSlideableMap(ssNew)

            val toReplaceDiagramElements = elementMap.filter { (_, v) -> v == it }.keys
            toReplaceDiagramElements.forEach { d -> elementMap.put(d, ssNew) }

            val toReplaceGroups = groupMap.filter { (_, v) -> v == it }.keys
            toReplaceGroups.forEach { g -> groupMap.put(g, ssNew) }

        }
    }

    fun add(de: DiagramElement, ss: RectangularSlideableSet) {
        elementMap[de] = ss
        updateSlideableMap(ss)
    }

    private fun updateSlideableMap(ss: SlideableSet) {
        ss.getAll().forEach {
            val set = slideableMap.getOrElse(it) { mutableSetOf() }
            set.add(ss)
            slideableMap[it] = set
            slideables.add(it)
        }
    }

    fun add(g: Group, ss: RoutableSlideableSet) {
        groupMap[g] = ss;
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

