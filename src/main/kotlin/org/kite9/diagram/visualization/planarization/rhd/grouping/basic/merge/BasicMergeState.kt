package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge

import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail

open class BasicMergeState(var contradictionHandler: ContradictionHandler) : GroupResult(), Logable {

    val log = Kite9Log(this)

    enum class GroupContainerState(var hasContent: Boolean, var isComplete: Boolean) {
        HAS_CONTENT(true, false), COMPLETE_WITH_CONTENT(true, true), NO_CONTENT(
            false,
            false
        ),
        COMPLETE_NO_CONTENT(false, true);

        fun hasContent(): Boolean {
            return hasContent
        }

        companion object {
            operator fun get(
                contained: Boolean,
                isComplete: Boolean
            ): GroupContainerState {
                return if (contained) {
                    if (isComplete) COMPLETE_WITH_CONTENT else HAS_CONTENT
                } else {
                    if (isComplete) COMPLETE_NO_CONTENT else NO_CONTENT
                }
            }
        }
    }

    protected var liveGroups: MutableSet<Group>? = null
    @JvmField
	protected var optionQueue // options in processing order
            : PriorityQueue<MergeOption>? = null
    protected var bestOptions // contains all considered
            : MutableMap<MergeKey, MergeOption>? = null

    // merge options, legal ones
    // outrank illegal ones
    private var groupContainers: MutableMap<Group, MutableMap<Container, GroupContainerState>>? = null
    protected var liveContainers: MutableSet<Container>? = null
    protected var nextMergeNumber = 0
    open fun initialise(capacity: Int, containers: Int, log: Kite9Log?) {
        optionQueue = PriorityQueue(capacity * 5 + 1, null)
        bestOptions = HashMap(capacity * 5)
        groupContainers = HashMap(capacity)
        liveContainers = UnorderedSet(containers * 2)
        liveGroups = DetHashSet(capacity * 2)
    }

    open fun removeLiveGroup(a: Group) {
        log!!.send("Completed group: $a")
        a.live = false
        liveGroups!!.remove(a)
        for (c in groupContainers!![a]!!.keys) {
            val csi = containerStates[c]
            csi!!.contents.remove(a)
        }
    }

    fun removeLiveContainer(c: Container) {
        liveContainers!!.remove(c)
    }

    fun addOption(mo: MergeOption): Boolean {
        val existing = bestOptions!![mo.mk]
        if (existing == null || mo.compareTo(existing) == -1) {
            // this option is better than existing
            optionQueue!!.add(mo)
            bestOptions!![mo.mk] = mo
            //log.send("New Merge Option: " + mo);
            return true
        }
        return false
    }

    fun getBestOption(mk: MergeKey): MergeOption? {
        return bestOptions!![mk]
    }

    fun nextMergeOptionNumber(): Int {
        return nextMergeNumber++
    }

    fun isLiveGroup(group: Group): Boolean {
        return group.live
    }

    open fun addLiveGroup(group: Group) {
        liveGroups!!.add(group)
        group.live = true
    }

    fun addGroupContainerMapping(toAdd: Group, c2: Container, newState: GroupContainerState) {
        var within = groupContainers!![toAdd]
        if (within == null) {
            within = HashMap(5)
            groupContainers!![toAdd] = within
        }
        log!!.send("Mapping " + toAdd.groupNumber + " into container " + c2 + " state= " + newState)
        within[c2] = newState
        val csi = getStateFor(c2)
        csi!!.contents.add(toAdd)
    }

    override fun getStateFor(c2: Container): ContainerStateInfo? {
        var csi = super.getStateFor(c2)
        if (csi == null) {
            csi = ContainerStateInfo(c2)
            super.containerStates[c2] = csi
            for (c in c2.getContents()) {
                if (c is Container && c is Connected) {
                    val csi2 = getStateFor(c as Container)
                    if (csi2 != null) {
                        csi.incompleteSubcontainers.add((c as Container))
                    }
                }
            }
        }
        return csi
    }

    fun addLiveContainer(c: Container) {
        log!!.send("Making container live:$c")
        liveContainers!!.add(c)
    }

    fun groupsCount(): Int {
        return liveGroups!!.size
    }

    fun hasRemainingMergeOptions(): Boolean {
        return optionQueue!!.size() > 0
    }

    fun nextMergeOption(): MergeOption? {
        //log.send("Merge options:", optionQueue);
        val mo = optionQueue!!.remove()
        bestOptions!!.remove(mo!!.mk)
        return mo
    }

    protected open fun isLinkCounted(from: Group?, to: Group?, via: Group, ld: LinkDetail?): Boolean {
        return if (!via.isActive()) {
            false
        } else true
    }

    val containers: Collection<Container>
        get() = containerStates.keys

    fun getContainersFor(a: Group?): Map<Container, GroupContainerState>? {
        return if (groupContainers == null) {
            if (a is LeafGroup) {
                mapOf(a.container!! to GroupContainerState.HAS_CONTENT)
            } else {
                throw LogicException("Group Containers should have been initialised")
            }
        } else groupContainers!![a]
    }

    fun removeGroupContainerMapping(g: Group, c: Container): GroupContainerState? {
        val within = groupContainers!![g] ?: throw LogicException("Group not present in an existing container$g")
        return within.remove(c)
    }

    fun isContainerLive(container: Container?): Boolean {
        return liveContainers!!.contains(container)
    }

    override fun groups(): Collection<Group> {
        return liveGroups!!
    }

    override val prefix: String?
        get() = "MS  "

    override val isLoggingEnabled: Boolean
        get() = true
}