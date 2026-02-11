package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.PlacementPositioned
import org.kite9.diagram.model.Positioned
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSetImpl
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet


/**
 * Used for holding transitive distances
 */
data class Constraint(val forward: Boolean, val dist: Int) {

    fun max(c2: Constraint?): Constraint {
        if (c2 == null) {
            return this
        }
        if (forward != c2.forward) {
            throw LogicException("Constraints must be in same direction")
        }

        return Constraint(forward, kotlin.math.max(c2.dist, dist))
    }

    operator fun plus(c2: Constraint?): Constraint {
        if (c2 == null) {
            return this
        }
        if (forward != c2.forward) {
            throw LogicException("Constraints must be in same direction")
        }

        return Constraint(forward, c2.dist + dist)
    }
}

/**
 * Augments SlackOptimisation to keep track of diagram elements underlying the slideables.
 * @author robmoffat
 */
class C2SlackOptimisation(val compaction: C2CompactionImpl, val dimension: Dimension) : AbstractSlackOptimisation(), Logable {

    /** Track mapping of elements to sets */
    private val rectangularMap: MutableMap<Rectangular, RectangularSlideableSet> = HashMap()
    private val pointMap: MutableMap<PlacementPositioned, RoutableSlideableSet> = HashMap()

    //private val groupMap: MutableMap<LeafGroup, MutableList<RoutableSlideableSet>> = HashMap()
    private val slideableMap: MutableMap<C2Slideable, MutableSet<SlideableSet<*>>> = HashMap()
    private val containment1: MutableMap<RoutableSlideableSet, MutableList<RectangularSlideableSet>> = HashMap()
    private val containment2: MutableMap<RectangularSlideableSet, RoutableSlideableSet> = HashMap()
    private var slideableOrdering: List<C2Slideable> = emptyList()
    private val laneGroups = mutableSetOf<Set<C2Slideable>>()

    override fun initialiseSlackOptimisation() {

    }

    fun getRectangularsOnSide(s: Side, ss: SlideableSet<*>) : Set<C2Slideable> {
        return when (ss) {
            is RectangularSlideableSet -> setOf(if (s == Side.START) ss.l else ss.r)
            is RoutableSlideableSet -> getContents(ss).flatMap { getRectangularsOnSide(s, it) }.toSet()
            else -> throw LogicException("Type unknown")
        }
    }

    fun getAllPositionedRectangulars() : Set<Positioned> {
        return rectangularMap.keys.toSet()
    }

    fun getSlideablesFor(de: Positioned): RectangularSlideableSet? {
        return rectangularMap[de]
    }

    fun getPPSlideablesFor(de: PlacementPositioned) : RoutableSlideableSet? {
        return pointMap[de]
    }


    fun addLaneGroup(lg: Set<C2Slideable>) {
        laneGroups.add(lg)
    }

    fun getLaneGroups() : Set<Set<C2Slideable>> {
        return laneGroups
    }

    fun mergeSlideables(s1: C2Slideable?, s2: C2Slideable?) : C2Slideable? {
        return mergeSlideablesInner(s1, s2)
    }

    private fun mergeSlideablesInner(s1: C2Slideable?, s2: C2Slideable?) : C2Slideable? {
        if (s1 == null) {
            return s2
        } else if (s2 == null) {
            return s1
        } else if (s1 == s2) {
            return s1
        } else {
            val sNew = s1.merge(s2)
            // now we need to replace s1 and s2 in their containers
            val containsS1 = slideableMap.remove(s1) ?: mutableSetOf()
            val containsS2 = slideableMap.remove(s2) ?: mutableSetOf()
            containsS1.addAll(containsS2)
            updateSlideableSets(containsS1, s1, s2, sNew)
            updatePortMap(s1, s2, sNew)

            slideables.add(sNew)
            slideables.remove(s1)
            slideables.remove(s2)

            slideables.forEach {
                it.replaceConstraint(s1, sNew)
                it.replaceConstraint(s2, sNew)
            }

            compaction.replaceIntersections(s1, s2, sNew)

            log.send("Merging: \n\t$s1\n\t$s2\nAdded: $sNew")

            return sNew
        }
    }

