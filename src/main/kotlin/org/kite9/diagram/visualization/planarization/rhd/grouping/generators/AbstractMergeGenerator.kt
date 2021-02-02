package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState

abstract class AbstractMergeGenerator(
    val gp: GroupPhase,
    val ms: BasicMergeState,
    val grouper: GeneratorBasedGroupingStrategy
) : MergeGenerator, Logable {

    @JvmField
	protected var log = Kite9Log.instance(this)

    open fun addMergeOption(
        g1: Group,
        g2: Group,
        alignedGroup: Group?,
        alignedSide: Direction?
    ) {
        grouper.addMergeOption(g1, g2, alignedGroup, alignedSide, getMyBestPriority(), ms)
    }

    override val prefix: String
        get() = "MG  "

    override val isLoggingEnabled: Boolean
        get() = true

    protected abstract fun getMyBestPriority(): Int

    override fun containerIsLive(c: Container) {}

    abstract fun getCode(): String

}