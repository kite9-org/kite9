package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState

abstract class AbstractMergeGenerator(
    val gp: GroupPhase,
    val ms: BasicMergeState,
    val grouper: GeneratorBasedGroupingStrategy
) : MergeGenerator, Logable {

    @JvmField
	protected var log = Kite9Log(this)

    open fun addMergeOption(
        g1: GroupPhase.Group,
        g2: GroupPhase.Group,
        alignedGroup: GroupPhase.Group?,
        alignedSide: Direction?
    ) {
        grouper.addMergeOption(g1, g2, alignedGroup, alignedSide, myBestPriority, ms)
    }

    override val prefix: String
        get() = "MG  "

    override val isLoggingEnabled: Boolean
        get() = true

    protected abstract val myBestPriority: Int

    override fun containerIsLive(c: Container) {}

    protected abstract val code: String?

}