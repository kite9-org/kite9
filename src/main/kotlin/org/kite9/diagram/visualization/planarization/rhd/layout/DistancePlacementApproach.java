/**
 * 
 */
package org.kite9.diagram.visualization.planarization.rhd.layout;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.logging.Kite9Log;

/**
 * Score of the placement is based on the length of connections.
 * 
 * @author robmoffat
 *
 */
public class DistancePlacementApproach extends AbstractPlacementApproach implements PlacementApproach {

	public DistancePlacementApproach(Kite9Log log, GroupPhase gp, Layout aDirection, CompoundGroup overall,
			RoutableHandler2D rh, boolean setHoriz, boolean setVert, boolean natural) {
		super(log, gp, aDirection, overall, rh, setHoriz, setVert, natural);
	}

	public void evaluate() {
		overall.setLayout(aDirection);
		score = 0;
		rh.clearTempPositions(false);
		rh.clearTempPositions(true);
		log.send("Position of A"+overall.getA().getAxis().getPosition(rh, true));
		log.send("Position of B"+overall.getB().getAxis().getPosition(rh, true));
		evaluateLinks(overall.getA());
		evaluateLinks(overall.getB());
	}

	private void evaluateLinks(Group group) {
		group.processLowestLevelLinks(new LinkProcessor() {
			
			@Override
			public void process(Group from, Group to, LinkDetail ld) {
				RoutingInfo aRI = from.getAxis().getPosition(rh, true);
				RoutingInfo bRI = to.getAxis().getPosition(rh, true);
				double cost = rh.cost(aRI, bRI) * ld.getNumberOfLinks();
				score += cost;
				log.send("Evaluating: "+cost+
						"\n\tfrom "+((LeafGroup)from).getContained()+" at "+aRI+
						"\n\tto "+((LeafGroup)to).getContained()+" at "+bRI);
			}
		});
	}
	
}