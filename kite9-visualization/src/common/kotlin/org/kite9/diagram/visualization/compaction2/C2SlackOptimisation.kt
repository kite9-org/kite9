package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Positioned
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor


/**
 * Augments SlackOptimisation to keep track of diagram elements underlying the slideables.
 * @author robmoffat
 */
class C2SlackOptimisation(val compaction: C2CompactionImpl) : AbstractSlackOptimisation(), Logable {

    private val positionedMap: MutableMap<Positioned, RectangularSlideableSet> = HashMap()
    private val groupMap: MutableMap<Group, RoutableSlideableSet> = HashMap()
    private val slideableMap: MutableMap<C2Slideable, MutableSet<SlideableSet<*>>> = HashMap()
    private val containment: MutableMap<RoutableSlideableSet, MutableList<RectangularSlideableSet>> = HashMap()

    override fun initialiseSlackOptimisation() {

    }

    fun getRectangularsOnSide(s: Side, ss: SlideableSet<*>) : Set<C2RectangularSlideable> {
        return when (ss) {
            is RectangularSlideableSet -> setOf(if (s == Side.START) ss.l else ss.r)
            is RoutableSlideableSet -> getContents(ss).flatMap { getRectangularsOnSide(s, it) }.toSet()
            else -> throw LogicException("Type unknown")
        }
    }

    fun getAllPositioned() : Set<Positioned> {
        return positionedMap.keys.toSet()
    }

    fun getSlideablesFor(de: Positioned): RectangularSlideableSet? {
        return positionedMap[de]
    }

    fun getSlideablesFor(group: Group) : RoutableSlideableSet? {
        return groupMap[group]
    }

    private fun groupAlignment(g1: Group, g2: Group): Boolean {
        var aligns = false
        val lp = object : LinkProcessor {
            override fun process(originatingGroup: Group, destinationGroup: Group, ld: LinkManager.LinkDetail) {
                if (destinationGroup == g2) {
                    if (ld.direction != null) {
                        aligns = true
                    }
                }
            }
        }

        val ss = g1.processLowestLevelLinks(lp)
        return aligns
    }

    private fun groupAlignment(g1: Set<Group>, g2: Set<Group>) : Boolean {
        // need to find just one aligned link between these two
        val out = g1.find { ga -> g2.find { gb -> groupAlignment(ga, gb) } != null } != null
        return out
    }

    fun mergeSlideables(s1: Set<C2BufferSlideable>, s2: Set<C2BufferSlideable>) : Set<C2BufferSlideable> {
        // slideables with the same groups can be merged
        val mightMatch = s2.filterIsInstance<C2IntersectionSlideable>().toMutableSet()

        val unchanged = s2.filter { !mightMatch.contains(it) }

        val out = s1.mapNotNull { l ->
            if (l is C2IntersectionSlideable) {
                val matching = mightMatch.find { groupAlignment(it.intersectingGroups, l.intersectingGroups ) }
                if (matching != null) {
                    mightMatch.remove(matching)
                    mergeSlideablesInner(l, matching)
                } else {
                    l
                }
            } else {
                l
            }
        }.toSet()

        return out.plus(mightMatch).plus(unchanged)
    }

    fun mergeSlideables(s1: C2OrbitSlideable?, s2: C2OrbitSlideable?) : C2OrbitSlideable? {
        return mergeSlideablesInner(s1, s2)
    }

    private fun <X : C2RectangularSlideable> mergeSlideablesInner(s1: X?, s2: X?) : X? {
        if (s1 == null) {
            return s2
        } else if (s2 == null) {
            return s1
        } else {
            @Suppress("UNCHECKED_CAST") val sNew = s1.merge(s2) as X
            // now we need to replace s1 and s2 in their containers
            val containsS1 = slideableMap.remove(s1)
            val containsS2 = slideableMap.remove(s2)
            containsS1!!.addAll(containsS2!!)
            updateSlideableSets(containsS1, s1, s2, sNew)

            slideables.add(sNew)
            slideables.remove(s1)
            slideables.remove(s2)

            slideables.forEach {
                it.replaceConstraint(s1, sNew)
                it.replaceConstraint(s2, sNew)
            }

            compaction.replaceJunction(s1, s2, sNew)

            log.send("Merging: \n\t$s1\n\t$s2\nAdded: $sNew")

            return sNew
        }
    }

    fun mergeSlideables(s1: C2RectangularSlideable, s2: C2RectangularSlideable) : C2RectangularSlideable {
       return mergeSlideablesInner(s1, s2)!!
    }

