package org.kite9.diagram.visualization.planarization.rhd.grouping.basic

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState.GroupContainerState
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor

/**
 * Merges groups together into a hierarchical structure for routing layout.
 *
 * MergeOptions hold the options for merge in a priority queue.
 *
 * @author robmoffat
 */
abstract class AbstractGroupingStrategy : GroupingStrategy, Logable {

	val log = Kite9Log(this)

    override val prefix: String
        get() = "GS  "

    override val isLoggingEnabled: Boolean
        get() = true

    /**
     * Actually does the merge suggested by the merge option, replacing the individual groups with a compound group.
     */
    protected fun performMerge(gp: GroupPhase, ms: BasicMergeState, mo: MergeOption) {
        if (mo.priority >= ILLEGAL_PRIORITY) {
            log.error(if (log.go()) null else "Merging: $mo")
        } else {
            log.send(if (log.go()) null else "Merging: $mo")
        }
        val combined = createCompoundGroup(gp, ms, mo)
        if (combined != null) {
            doCompoundGroupInsertion(gp, ms, combined, false)
        }
    }

    protected open fun doCompoundGroupInsertion(
        gp: GroupPhase,
        ms: BasicMergeState,
        combined: CompoundGroup,
        skipContainerCompletionCheck: Boolean
    ) {
        updateContainers(combined, ms)
        removeOldGroups(gp, ms, combined)
        handleReferences(combined)
        introduceCombinedGroup(gp, ms, combined)
        if (!skipContainerCompletionCheck) {
            checkGroupsContainersAreComplete(combined, gp, ms)
        }
    }

    /**
     * See if the merging of this group completes any containers, and change the container states
     * accordingly.
     */
    private fun updateContainers(
        group: CompoundGroup,
        ms: BasicMergeState) {
        val containersA = ms.getContainersFor(group.a)
        val containersB = ms.getContainersFor(group.b)
        val combined: MutableSet<Container> = LinkedHashSet(containersA.keys)
        combined.addAll(containersB.keys)
        val iterator: Iterator<Container> = combined.iterator()
        while (iterator.hasNext()) {
            val container = iterator.next()
            val aContained = containersA[container]
            val bContained = containersB[container]
            val contained = aContained != null && aContained.hasContent() ||
                    bContained != null && bContained.hasContent()
            val isComplete = aContained != null && aContained.isComplete ||
                    bContained != null && bContained.isComplete
            val out = GroupContainerState.get(contained, isComplete)
            ms.addGroupContainerMapping(group, container, out)
        }
    }

    /**
     * Performs the functions needed to add the new compound group to the merge state
     */
    protected abstract fun introduceCombinedGroup(gp: GroupPhase?, ms: BasicMergeState?, combined: CompoundGroup?)
    protected abstract fun createCompoundGroup(gp: GroupPhase?, ms: BasicMergeState?, mo: MergeOption?): CompoundGroup?

    /**
     * This works out how to handle references going to the components of a new compound group.
     */
    protected fun handleReferences(group: CompoundGroup) {
        val a = group.a
        val b = group.b
        group.processAllLeavingLinks(true, group.linkManager.allMask(), object : LinkProcessor {
            override fun process(originatingGroup: GroupPhase.Group, to: GroupPhase.Group, ld: LinkDetail) {
                if (to.isActive) {
                    to.linkManager.notifyMerge(group, a.isActive, b.isActive)
                }
            }
        })
    }

    /**
     * Checks the groups that were part of the merge and removes them.
     */
    protected abstract fun removeOldGroups(gp: GroupPhase?, ms: BasicMergeState?, combined: CompoundGroup?)

    /**
     * If the provided group doesn't need any more merging, remove it from the merge state, and potentially, promote
     * it's container.
     */
    protected fun checkGroupsContainersAreComplete(group: GroupPhase.Group, gp: GroupPhase, ms: BasicMergeState) {
        val containerMap = ms.getContainersFor(group)
        val containers: Set<Container> = containerMap.keys
        val containers2 = ArrayList(containers)
        for (container in containers2) {
            val state = containerMap[container]
            if (state != null && !state.isComplete) {
                if (isContainerComplete(container, ms)) {
                    completeContainer(gp, ms, container)
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

    protected fun completeContainer(gp: GroupPhase, ms: BasicMergeState, c: Container) {
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
            val toIterate: List<GroupPhase.Group> = ArrayList(csiChild.contents)
            var promotionOK = true
            for (g in toIterate) {
                promotionOK = checkGroupChangeContainer(ms, c, cc, g) && promotionOK
            }
            if (promotionOK) {
                if (isContainerMergeable(cc, ms)) {
                    startContainerMerge(ms, cc)
                }
                if (isContainerComplete(cc, ms)) {
                    completeContainer(gp, ms, cc)
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
        g: GroupPhase.Group
    ): Boolean {
        log.send("Moving group: " + g.groupNumber + " from " + c + " to " + cc)
        ms.removeGroupContainerMapping(g, c)
        ms.addGroupContainerMapping(g, cc, GroupContainerState.HAS_CONTENT)
        return true
    }

    protected abstract fun groupChangedContainer(ms: BasicMergeState, g: GroupPhase.Group)

    protected fun initContained(
        gp: GroupPhase,
        ms: BasicMergeState,
        leaves: MutableList<LeafGroup>,
        toAdd: LeafGroup
    ) {
        // set up container details
        val c2 = toAdd.container
        if (c2 != null) {
            ms.addGroupContainerMapping(
                toAdd, c2,
                if (toAdd.contained != null) GroupContainerState.HAS_CONTENT else GroupContainerState.NO_CONTENT
            )
        }
        leaves?.add(toAdd)
    }

    /**
     * Throws out any merge options that don't make sense.
     *
     * Priority is returned, or null if the merge is irrelevant
     *
     * This is called before we add merge options to the queue, and also when we take them
     * off the queue, as the state will change.
     *
     * Extra parameters about aligned group are added to simplify the process for
     */
    open fun canGroupsMerge(
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        ms: BasicMergeState,
        alignedGroup: GroupPhase.Group?,
        d: Direction?
    ): Int {
        // not a real merge
        return if (a === b) {
            INVALID_MERGE
        } else 0
    }

    protected fun preMergeInitialisation(gp: GroupPhase, ms: BasicMergeState) {
        for (g in gp.allGroups) {
            initContained(gp, ms, mutableListOf(), g)
        }
        val bottomLevelContainers: List<Container> = ArrayList(ms.containers)
        if (bottomLevelContainers.size > 0) {
            for (c in bottomLevelContainers) {
                if (isContainerMergeable(c, ms)) {
                    startContainerMerge(ms, c)
                }
                if (isContainerComplete(c, ms)) {
                    completeContainer(gp, ms, c)
                }
            }
        } else if (gp.allGroups.size == 1) {
            ms.addLiveGroup(gp.allGroups.iterator().next())
        }
    }

    companion object {
        const val ILLEGAL_PRIORITY = 1000
        const val INVALID_MERGE = -1
    }
}