/**
 *
 */
package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup
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
    gp: GroupPhase,
    aDirection: Layout?,
    overall: CompoundGroup,
    rh: RoutableHandler2D,
    setHoriz: Boolean,
    setVert: Boolean,
    natural: Boolean
) : AbstractPlacementApproach(
    log, gp, aDirection, overall, rh, setHoriz, setVert, natural
) {

    override fun evaluate() {
        overall.layout = aDirection
        score = 0.0
        rh.clearTempPositions(false)
        rh.clearTempPositions(true)
        log.send("Position of A" + overall.a.getAxis().getPosition(rh, true))
        log.send("Position of B" + overall.b.getAxis().getPosition(rh, true))
        evaluateLinks(overall.a)
        evaluateLinks(overall.b)
    }

    private fun evaluateLinks(group: GroupPhase.Group) {
        group.processLowestLevelLinks(object : LinkProcessor {
            override fun process(from: GroupPhase.Group, to: GroupPhase.Group, ld: LinkDetail) {
                val aRI = from.getAxis().getPosition(rh, true)
                val bRI = to.getAxis().getPosition(rh, true)
                val cost = rh.cost(aRI, bRI) * ld!!.numberOfLinks
                score += cost
                log.send(
                    """Evaluating: $cost
	from ${(from as LeafGroup).contained} at $aRI
	to ${(to as LeafGroup).contained} at $bRI"""
                )
            }
        })
    }
}