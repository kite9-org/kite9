package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge

import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail

open class BasicMergeState(var contradictionHandler: ContradictionHandler, elements: Int) : GroupResult(), Logable {

    val log = Kite9Log.instance(this)

    enum class GroupContainerState(val isComplete: Boolean) {
        COMPLETE(true),
        INCOMPLETE(false);

        companion object {
            operator fun get(
                isComplete: Boolean
            ): GroupContainerState {
                return if (isComplete) COMPLETE else INCOMPLETE
            }
        }
    }

    protected val liveGroups: MutableSet<Group> = DetHashSet(elements * 2)

	protected val optionQueue // options in processing order
	    : PriorityQueue<MergeOption> = PriorityQueue(elements * 5 + 1, null)
    protected val bestOptions // contains all considered
            : MutableMap<MergeKey, MergeOption> = HashMap(elements * 5)

    // merge options, legal ones
    // outrank illegal ones
    private val groupContainers: MutableMap<Group, MutableMap<Rectangular, GroupContainerState>> = HashMap(elements)
    private val liveContainers: MutableSet<Rectangular> = UnorderedSet(elements * 2)
    private var nextMergeNumber = 0

    open fun removeLiveGroup(a: Group) {
        log.send("Completed group: $a")
        a.live = false
        liveGroups.remove(a)
        for (c in groupContainers[a]!!.keys) {
            val csi = containerStates[c]
            csi!!.contents.remove(a)
        }
    }

    fun removeLiveContainer(c: Rectangular) {
        liveContainers.remove(c)
    }

    fun addOption(mo: MergeOption): Boolean {
        val existing = bestOptions[mo.mk]
        if (existing == null || mo.compareTo(existing) == -1) {
            // this option is better than existing
            optionQueue.add(mo)
            bestOptions[mo.mk] = mo
            //log.send("New Merge Option: " + mo);
            return true
        }
        return false
    }

    fun getBestOption(mk: MergeKey): MergeOption? {
        return bestOptions[mk]
    }

    fun nextMergeOptionNumber(): Int {
        return nextMergeNumber++
    }

    fun isLiveGroup(group: Group): Boolean {
        return group.live
    }

    open fun addLiveGroup(group: Group) {
        liveGroups.add(group)
        group.live = true
    }

    fun addGroupContainerMapping(toAdd: Group, c2: Rectangular, newState: GroupContainerState) {
        var within = groupContainers[toAdd]
        if (within == null) {
            within = HashMap(5)
            groupContainers[toAdd] = within
        }
        log.send("Mapping " + toAdd.groupNumber + " into container " + c2 + " state= " + newState)
        within[c2] = newState
        val csi = getStateFor(c2)
        csi!!.contents.add(toAdd)
    }

    override fun getStateFor(c2: Rectangular): ContainerStateInfo? {
        var csi = super.getStateFor(c2)
        if (csi == null) {
            csi = ContainerStateInfo(c2)
            super.containerStates[c2] = csi
            for (c in c2.getContents()) {
                if (c is Rectangular && c is Connected) {
                    val csi2 = getStateFor(c)
                    if (csi2 != null) {
                        csi.incompleteSubcontainers.add((c))
                    }
                }
            }
        }
        return csi
    }

    fun addLiveContainer(c: Rectangular) {
        log.send("Making container live:$c")
        liveContainers.add(c)
    }

    fun groupsCount(): Int {
        return liveGroups.size
    }

    fun hasRemainingMergeOptions(): Boolean {
        return optionQueue.size() > 0
    }

    fun nextMergeOption(): MergeOption? {
        //log.send("Merge options:", optionQueue);
        val mo = optionQueue.remove()
        bestOptions.remove(mo!!.mk)
        return mo
    }

    protected open fun isLinkCounted(from: Group?, to: Group?, via: Group, ld: LinkDetail?): Boolean {
        return if (!via.isActive()) {
            false
        } else true
    }

    fun getContainersFor(a: Group?): Map<Rectangular, GroupContainerState>? {
        if (a is LeafGroup) {
             return groupContainers.getOrPut(a, { mutableMapOf(a.container to GroupContainerState.INCOMPLETE) } )
        } else {
            return groupContainers[a]
        }
    }

    fun removeGroupContainerMapping(g: Group, c: Rectangular): GroupContainerState? {
        val within = groupContainers[g] ?: throw LogicException("Group not present in an existing container$g")
        return within.remove(c)
    }

    fun isContainerLive(container: Rectangular): Boolean {
        return liveContainers.contains(container)
    }

    override fun groups(): Collection<Group> {
        return liveGroups
    }

    override val prefix: String
        get() = "MS  "

    override val isLoggingEnabled: Boolean
        get() = true
}