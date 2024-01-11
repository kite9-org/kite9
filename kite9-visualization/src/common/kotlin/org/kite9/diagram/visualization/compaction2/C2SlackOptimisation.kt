package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Positioned
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

interface SlideableSet<X : SlideableSet<X>> {

    fun getAll() : Set<C2Slideable>

    fun replaceRectangular(s: C2RectangularSlideable, with: C2RectangularSlideable) : X

    fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable) : X

    fun replaceGeneric(s: C2Slideable, with: C2Slideable) : X {
        return if ((s is C2IntersectionSlideable) && (with is C2IntersectionSlideable)) {
            replaceIntersection(s, with)
        } else if ((s is C2RectangularSlideable) && (with is C2RectangularSlideable)) {
            replaceRectangular(s, with)
        } else {
            throw LogicException("Can only replace slideable with same type $s $with")
        }
    }

    fun getBufferSlideables() : Set<C2BufferSlideable>

    fun mergeWithOverlap(over: SlideableSet<*>, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun mergeWithGutter(after: SlideableSet<*>, c2: C2SlackOptimisation) :  RoutableSlideableSet

    val done : Boolean

    val number : Int

}
interface RectangularSlideableSet : SlideableSet<RectangularSlideableSet> {

    val d: Rectangular
    val l: C2RectangularSlideable
    val r: C2RectangularSlideable
    val c: C2IntersectionSlideable?

    fun getRectangularSlideables(): Collection<C2RectangularSlideable>

    override fun getBufferSlideables() : Set<C2BufferSlideable> = setOfNotNull(c)

    fun wrapInRoutable(so: C2SlackOptimisation) : RoutableSlideableSet

}

interface RoutableSlideableSet : SlideableSet<RoutableSlideableSet> {

    val c: C2IntersectionSlideable?
    val bl: C2OrbitSlideable
    val br: C2OrbitSlideable

    fun replaceOrbit(s: C2OrbitSlideable, with: C2OrbitSlideable) : RoutableSlideableSet

    override fun replaceGeneric(s: C2Slideable, with: C2Slideable) : RoutableSlideableSet {
        println("Replacing $s with $with" )
        return if ((s is C2OrbitSlideable) && (with is C2OrbitSlideable)) {
            replaceOrbit(s, with)
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

    override var done = false

    override fun replaceRectangular(s: C2RectangularSlideable, with: C2RectangularSlideable): RectangularSlideableSet {
        done = true
        return RectangularSlideableSetImpl(
            d,
            if (l == s) with else l,
            if (r == s) with else r,
            c
        )
    }

    override fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable): RectangularSlideableSet {
        done = true
        return RectangularSlideableSetImpl(
            d,
            if (l == s) with else l,
            if (r == s) with else r,
            if (c == s) with else c
        )
    }

    override fun mergeWithOverlap(over: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (over is RoutableSlideableSet) {
            val allBs = setOf(getBufferSlideables(), over.getBufferSlideables()).flatten().toSet()
            val newC = c2.mergeSlideables(over.c, c)
            done = true
            val out = RoutableSlideableSetImpl(allBs, newC, over.bl, over.br)
            c2.contains(out, c2.getContents(over).plus(this))
            out
        } else {
            throw LogicException("unsupported")
        }
    }

    override fun mergeWithGutter(after: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return if (after is RoutableSlideableSet) {
            val allBs = setOf(getBufferSlideables(), after.getBufferSlideables()).flatten().toSet()
            val newC = c2.mergeSlideables(this.c, c)
            done = true
            val  out = RoutableSlideableSetImpl(allBs, newC, after.bl, after.br)
            c2.contains(out, c2.getContents(after).plus(this))
            out
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

    override fun wrapInRoutable(so: C2SlackOptimisation): RoutableSlideableSet {
        val bl = C2OrbitSlideable(so, l.dimension, setOf(RectAnchor(d, Side.START)))
        val br = C2OrbitSlideable(so, l.dimension, setOf(RectAnchor(d, Side.END)))

        so.ensureMinimumDistance(bl, l, 0)
        so.ensureMinimumDistance(r, br, 0)

        return RoutableSlideableSetImpl(
            setOfNotNull(bl, c, br),
            c,
            bl,
            br)
    }

    override val number = C2SlackOptimisation.nextNumber()
}


data class RoutableSlideableSetImpl(val bs: Set<C2BufferSlideable>,
                                    override val c: C2IntersectionSlideable?,
                                    override val bl: C2OrbitSlideable,
                                    override val br: C2OrbitSlideable,
                                    ) : RoutableSlideableSet {

    override var done = false

    override fun mergeWithGutter(after: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return when (after) {

            is RoutableSlideableSet -> {
                //val allRs = setOf(rs, after.getRectangularSlideableSets()).flatten().toSet()
                val newOrbit = c2.mergeSlideables(br, after.bl)
                val allBs = setOf(bs, after.getBufferSlideables()).asSequence().flatten().minus(br).minus(after.bl).plus(newOrbit).toSet()
                done = true
                val new = RoutableSlideableSetImpl(allBs, null, bl, after.br)
                c2.contains(new, c2.getContents(this).plus(c2.getContents(after)))
                new
            }

            is RectangularSlideableSet -> {
                done = true
                val out = RoutableSlideableSetImpl(bs, c, bl, br)
                c2.contains(out, c2.getContents(this).plus(after))
                out
            }
            else -> throw LogicException("unsupported")
        }
    }

    override fun mergeWithOverlap(over: SlideableSet<*>, c2: C2SlackOptimisation): RoutableSlideableSet {
        return when (over) {

            is RoutableSlideableSet -> {
                //val allRs = setOf(rs, over.getRectangularSlideableSets()).flatten().toSet()
                val newL = c2.mergeSlideables(over.bl, bl)
                val newR = c2.mergeSlideables(over.br, br)
                val newC = c2.mergeSlideables(over.c, c)
                done = true
                val out = RoutableSlideableSetImpl(setOfNotNull(newL, newR, newC), newC, newL, newR)
                c2.contains(out, c2.getContents(this).plus(c2.getContents(over)))
                out
            }

            is RectangularSlideableSet -> {
                val newC = c2.mergeSlideables(over.c, c)
                done = true
                val out = RoutableSlideableSetImpl(bs, newC, bl, br)
                c2.contains(out, c2.getContents(this).plus(over))
                out
            }

            else -> throw LogicException("unsupported")
        }
    }

    override fun getBufferSlideables(): Set<C2BufferSlideable> {
        return bs
    }

    override fun getAll(): Set<C2Slideable> {
        return bs
    }

    override fun replaceRectangular(s: C2RectangularSlideable, with: C2RectangularSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            bs,
            c,
            bl,
            br)
    }

    override fun replaceOrbit(s: C2OrbitSlideable, with: C2OrbitSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            RectangularSlideableSetImpl.replaceIfPresent(bs, s, with),
            c,
            if (bl == s) with else bl,
            if (br == s) with else br)
    }

    override fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            RectangularSlideableSetImpl.replaceIfPresent(bs, s, with),
            if (c == s) with else c,
            bl,
            br)
    }

