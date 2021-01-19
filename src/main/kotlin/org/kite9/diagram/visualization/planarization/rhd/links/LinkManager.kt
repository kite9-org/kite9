package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Handles the links from one group to others.
 */
interface LinkManager {

    interface LinkProcessor {
        fun process(originatingGroup: Group, destinationGroup: Group, ld: LinkDetail)
    }

    /**
     * Uses a link processor to manage the iteration.
     */
    fun processAllLeavingLinks(compound: Boolean, mask: Int, lp: LinkProcessor)

    interface LinkDetail {
        /**
         * Rank allows us to prioritise importance of adhering to a link.
         * Higher = later specified link = more important.
         */
        val linkRank: Int

        /**
         * True if this link is involved in ordering a container
         */
        val isOrderingLink: Boolean
        val direction: Direction?
        val numberOfLinks: Float
        val connections: Iterable<BiDirectional<Connected>>
        val group: Group
        fun processToLevel(lp: LinkProcessor, l: Int)
        fun processLowestLevel(lp: LinkProcessor)

        /**
         * Returns true if the linkdetail leaves a given group.
         */
        fun from(b: Group): Boolean
    }

    /**
     * Called when two original groups (linked to this LM) are merged into a compound group.
     */
    fun notifyMerge(g: CompoundGroup, aRemains: Boolean, bRemains: Boolean)

    /**
     * Called when a group (linked to this LM) changes container, because a container gets completed.
     */
    fun notifyContainerChange(g: Group)

    /**
     * Called when this group changes container.
     */
    fun notifyContainerChange()

    /**
     * Called when the group with it's links being managed has an axis change
     */
    fun notifyAxisChange()
    fun subset(mask: Int): Collection<LinkDetail>
    fun subsetGroup(mask: Int): Collection<Group>
    fun allMask(): Int

    operator fun get(g: Group): LinkDetail?

    /**
     * Adds links to the link manager.
     */
    fun sortLink(
        d: Direction?, otherGroup: Group, linkValue: Float, ordering: Boolean, linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    )

    /**
     * Adds an existing link detail to the link manager (promoted from a sub-group)
     */
    fun sortLink(ld: LinkDetail)
    var linkCount: Int
    fun forLogging(): Map<Group, LinkDetail>
}