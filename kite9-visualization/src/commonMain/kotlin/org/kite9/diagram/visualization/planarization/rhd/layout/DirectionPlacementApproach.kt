/**
 *
 */
package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.Layout.Companion.reverse
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LayoutSetPoint
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * A placement approach arranges the two groups within a compound group, based on compass directions.
 *
 * The placement approach positions the attr and then works out how much the placement 'costs' in terms of the
 * overlaps of the resulting edges, rather than the distances.
 *
 * @author robmoffat
 */
class DirectionPlacementApproach(
    log: Kite9Log,
    aDirection: Direction?,
    overall: CompoundGroup,
    rh: RoutableHandler2D,
    natural: Boolean
) : AbstractPlacementApproach(
    log, aDirection, overall, natural, rh
) {

    val ev = ExitMatrixEvaluator()

    override fun evaluate() {
        overall.setLayout(Layout.fromDirection(aDirection), LayoutSetPoint.PLACEMENT_APPROACH)
        log.send("Position of A" + rh.getPlacedPosition(overall.a))
        val aMatrix = createMatrix(overall.a, overall.internalLinkA)
        log.send("A Matrix: $aMatrix")
        log.send("Position of B" + rh.getPlacedPosition(overall.b))
        val bMatrix = createMatrix(overall.b, overall.internalLinkB)
        log.send("B Matrix: $bMatrix")

        // can't understand how this got reversed
        val matrixDirection = Layout.fromDirection(Direction.reverse(aDirection))
        score = ev.countOverlaps(aMatrix, bMatrix, matrixDirection!!, rh)
        val externalDistance = ev.calculateExtraExternalLinkDistance(aMatrix, bMatrix, matrixDirection, rh)
        val internalDistance = ev.calculateInternalDistance(overall.internalLinkA, rh)
        log.send("Overlap score: $score")
        log.send("Internal distance cost: $internalDistance")
        log.send("External distance cost: $externalDistance")
        score += (internalDistance + externalDistance) * DISTANCE_COST
    }

    private fun createMatrix(with: Group, ignore: LinkDetail?): ExitMatrix {
        val out = ExitMatrix()
        out.setSize(rh.getBoundsOf(with, Dimension.H), rh.getBoundsOf(with, Dimension.V))
        with.processAllLeavingLinks(true, DirectedLinkManager.all(), object : LinkProcessor {
            override fun process(
                originatingGroup: Group,
                destinationGroup: Group,
                ld: LinkDetail
            ) {
                if (ld !== ignore) {
                    ld.processLowestLevel(object : LinkProcessor {
                        override fun process(
                            originatingGroup: Group,
                            destinationGroup: Group,
                            ld: LinkDetail
                        ) {
                            out.addLink(originatingGroup, destinationGroup, ld, rh)
                        }
                    })
                }
            }
        })
        return out
    }

    companion object {
        private const val DISTANCE_COST = 0.2f // mulitplier allowing you to add distance to crossings.
    }
}