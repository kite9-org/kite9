package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeKey
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AxisHandlingGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.PriorityRule
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.merge.GeneratorMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.rules.AlignedDirectedPriorityRule
import org.kite9.diagram.visualization.planarization.rhd.grouping.rules.NeighbourDirectedPriorityRule
import org.kite9.diagram.visualization.planarization.rhd.grouping.rules.UndirectedPriorityRule
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler

/**
 * This contains the mechanism for creating merge options from generators.
 *
 * The general approach is to generate as few merge options as possible.  This is done by asking each
 * generator to create legal merge options.  Should a generator create a legal option, we stop
 * and process the merges from it.
 *
 * Generat
 *
 * @author robmoffat
 */
class GeneratorBasedGroupingStrategyImpl(
    top: DiagramElement,
    elements: Int,
    ch: ContradictionHandler,
    gp: GridPositioner,
    em: ElementMapper) :
    AxisHandlingGroupingStrategy(top, elements, ch, gp, em,
        GeneratorMergeState(
            ch, elements
        )
    ), GeneratorBasedGroupingStrategy {

    override fun addMergeOption(
        g1: Group,
        g2: Group,
        alignedGroup: Group?,
        alignedSide: Direction?,
        bestPriority: Int,
        ms: BasicMergeState
    ) {
        val mk = MergeKey(g1, g2)
        val best = ms.getBestOption(mk)
        if (best != null && best.priority <= bestPriority) {
            // we already have this option with at least as good a priority.
            return
        }
        val p = canGroupsMerge(g1, g2, ms, alignedGroup, alignedSide)
        if (p != INVALID_MERGE) {
            val mo = MergeOption(g1, g2, ms.nextMergeOptionNumber(), p, alignedGroup, alignedSide)
            mo.calculateMergeOptionMetrics(ms)
            val added = ms.addOption(mo)
            if (added) {
                log.send(if (log.go()) null else "Added Merge Option: $mo")
            }
        }
    }

    override fun addMergeOption(mo: MergeOption, bestPriority: Int, ms: BasicMergeState): Int {
        val mk = mo.mk
        val best = ms.getBestOption(mk)
        if (best != null && best.priority <= bestPriority) {
            // we already have this option with at least as good a priority.
            return best.priority
        }
        val c = updateMergeOption(mo)
        if (c == Change.DISCARD) {
            return INVALID_MERGE
        }
        val p = canGroupsMerge(mk.a, mk.b, ms, mo.alignedGroup, mo.alignedDirection)
        if (p != INVALID_MERGE) {
            mo.resetPriority(p)
            mo.calculateMergeOptionMetrics(ms)
            val added = ms.addOption(mo)
            if (added) {
                log.send(if (log.go()) null else "Added Merge Option: $mo")
            }
        }
        return p
    }

    /**
     * Add MergeOptions to the queue for a given group.
     */
    fun createMergeOptions(ms: BasicMergeState) {
        val gms = ms as GeneratorMergeState
        var next = gms.nextLiveGroup()
        while (next != null) {
            log.send("Merge options for:$next")
            for (strat in gms.generators!!) {
                strat.generate(next)
            }
            next = gms.nextLiveGroup()
        }
    }

    override fun group(): GroupResult {
        val capacity = groupCount
        val containers = containerCount
        ms.initialise(capacity, containers, log)
        setupMergeState(ms)
        preMergeInitialisation(ms)
        while (ms.groupsCount() > 1) {
            createMergeOptions(ms)
            var mo: MergeOption
            try {
                mo = ms.nextMergeOption()!!
                val c = updateMergeOption(mo)
                if (c == Change.CHANGED || c == Change.NO_CHANGE) {
                    val p = canGroupsMerge(mo.mk.a, mo.mk.b, ms, mo.alignedGroup, mo.alignedDirection)
                    if (p != INVALID_MERGE) {
                        if (p != mo.priority || c == Change.CHANGED) {
                            // poke it back in to use in desperation
                            mo.resetPriority(p)
                            mo.calculateMergeOptionMetrics(ms)
                            ms.addOption(mo)
                        } else {
                            if (p == ILLEGAL_PRIORITY) {
                                log.error("Inserting with Illegal: $mo")
                            }
                            performMerge(ms, mo)
                        }
                    }
                }
            } catch (e: RuntimeException) {
                log.send("Groups:", ms.groups())
                throw e
            }
        }
        return ms
    }

    internal enum class Change {
        NO_CHANGE, CHANGED, DISCARD
    }

    /**
     * This is called when a merge option changes because groups within it have already
     * been merged.
     */
    private fun updateMergeOption(mo: MergeOption): Change {
        val a = getWorkingGroup(mo.mk.a)
        val b = getWorkingGroup(mo.mk.b)
        val alignedGroup = getWorkingGroup(mo.alignedGroup)
        return if (a !== b && a !== alignedGroup && b !== alignedGroup) {
            if (a !== mo.mk.a || b !== mo.mk.b || alignedGroup !== mo.alignedGroup) {
                val newKey = MergeKey(a!!, b!!)
                mo.mk = newKey
                mo.alignedGroup = alignedGroup
                Change.CHANGED
            } else {
                Change.NO_CHANGE
            }
        } else Change.DISCARD
    }

    override fun introduceCombinedGroup(ms: BasicMergeState, combined: CompoundGroup) {
        combined.log(log)
        ms.addLiveGroup(combined)
    }

    override fun compatibleMerge(a: Group, b: Group): Boolean {
        return DirectedGroupAxis.compatibleNeighbour(a, b)
    }

    protected fun setupMergeState(bms: BasicMergeState) {
        val ms = bms as GeneratorMergeState

        // axis merges take priority over everything else
        ms.generators.add(AxisSingleMergeGenerator(this, ms, this))
        ms.generators.add(AxisAlignedMergeGenerator(this, ms, this))
        ms.generators.add(AxisNeighbourMergeGenerator(this, ms, this))

        // perpendicular, undirected, in-container merges
        ms.generators.add(ContainerUndirectedLinkedMergeGenerator(this, ms, this))
        ms.generators.add(ContainerUndirectedAlignedMergeGenerator(this, ms, this))
        ms.generators.add(ContainerUndirectedNeighbourMergeGenerator(this, ms, this))

        // perpendicular, directed, in & out of container merges
        ms.generators.add(PerpendicularAlignedMergeGenerator(this, ms, this))
        ms.generators.add(PerpendicularDirectedMergeGenerator(this, ms, this))


        // order is significant

        // axis merges first
        ms.rules.add(NeighbourDirectedPriorityRule(true))
        ms.rules.add(AlignedDirectedPriorityRule(true))

        // undirected merges
        ms.rules.add(UndirectedPriorityRule())

        // perpendicular merges
        ms.rules.add(NeighbourDirectedPriorityRule(false))
        ms.rules.add(AlignedDirectedPriorityRule(false))
    }

    override fun getRules(ms: DirectedMergeState): List<PriorityRule> {
        return (ms as GeneratorMergeState).rules!!
    }

    override fun startContainerMerge(ms: BasicMergeState, c: Container) {
        super.startContainerMerge(ms, c)
        for (gen in (ms as GeneratorMergeState).generators!!) {
            gen.containerIsLive(c)
        }
    }
}