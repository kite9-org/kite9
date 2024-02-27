package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

/**
 * This turns the diagram's hierarchical structure of DiagramElement's into C2Slideables.
 *
 * @author robmoffat
 */
class C2ContainerBuilderCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) : AbstractC2BuilderCompactionStep(cd, gp) {

    override val prefix: String
        get() = "CBCS"

    override val isLoggingEnabled: Boolean
        get() = true

    var firstGroup = true;

    override fun compact(c: C2Compaction, g: Group) {
        if (firstGroup) {
            // we only need do this once for the whole group structure
            checkCreate(c.getDiagram(), Dimension.H, c.getSlackOptimisation(Dimension.H), null, g)
            checkCreate(c.getDiagram(), Dimension.V, c.getSlackOptimisation(Dimension.V), null, g)
            firstGroup = false
        }
    }

    /**
     * Either content are laid out using the Group process, or they aren't connected so we need
     * to just follow layout.
     */
    override fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?) : Boolean {
        val gm = contents.map { hasGroup(it, topGroup) }
        return gm.reduceRightOrNull {  a, b -> a && b } ?: false
    }

    private fun hasGroup(item: Connected, group: Group?) : Boolean {
        return if (group is CompoundGroup) {
            hasGroup(item, group.a) || hasGroup(item, group.b)
        } else if (group is LeafGroup){
            (group.container == item) || (group.connected == item);
        } else {
            false;
        }
    }

}