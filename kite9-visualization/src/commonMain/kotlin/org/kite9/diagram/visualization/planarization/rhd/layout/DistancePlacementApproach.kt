/**
 */
package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * Score of the placement is based on the length of connections.
 *
 * @author robmoffat
 */
class DistancePlacementApproach(
        log: Kite9Log,
        aDirection: Layout?,
        overall: CompoundGroup,
        val rh: RoutableHandler2D,
        natural: Boolean
) : AbstractPlacementApproach(log, aDirection, overall, natural) {

    override fun evaluate() {
        overall.layout = aDirection
        score = 0.0
        rh.clearTempPositions(false)
        rh.clearTempPositions(true)
        log.send("Position of A" + overall.a.axis.getPosition(rh, true))
        log.send("Position of B" + overall.b.axis.getPosition(rh, true))
        evaluateLinks(overall.a)
        evaluateLinks(overall.b)
    }

    private fun evaluateLinks(group: Group) {
        group.processLowestLevelLinks(
                object : LinkProcessor {
                    override fun process(
                            originatingGroup: Group,
                            destinationGroup: Group,
                            ld: LinkDetail
                    ) {
                        val aRI = originatingGroup.axis.getPosition(rh, true)
                        val bRI = destinationGroup.axis.getPosition(rh, true)
                        val cost = rh.cost(aRI, bRI) * ld!!.numberOfLinks
                        score += cost
                        log.send(
                                """Evaluating: $cost
	from ${(originatingGroup as LeafGroup).connected} at $aRI
	to ${(destinationGroup as LeafGroup).connected} at $bRI"""
                        )
                    }
                }
        )
    }
}
