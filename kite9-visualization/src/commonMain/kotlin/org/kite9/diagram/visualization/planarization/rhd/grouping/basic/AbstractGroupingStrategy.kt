package org.kite9.diagram.visualization.planarization.rhd.grouping.basic

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState.GroupContainerState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor

/**
 * Merges groups together into a hierarchical structure for routing layout.
 *
 * MergeOptions hold the options for merge in a priority queue.
 *
 * @author robmoffat
 */
abstract class AbstractGroupingStrategy(
        top: DiagramElement,
        elements: Int,
        ch: ContradictionHandler,
        gp: GridPositioner,
        em: ElementMapper,
        ef: DiagramElementFactory<*>
) : GroupPhase(top, elements, ch, gp, em, ef), GroupingStrategy {

    /**
     * Actually does the merge suggested by the merge option, replacing the individual groups with a
     * compound group.
     */
    protected fun performMerge(ms: BasicMergeState, mo: MergeOption) {
        if (mo.priority >= ILLEGAL_PRIORITY) {
            log.error(if (log.go()) null else "Merging: $mo")
        } else {
            log.send(if (log.go()) null else "Merging: $mo")
        }
        val combined = createCompoundGroup(ms, mo)
        doCompoundGroupInsertion(ms, combined, false)
    }

    protected open fun doCompoundGroupInsertion(
            ms: BasicMergeState,
            combined: CompoundGroup,
            skipContainerCompletionCheck: Boolean
    ) {
        updateContainers(combined, ms)
        removeOldGroups(ms, combined)
        handleReferences(combined)
        introduceCombinedGroup(ms, combined)
        if (!skipContainerCompletionCheck) {
            checkGroupsContainersAreComplete(combined, ms)
        }
    }

    /**
     * See if the merging of this group completes any containers, and change the container states
     * accordingly.
     */
    private fun updateContainers(group: CompoundGroup, ms: BasicMergeState) {
        val containersA = ms.getContainersFor(group.a)!!
        val containersB = ms.getContainersFor(group.b)!!
        val combined: MutableSet<Container> = LinkedHashSet(containersA.keys)
        combined.addAll(containersB.keys)
        val iterator: Iterator<Container> = combined.iterator()
        while (iterator.hasNext()) {
            val container = iterator.next()
            val aContained = containersA[container]
            val bContained = containersB[container]
            val contained =
                    aContained != null && aContained.hasContent() ||
                            bContained != null && bContained.hasContent()
            val isComplete =
                    aContained != null && aContained.isComplete ||
                            bContained != null && bContained.isComplete
            val out = GroupContainerState.get(contained, isComplete)
            ms.addGroupContainerMapping(group, container, out)
        }
    }

    /** Performs the functions needed to add the new compound group to the merge state */
    protected abstract fun introduceCombinedGroup(ms: BasicMergeState, combined: CompoundGroup)

    protected abstract fun createCompoundGroup(ms: BasicMergeState, mo: MergeOption): CompoundGroup

    /** This works out how to handle references going to the components of a new compound group. */
    protected fun handleReferences(group: CompoundGroup) {
        val a = group.a
        val b = group.b
        group.processAllLeavingLinks(
                true,
                group.linkManager.allMask(),
                object : LinkProcessor {
                    override fun process(
                            originatingGroup: Group,
                            destinationGroup: Group,
                            ld: LinkDetail
                    ) {
                        if (destinationGroup.isActive()) {
                            destinationGroup.linkManager.notifyMerge(
                                    group,
                                    a.isActive(),
                                    b.isActive()
                            )
                        }
                    }
                }
        )
    }

    /** Checks the groups that were part of the merge and removes them. */
    protected abstract fun removeOldGroups(ms: BasicMergeState, combined: CompoundGroup)

    /**
     * If the provided group doesn't need any more merging, remove it from the merge state, and
     * potentially, promote it's container.
     */
    protected fun checkGroupsContainersAreComplete(group: Group, ms: BasicMergeState) {
        val containerMap = ms.getContainersFor(group)!!
        val containers: Set<Container> = containerMap.keys
        val containers2 = ArrayList(containers)
        for (container in containers2) {
            val state = containerMap[container]
            if (state != null && !state.isComplete) {
                if (isContainerComplete(container, ms)) {
                    completeContainer(ms, container)
                }
            }
        }
    }

    protected fun isContainerComplete(c: Container, ms: BasicMergeState): Boolean {
        val csi = ms.getStateFor(c)!!
        if (csi.done) return true
        if (csi.incompleteSubcontainers.size > 0) {
            return false
        }
        csi.done = isContainerCompleteInner(c, ms)
        return csi.done
    }

    protected abstract fun isContainerCompleteInner(c: Container, ms: BasicMergeState): Boolean

    protected fun isContainerMergeable(c: Container, ms: BasicMergeState): Boolean {
        val csi = ms.getStateFor(c)!!
        return csi.incompleteSubcontainers.size == 0
    }

    protected fun completeContainer(ms: BasicMergeState, c: Container) {
        // ok, no need to merge this one - it needs removing from the list
        log.send(if (log.go()) null else "Completed container: $c")
        val csiChild = ms.getStateFor(c)!!
        csiChild.done = true
        ms.removeLiveContainer(c)
        val cc = c.getContainer()
        if (cc != null) {
            // push groups from this container into parent
            val csiParent = ms.getStateFor(cc)!!
            csiParent.incompleteSubcontainers.remove(c)
            val toIterate: List<Group> = ArrayList(csiChild.contents)
            var promotionOK = true
            for (g in toIterate) {
                promotionOK = checkGroupChangeContainer(ms, c, cc, g) && promotionOK
            }
            if (promotionOK) {
                if (isContainerMergeable(cc, ms)) {
                    startContainerMerge(ms, cc)
                }
                if (isContainerComplete(cc, ms)) {
                    completeContainer(ms, cc)
                }
            }
        }
    }

    protected open fun startContainerMerge(ms: BasicMergeState, c: Container) {
        ms.addLiveContainer(c)
        val csi = ms.getStateFor(c)!!
        for (g in csi.contents) {
            ms.addLiveGroup(g)
            groupChangedContainer(ms, g)
        }
    }

    private fun checkGroupChangeContainer(
            ms: BasicMergeState,
            c: Container,
            cc: Container,
            g: Group
    ): Boolean {
        log.send("Moving group: " + g.groupOrdinal + " from " + c + " to " + cc)
        ms.removeGroupContainerMapping(g, c)
        ms.addGroupContainerMapping(g, cc, GroupContainerState.HAS_CONTENT)
        return true
    }

    protected abstract fun groupChangedContainer(ms: BasicMergeState, g: Group)

    protected fun initContained(
            ms: BasicMergeState,
            leaves: MutableList<LeafGroup>,
            toAdd: LeafGroup
    ) {
        // set up container details
        val c2 = toAdd.container
        if (c2 != null) {
            ms.addGroupContainerMapping(
                    toAdd,
                    c2,
                    if (toAdd.occupiesSpace()) GroupContainerState.HAS_CONTENT
                    else GroupContainerState.NO_CONTENT
            )
        }
        leaves.add(toAdd)
    }

    /**
     * Throws out any merge options that don't make sense.
     *
     * Priority is returned, or null if the merge is irrelevant
     *
     * This is called before we add merge options to the queue, and also when we take them off the
     * queue, as the state will change.
     *
     * Extra parameters about aligned group are added to simplify the process for
     */
    open fun canGroupsMerge(
            a: Group,
            b: Group,
            ms: BasicMergeState,
            alignedGroup: Group?,
            d: Direction?
    ): Int {
        // not a real merge
        return if (a === b) {
            INVALID_MERGE
        } else 0
    }

    protected fun preMergeInitialisation(ms: BasicMergeState) {
        for (g in allGroups) {
            initContained(ms, mutableListOf(), g as LeafGroup)
        }
        val bottomLevelContainers = ArrayList(ms.getContainers())
        if (bottomLevelContainers.size > 0) {
            for (c in bottomLevelContainers) {
                if (isContainerMergeable(c, ms)) {
                    startContainerMerge(ms, c)
                }
                if (isContainerComplete(c, ms)) {
                    completeContainer(ms, c)
                }
            }
        } else if (allGroups.size == 1) {
            ms.addLiveGroup(allGroups.iterator().next())
        }
    }

    companion object {
        const val ILLEGAL_PRIORITY = 1000
        const val INVALID_MERGE = -1
    }
}
