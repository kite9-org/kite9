/**
 */
package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Direction
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
        aDirection: Direction?,
        overall: CompoundGroup,
        rh: RoutableHandler2D,
        natural: Boolean
) : AbstractPlacementApproach(log, aDirection, overall, natural, rh) {

    override fun evaluate() {
        overall.setLayout(Layout.fromDirection(aDirection))
        score = 0.0
        log.send("Position of A" + rh.getPlacedPosition(overall.a))
        log.send("Position of B" + rh.getPlacedPosition(overall.b))
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
                        val cost = rh.cost(originatingGroup, destinationGroup) * ld.numberOfLinks
                        score += cost
                        log.send(
                                """Evaluating: $cost
	from ${(originatingGroup as LeafGroup).connected} at ${rh.getPlacedPosition(originatingGroup)}
	to ${(destinationGroup as LeafGroup).connected} at ${rh.getPlacedPosition(destinationGroup)}"""
                        )
                    }
                }
        )
    }
}