    private fun copyNeighbourMap(from: C2Slideable, to: C2Slideable) {
        val sets = compaction.getNeighbourSetsOn(from)
        sets.flatMap { it }.forEach {
            compaction.addNeighbour(it, from, to)
        }

        sets.forEach { s ->
            var last : C2Slideable? = null
            s.forEach {
                if (last != null) {
                    compaction.addNeighbour(to, last, it)
                }

                last = it
            }
        }
    }

    fun addSide(container: RectangularSlideableSet, inner: RoutableSlideableSet, side: Side, useOrbit: Boolean, separation: Int) : RoutableSlideableSet?  {
        val containerRoutable = if (useOrbit) container.wrapInRoutable() else null
        if (containerRoutable != null) {
            contains(containerRoutable, container)
            updateSlideableMap(containerRoutable)
        }

        val new = when (side) {
            Side.START -> {
                val newLeft = if (containerRoutable != null) {
                    copyNeighbourMap(inner.bl!!, containerRoutable.bl!!)
                    containerRoutable.bl!!
                } else {
                    container.l
                }
                if (inner.bl != null) {
                    ensureMinimumDistance(newLeft, inner.bl!!, separation)
                }
                inner.replaceSide(newLeft, side)

            }
            Side.END -> {
                val newRight = if (containerRoutable != null) {
                    copyNeighbourMap(inner.br!!, containerRoutable.br!!)
                    containerRoutable.br!!
                } else {
                    container.r
                }
                if (inner.br != null) {
                    ensureMinimumDistance(inner.br!!, newRight, separation)
                }
                inner.replaceSide(newRight, side)
            }
            else -> {
                throw LogicException("Illegal Side")
            }
        }

        this.updateContainment1(inner, new)
        updateSlideableMap(new)
        checkConsistency()
        return new
    }

    private fun updateSlideableSets(
        contains: Set<SlideableSet<*>>?,
        sOld1: C2Slideable,
        sOld2: C2Slideable,
        sNew: C2Slideable
    ) {
        contains?.forEach {
            val ssNew : SlideableSet<*> = it.replace(sOld1, sNew).replace(sOld2, sNew)
            removeFromSlideableMap(it)
            updateSlideableMap(ssNew)
            updateContainment1(it, ssNew)
            updateContainment2(it, ssNew)

            if (ssNew is RectangularSlideableSet) {
                val toReplaceDiagramElements = rectangularMap.filter { (_, v) -> v == it }.keys
                toReplaceDiagramElements.forEach { d -> rectangularMap[d] = ssNew }
            }
        }
    }

    private fun updatePortMap(sOld1: C2Slideable, sOld2: C2Slideable, newS: C2Slideable) {
        val toReplacePorts = pointMap.filter { (_, v) -> v.c == sOld1 || v.c == sOld2 }.keys
        toReplacePorts.forEach { p ->
            pointMap[p] = RoutableSlideableSetImpl(newS, null, null)
        }
    }

    private fun updateContainment1(old: SlideableSet<*>, new: SlideableSet<*>) {
        if ((old is RoutableSlideableSet) && (new is RoutableSlideableSet)) {
            // replace key
            val contains = containment1[old]
            if (contains != null) {
                containment1[new] = contains
                containment1.remove(old)
            }
        }

        if (new is RectangularSlideableSet) {
            containment1.values.forEach {
                val i = it.indexOf(old)
                if (i > -1) {
                    it[i] = new
                }
            }
        }
    }

