package org.kite9.diagram.visualization.orthogonalization.flow

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.orthogonalization.AbstractOrthogonalizer
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger
import org.kite9.diagram.visualization.planarization.Planarization

/**
 * Uses a flow algorithm to convert from a Planarization to an Orthogonalization.
 *
 * @author robmoffat
 *
 * @param <X>
</X> */
abstract class MappedFlowOrthogonalizer(val va: VertexArranger, val clc: EdgeConverter) : AbstractOrthogonalizer() {

    abstract fun createOptimisedFlowGraph(pln: Planarization): MappedFlowGraph

    override fun createOrthogonalization(pln: Planarization): Orthogonalization {
        return try {
            val fg = createOptimisedFlowGraph(pln)
            val fb: OrthBuilder = MappedFlowGraphOrthBuilder(va, fg, clc)
            fb.build(pln)
        } catch (le: LogicException) {
            log.send("Plan: $pln")
            throw le
        }
    }

    protected open fun checkFlows(fg: MappedFlowGraph?) {}
}