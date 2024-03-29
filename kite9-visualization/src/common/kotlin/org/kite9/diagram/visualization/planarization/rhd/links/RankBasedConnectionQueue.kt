package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.Tools
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.AbstractCompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class RankBasedConnectionQueue(rh: RoutableHandler2D) : ConnectionManager, Logable {

    fun getRankFor(arg0: BiDirectional<Connected>): Int {
        return if (arg0 is Connection) {
            arg0.getRank()
        } else {
            0
        }
    }

    private val alreadyAdded: MutableSet<BiDirectional<Connected>> = UnorderedSet(1000)
    var log = Kite9Log.instance(this)
    private var hasContradictions = false

    val comp  = { arg0: BiDirectional<Connected>, arg1: BiDirectional<Connected> ->
        val r0 = getRankFor(arg0)
        val r1 = getRankFor(arg1)
        r1.compareTo(r0)
    }

    val x: ArrayList<BiDirectional<Connected>> = ArrayList(1000)
    val y: ArrayList<BiDirectional<Connected>> = ArrayList(1000)
    val u: ArrayList<BiDirectional<Connected>> = ArrayList(1000)

    fun considerThis(c: BiDirectional<Connected>, cg: AbstractCompoundGroup): Boolean {
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
       // u.sortWith(comp)

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
                    throw LogicException("No such element")
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




    override fun handleLinks(g: Group) {
        if (g is AbstractCompoundGroup) {
            val cg = g
            val internalLinkA = cg.internalLinkA
            if (internalLinkA != null) {
                for (c in internalLinkA.connections) {
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
                        l.getRenderingInformation().rendered = false
                    } else {
                        throw LogicException("Not sure what this is: $l")
                    }
                }
            }
        }
    }

    fun considerThisB(c: BiDirectional<Connected>, cg: AbstractCompoundGroup): Boolean {
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