    private fun updateSlideableSets(
        contains: Set<SlideableSet<*>>?,
        sOld1: C2Slideable,
        sOld2: C2Slideable,
        sNew: C2Slideable
    ) {
        contains?.forEach {
            val ssNew : SlideableSet<*> = it.replaceGeneric(sOld1, sNew).replaceGeneric(sOld2, sNew)
            removeFromSlideableMap(it)
            updateSlideableMap(ssNew)
            updateContainment(it, ssNew)

            if (ssNew is RectangularSlideableSet) {
                val toReplaceDiagramElements = positionedMap.filter { (_, v) -> v == it }.keys
                toReplaceDiagramElements.forEach { d -> positionedMap[d] = ssNew }
            }

            if (ssNew is RoutableSlideableSet) {
                val toReplaceGroups = groupMap.filter { (_, v) -> v == it }.keys
                toReplaceGroups.forEach { g -> groupMap[g] = ssNew }
            }
        }
    }

    private fun updateContainment(old: SlideableSet<*>, new: SlideableSet<*>) {
        if ((old is RoutableSlideableSet) && (new is RoutableSlideableSet)) {
            // replace key
            val contains = containment[old]
            if (contains != null) {
                containment[new] = contains
            }
        }

        if (new is RectangularSlideableSet) {
            containment.values.forEach {
                val i = it.indexOf(old)
                if (i > -1) {
                    it[i] = new
                }
            }
        }
    }

    fun add(de: Rectangular, ss: RectangularSlideableSet) {
        positionedMap[de] = ss
        updateSlideableMap(ss)
        //compaction.interections.plus(ss.getJunctions())
    }

    private fun updateSlideableMap(ss: SlideableSet<*>) {
        ss.getAll().forEach {
            val set = slideableMap.getOrElse(it) { mutableSetOf() }
            set.add(ss)
            slideableMap[it] = set
            slideables.add(it)
        }

        slideableMap.values.forEach { ll -> ll.removeAll { it.done }}
    }

    private fun removeFromSlideableMap(ss: SlideableSet<*>) {
        ss.getAll().forEach {
            slideableMap[it]?.remove(ss)
        }
    }

    fun add(g: Group, ss: RoutableSlideableSet) {
        groupMap[g] = ss
        updateSlideableMap(ss)
        //compaction.interections.plus(ss.getJunctions())
    }

    private fun checkValid(s: C2Slideable, k: Any) {
        if (!slideables.contains(s)) {
            throw LogicException("Wasn't expecting reference to $s for key $k")
        }
    }

    fun contains(outer: RoutableSlideableSet, inner: RectangularSlideableSet) {
        val contents = containment.getOrPut(outer) { mutableListOf() }
        contents.add(inner)
        log.send("Containment: $outer has contents", contents)
    }

    fun contains(outer: RoutableSlideableSet, all: List<RectangularSlideableSet>) {
        val contents = containment.getOrPut(outer) { mutableListOf() }
        contents.clear()
        contents.addAll(all)
        log.send("Containment: $outer has contents", contents)
    }

    fun getContents(outer: RoutableSlideableSet) : List<RectangularSlideableSet> {
        return containment.getOrElse(outer) { emptyList() }.toList()
    }

    fun getContainer(inner: RectangularSlideableSet) : RoutableSlideableSet {
        return containment.filterValues { it.contains(inner) }
            .keys
            .first()
    }

    fun checkConsistency() {
        slideables.removeAll { it is C2Slideable && it.done }

        positionedMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        groupMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        slideables.forEach { k ->
            k.getForwardSlideables(true)
                .forEach { checkValid(it as C2Slideable, "referenced by $k") }
        }

        slideables.forEach { k ->
            k.getForwardSlideables(false)
                .forEach { checkValid(it as C2Slideable, "referenced by $k") }
        }

        slideableMap.keys.forEach {
            if (it.done) {
                slideableMap.remove(it)
            }
        }

        // TBD - need to do this
//        containment.entries.forEach { (e, v) ->
//            e.getAll().forEach { checkValid(it, e) }
//            v
//                .flatMap { l -> l.getAll() }
//                .forEach { checkValid(it, e) }
//        }

        log.send("Consistent")
    }

    fun remove(g: Group) {
        groupMap.remove(g)
    }

    fun getSlideableSets(s: C2Slideable) : Set<SlideableSet<*>>? {
        return slideableMap[s]
    }

    fun addSlideable(c: C2IntersectionSlideable) {
        slideables.add(c)
    }

    companion object {

        private var nn = 0

        fun nextNumber(): Int {
            return nn++
        }
    }
}

