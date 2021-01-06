package org.kite9.diagram.visualization.planarization.mgt.builder

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.Table
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarizationImpl
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarizationBuilder
import java.util.*

/**
 * This follows the general GT approach to producing a maximal planar subgraph
 * by introducing edges above and below the line of the planarization, and as
 * many as possible.
 *
 * @author moffatr
 */
abstract class MGTPlanarizationBuilder(em: ElementMapper, gp: GridPositioner) : RHDPlanarizationBuilder(em, gp), Logable {

    @JvmField
	protected var log = Kite9Log(this)

    override fun buildPlanarization(
        d: Diagram,
        vertexOrder: List<Vertex>,
        initialUninsertedConnections: Collection<BiDirectional<Connected>>,
        sortedContainerContents: Map<Container, List<Connected>>
    ): MGTPlanarizationImpl {
        val p = MGTPlanarizationImpl(d, vertexOrder, initialUninsertedConnections, sortedContainerContents)
        logPlanarEmbeddingDetails(p, log)
        routableReader!!.initRoutableOrdering(vertexOrder)
        completeEmbedding(p)
        log.send(if (log.go()) null else "Initial Planar Embedding: \n$p")
        return p
    }

    /**
     * This method allows you to do any post-processing of the planarization.
     */
    protected open fun completeEmbedding(p: MGTPlanarization) {
        processConnections(p)
    }

    protected abstract fun processConnections(p: MGTPlanarization)

    override val prefix: String
        get() = "GTPB"
    override val isLoggingEnabled: Boolean
        get() = true

    companion object {
        @JvmStatic
		fun logPlanarEmbeddingDetails(pln: MGTPlanarization, log: Kite9Log) {
            val t = Table()
            val vertexOrder = pln.vertexOrder
            val size = vertexOrder.size
            val xPositions: MutableList<String> = ArrayList(size)
            val yPositions: MutableList<String> = ArrayList(size)
            val index: MutableList<String> = ArrayList(size)
            for (i in 0 until size) {
                val routingInfo = vertexOrder[i].routingInfo
                xPositions.add(routingInfo?.outputX() ?: "")
                yPositions.add(routingInfo?.outputY() ?: "")
                index.add("" + i)
            }
            t.addRow(index)
            t.addRow(vertexOrder)
            t.addRow(xPositions)
            t.addRow(yPositions)
            log.send(if (log.go()) null else "Vertex Notional Positions: \n", t)
        }
    }
}