    private fun updateContainment2(old: SlideableSet<*>, new: SlideableSet<*>) {
        if ((old is RectangularSlideableSet) && (new is RectangularSlideableSet)) {
            // replace key
            containment2[new] == containment2[old]
            containment2.remove(old)
        } else if ((old is RoutableSlideableSet) && (new is RoutableSlideableSet)) {
            for ((key, value) in containment2) {
                if (value == old) {
                    containment2[key] = new
                }
            }
        } else {
            throw LogicException("Type mismatch")
        }

    }

    fun add(ss: RoutableSlideableSet?) {
        if (ss != null) {
            updateSlideableMap(ss)
        }
    }

    fun add(de: Rectangular, ss: RectangularSlideableSet) {
        rectangularMap[de] = ss
        updateSlideableMap(ss)
    }

    fun add(p: PlacementPositioned, ss: RoutableSlideableSet) {
        pointMap[p] = ss
        updateSlideableMap(ss)
    }

    private fun updateSlideableMap(ss: SlideableSet<*>) {
        ss.getAll().forEach {
            val set = slideableMap.getOrElse(it) { mutableSetOf() }
            set.add(ss)
            slideableMap[it] = set
            slideables.add(it)
        }

        slideableMap.values.forEach { ll -> ll.removeAll {
            it.done
        }}
    }

    private fun removeFromSlideableMap(ss: SlideableSet<*>) {
        ss.getAll().forEach {
            slideableMap[it]?.remove(ss)
        }
    }

    private fun checkValid(s: C2Slideable, k: Any) {
        if (!slideables.contains(s)) {
            throw LogicException("Wasn't expecting reference to $s for key $k")
        }
    }

    fun contains(outer: RectangularSlideableSet, inner: RoutableSlideableSet) {
        if (containment2.containsKey(outer)) {
            throw LogicException("Already set containment")
        }

        containment2[outer] = inner
    }

    fun contains(outer: RoutableSlideableSet, inner: RectangularSlideableSet) {
        val contents = containment1.getOrPut(outer) { mutableListOf() }
        contents.add(inner)
        log.send("Containment: $outer has contents", contents)
    }

    fun contains(outer: RoutableSlideableSet, all: List<RectangularSlideableSet>) {
        val contents = containment1.getOrPut(outer) { mutableListOf() }
        contents.clear()
        contents.addAll(all)
        log.send("Containment: $outer has contents", contents)
    }

    fun getContents(outer: RoutableSlideableSet) : List<RectangularSlideableSet> {
        return containment1.getOrElse(outer) { emptyList() }.toList()
    }

    fun getContents(outer: RectangularSlideableSet) : RoutableSlideableSet? {
        return containment2.get(outer)
    }

    fun getContainers(inner: RectangularSlideableSet) : Set<RoutableSlideableSet> {
        val filtered = containment1.filterValues { it.contains(inner) }
        return filtered.keys
    }

    fun checkConsistency() {
        slideables.removeAll { it is C2Slideable && it.isDone() }

        rectangularMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        slideables.forEach { k ->
            k.getForwardSlideables(true)
                .forEach { checkValid(it, "referenced by $k") }
        }

        slideables.forEach { k ->
            k.getForwardSlideables(false)
                .forEach { checkValid(it, "referenced by $k") }
        }

        slideableMap.keys.forEach {
            if (it.isDone()) {
                slideableMap.remove(it)
            }
        }

        log.send("Consistent")
    }

    fun addSlideable(c: C2Slideable) {
        slideables.add(c)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllSlideables(): Collection<C2Slideable> {
        return super.getAllSlideables() as Collection<C2Slideable>
    }


    fun updateSlideableOrdering() {
        val out = this.getAllSlideables().sortedWith { a, b ->
            if (a.minimumPosition != b.minimumPosition) {
                b.minimumPosition - a.minimumPosition
            } else {
                val c = a.getMinimumForwardConstraintTo(b)
                c ?: 0
            }
        }

        this.slideableOrdering = out
    }

    companion object {

        private var nn = 0

        fun nextNumber(): Int {
            return nn++
        }
    }
}

