package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.visualization.planarization.rhd.GroupAxis
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager

abstract sealed class AbstractGroup protected constructor(
    override val groupNumber: Int,
    val hc: Int) :
    Group {

    /**
     * Returns the leaf group number (or numbers for compound group)
     * composing this group.
     */
    override val leafList by lazy {
            val gs = mutableSetOf<Int>()
            addLeafGroupOrdinalsToSet(gs)
            val sortedList = gs.sorted();
            sortedList.toString()
        }

    override var live : Boolean = false

    override fun isActive() : Boolean = (this.axis==null) || (this.axis.active);

    /**
     * TODO: use the group number for hashcode in a more normal way.
     */
    override fun getID(): String {
        return "g$groupNumber"
    }

    override fun processAllLeavingLinks(compound: Boolean, mask: Int, lp: LinkManager.LinkProcessor) {
        linkManager.processAllLeavingLinks(compound, mask, lp)
    }

    override fun getLink(g: Group): LinkManager.LinkDetail? {
        return linkManager[g]
    }

    override fun log(log: Kite9Log) {
        log.send("Group: $this")
        log.send("  Links:", linkManager.forLogging())
    }


    override fun hashCode(): Int {
        return hc
    }



}