//    override fun getRectangularsOnSide(s: Side) : Set<C2RectangularSlideable> = getRectangularSlideableSets()
//        .flatMap { it.getRectangularsOnSide(s) }
//        .toSet()

    override val number = C2SlackOptimisation.nextNumber()

}



/**
 * Augments SlackOptimisation to keep track of diagram elements underlying the slideables.
 * @author robmoffat
 */
class C2SlackOptimisation : AbstractSlackOptimisation(), Logable {

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

    fun mergeSlideables(s1: C2IntersectionSlideable?, s2: C2RectangularSlideable?) : C2IntersectionSlideable? {
        return mergeSlideablesInner(s1, s2) as C2IntersectionSlideable?
    }

    fun mergeSlideables(s1: C2OrbitSlideable, s2: C2RectangularSlideable) : C2OrbitSlideable {
        return mergeSlideablesInner(s1, s2) as C2OrbitSlideable
    }

    private fun <X : C2RectangularSlideable> mergeSlideablesInner(s1: X?, s2: X?) : X? {
        if (s1 == null) {
            return s2
        } else if (s2 == null) {
            return s1
        } else {
            val sNew = s1.merge(s2) as X
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
    }

    private fun checkValid(s: C2Slideable, k: Any) {
        if (!slideables.contains(s)) {
            throw LogicException("Wasn't expecting reference to $s for key $k")
        }
    }

    fun contains(outer: RoutableSlideableSet, inner: RectangularSlideableSet) {
        val contents = containment.getOrPut(outer) { mutableListOf() }
        contents.add(inner)
    }

    fun contains(outer: RoutableSlideableSet, all: List<RectangularSlideableSet>) {
        val contents = containment.getOrPut(outer) { mutableListOf() }
        contents.clear()
        contents.addAll(all)
    }

    fun getContents(outer: RoutableSlideableSet) : List<RectangularSlideableSet> {
        return containment.getOrElse(outer) { emptyList() }.toList()
    }

    fun checkConsistency() {
        slideables.removeAll { it is C2Slideable && it.done }

        positionedMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        groupMap.forEach { (k, v) -> v.getAll().forEach { checkValid(it, k) } }

        slideables.flatMap { it.getForwardSlideables(true) }
            .forEach { checkValid(it as C2Slideable, "*") }

        slideables.flatMap { it.getForwardSlideables(false) }
            .forEach { checkValid(it as C2Slideable, "*") }

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
        val v = groupMap.remove(g)
        containment.remove(v)
    }

    companion object {

        var nn = 0;

        fun nextNumber(): Int {
            return nn++
        }
    }
}

