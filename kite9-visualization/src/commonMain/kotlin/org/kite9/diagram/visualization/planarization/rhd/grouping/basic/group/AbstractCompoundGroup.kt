package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager
import kotlin.math.max
import kotlin.math.min

/**
 * Represents the relative positions of two other groups within the diagram, allowing the immediate contents
 * of any container to be expressed as a binary tree.
 */
abstract class AbstractCompoundGroup(
    override val a: Group,
    override val b: Group,
    private val treatAsLeaf: Boolean,
    groupNumber: Int,
    override val size: Int,
    hc: Int) :
    AbstractGroup(groupNumber, hc), CompoundGroup {

    override val groupOrdinal = min(a.groupOrdinal, b.groupOrdinal)
    override val height: Int = max(a.height, b.height) + 1
    override val hints: Map<String, Float?> = emptyMap()
    override var layout : Layout? = null

    override fun addLeafGroupOrdinalsToSet(s: MutableSet<Int>) {
        if (treatAsLeaf) {
            s.add(groupNumber)
        } else {
            a.addLeafGroupOrdinalsToSet(s)
            b.addLeafGroupOrdinalsToSet(s)
        }
    }

    /**
     * This will process [OrderingTemporaryConnection]s first, so that if there is
     * a contradiction in the links, it will occur on one of the Link - Connections.
     */
    fun fileLinks(linksGroup: Group, toGroup: Group) : LinkManager.LinkDetail? {
        var ldOut : MutableList<LinkManager.LinkDetail> = mutableListOf()
        linksGroup.processAllLeavingLinks(true, linkManager.allMask(), object : LinkManager.LinkProcessor {
            override fun process(notUsed: Group, g: Group, ld: LinkManager.LinkDetail) {
                val ld2 = fileLink(linksGroup, g, toGroup, ld)
                if (ld2 != null) {
                    ldOut.add(ld2!!)
                }
            }
        })

        if (ldOut.size == 0) {
            return null
        } else if (ldOut.size == 1) {
            return ldOut[0]
        } else {
            throw LogicException("Too many internal links?")
        }
    }

    private fun fileLink(from: Group, to: Group, merging: Group, ld: LinkManager.LinkDetail) : LinkManager.LinkDetail? {
        val internal = merging.contains(to)
        if (!internal) {
            linkManager.sortLink(ld!!)
        } else {
            if (merging === to) {
                return ld
            }
        }

        return null
    }

    override fun toString(): String {
        return "[$groupNumber$a,$b:$axis]"
    }

    override fun contains(lg: Group): Boolean {
        if (this === lg) {
            return true
        }
        /**
         * Obviously, we can't contain bigger groups than ourselves.
         */
        if (lg.size >= size) {
            return false
        }
        /**
         * Also, can't create later-created groups than this.
         */
        return if (lg.groupNumber > groupNumber) {
            false
        } else a.contains(lg) || b.contains(lg)
    }

    override fun processLowestLevelLinks(lp: LinkManager.LinkProcessor) {
        a.processLowestLevelLinks(lp)
        b.processLowestLevelLinks(lp)
    }

}