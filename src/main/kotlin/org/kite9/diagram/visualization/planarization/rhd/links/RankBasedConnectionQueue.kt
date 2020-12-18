package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.Tools
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D
import java.util.function.Predicate

class RankBasedConnectionQueue(rh: RoutableHandler2D) : ConnectionManager, Logable {

    class LinkComparator : Comparator<BiDirectional<Connected>> {
        override fun compare(arg0: BiDirectional<Connected>, arg1: BiDirectional<Connected>): Int {
            val r0 = getRankFor(arg0)
            val r1 = getRankFor(arg1)
            return r1.compareTo(r0)
        }

        fun getRankFor(arg0: BiDirectional<Connected>): Int {
            return if (arg0 is Connection) {
                arg0.getRank()
            } else {
                0
            }
        }
    }

    private val alreadyAdded: MutableSet<BiDirectional<Connected>> = UnorderedSet(1000)
    var log = Kite9Log(this)
    var hasContradictions = false
    val comp : Comparator<BiDirectional<Connected>> = LinkComparator()

    val x: ArrayList<BiDirectional<Connected>> = ArrayList(1000)
    val y: ArrayList<BiDirectional<Connected>> = ArrayList(1000)
    val u: ArrayList<BiDirectional<Connected>> = ArrayList(1000)

    fun considerThis(c: BiDirectional<Connected>, cg: CompoundGroup): Boolean {
        if (alreadyAdded.contains(c)) {
            return false
        }

        return if (considerThisB(c, cg)) {
            alreadyAdded.add(c)
            true
        } else {
            false
        }
    }

    override fun iterator(): MutableIterator<BiDirectional<Connected>> {

        x.sortWith(comp)
        y.sortWith(comp)
        u.sortWith(comp)

        return object : MutableIterator<BiDirectional<Connected>> {

            var xi = x.iterator()
            var yi = y.iterator()
            var ui = u.iterator()
            var current = 0

            override fun hasNext(): Boolean {
                return xi.hasNext() || yi.hasNext() || ui.hasNext()
            }

            override fun next(): BiDirectional<Connected> {
                val out: BiDirectional<Connected>
                if (xi.hasNext()) {
                    out = xi.next()
                    current = 0
                } else if (yi.hasNext()) {
                    out = yi.next()
                    current = 1
                } else if (ui.hasNext()) {
                    out = ui.next()
                    current = 2
                } else {
                    throw NoSuchElementException()
                }
                return out
            }

            override fun remove() {
                if (current == 0) {
                    xi.remove()
                } else if (current == 1) {
                    yi.remove()
                } else if (current == 2) {
                    ui.remove()
                }
            }
        }
    }




    override fun handleLinks(g: GroupPhase.Group) {
        if (g is CompoundGroup) {
            val cg = g
            if (cg.internalLinkA != null) {
                for (c in cg.internalLinkA.connections) {
                    if (considerThis(c, cg)) {
                        //checkForPositionContradiction(c);
                        add(c)
                    }
                }
            }
        } else {
            // self-links aren't allowed yet
            val internalLinks = g.getLink(g)
            if (internalLinks != null) {
                for (l in internalLinks.connections) {
                    if (l is Connection) {
                        l.getRenderingInformation().isRendered = false
                    } else {
                        throw LogicException("Not sure what this is: $l")
                    }
                }
            }
        }
    }

    fun considerThisB(c: BiDirectional<Connected>, cg: CompoundGroup): Boolean {
        return if (c is Connection) {
            val d = c.getDrawDirection()
            (d == null && !u!!.contains(c)
                    || (d === Direction.LEFT || d === Direction.RIGHT) && cg.axis.isHorizontal
                    || (d === Direction.UP || d === Direction.DOWN) && cg.axis.isVertical)
        } else {
            false
        }
    }

    override fun add(c2: BiDirectional<Connected>): Boolean {
        //System.out.println("Admitting: "+c2);
        val d = c2.getDrawDirection()
        val contradiction = c2 is Connection && Tools.isConnectionContradicting(
            c2
        )
        if (contradiction) {
            hasContradictions = true
        }
        return if (d == null || contradiction) {
            u!!.add(c2)
            true
        } else {
            when (d) {
                Direction.UP, Direction.DOWN -> {
                    y!!.add(c2)
                    true
                }
                Direction.LEFT, Direction.RIGHT -> {
                    x!!.add(c2)
                    true
                }
            }
        }
        return false
    }

    override fun addAll(elements: Collection<BiDirectional<Connected>>): Boolean {
        elements.forEach { add (it) }
        return true
    }

    override fun clear() {
        u.clear()
        x.clear()
        y.clear()
    }

    override fun isEmpty(): Boolean {
        return x.isEmpty() && y.isEmpty() && u.isEmpty()
    }

    override fun remove(o: BiDirectional<Connected>): Boolean {
        return x.remove(o) || y.remove(o) || u.remove(o)
    }

    override fun contains(o: BiDirectional<Connected>): Boolean {
        return alreadyAdded.contains(o) || u.contains(o) || x.contains(o) || y.contains(o)
    }

    override fun containsAll(c: Collection<BiDirectional<Connected>>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun retainAll(elements: Collection<BiDirectional<Connected>>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeAll(elements: Collection<BiDirectional<Connected>>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeIf(filter: Predicate<in BiDirectional<Connected>>): Boolean {
        throw UnsupportedOperationException()
    }

    override val size: Int
        get() =  x.size + y.size + u.size

    override val prefix: String
        get() = "CQ  "

    override val isLoggingEnabled: Boolean
        get() = true

    override fun hasContradictions(): Boolean {
        return hasContradictions
    }

    override fun toString(): String {
        return "$x/$y/$u"
    }
}