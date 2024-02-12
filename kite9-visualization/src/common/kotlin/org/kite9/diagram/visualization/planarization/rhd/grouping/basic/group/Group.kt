package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.common.algorithms.det.Deterministic
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.GroupAxis
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager

sealed interface Group : Deterministic {

    /**
     * Unique number for this group
     */
    val groupNumber : Int

    /**
     * Either groupNumber (for a leaf) or the lowest group number of any contained
     * leaf.   This allows us to track "earlier" vs "later" elements defined in a
     * diagram
     */
    val groupOrdinal : Int

    /**
     * Number of leaf groups contained in this group
     */
    val size : Int

    fun processAllLeavingLinks(compound: Boolean, mask: Int, lp: LinkManager.LinkProcessor)

    fun getLink(g: Group): LinkManager.LinkDetail?

    val axis : GroupAxis

    val linkManager: LinkManager

    fun processLowestLevelLinks(lp: LinkManager.LinkProcessor)

    fun addLeafGroupOrdinalsToSet(s: MutableSet<Int>)

    /**
     * Sorted list of leaf element numbers, used for comparing compound groups
     * easily (since you can use hashcode / equals)
     */
    val leafList : String

    /**
     * Returns true if this group is lg, or it contains it somehow in the hierarchy
     */
    fun contains(lg: Group): Boolean

    /**
     * Returns the number of nested levels below this group
     */
    val height : Int

    /**
     * Dump group info to log
     */
    fun log(log: Kite9Log)

    /**
     * Layout that we've set for the group
     */
    var layout: Layout?

    /**
     * Whether the group is ready to be merged
     */
    var live : Boolean

    val hints: Map<String, Float?>

    /**
     * Used for link-counting.  We only count links to active groups
     */
    fun isActive() : Boolean
}