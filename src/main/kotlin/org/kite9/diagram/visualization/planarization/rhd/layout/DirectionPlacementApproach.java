/**
 * 
 */
package org.kite9.diagram.visualization.planarization.rhd.layout;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.logging.Kite9Log;

/**
 * A placement approach arranges the two groups within a compound group, based on compass directions.
 * 
 * The placement approach positions the attr and then works out how much the placement 'costs' in terms of the
 * overlaps of the resulting edges, rather than the distances.
 * 
 * @author robmoffat
 * 
 */
public class DirectionPlacementApproach extends AbstractPlacementApproach implements PlacementApproach {

	private static final float DISTANCE_COST = 0.2f;  // mulitplier allowing you to add distance to crossings.

	public DirectionPlacementApproach(Kite9Log log, GroupPhase gp, Layout aDirection, CompoundGroup overall,
			RoutableHandler2D rh, boolean setHoriz, boolean setVert, boolean natural) {
		super(log, gp, aDirection, overall, rh, setHoriz, setVert, natural);
	}

	public void evaluate() {
		overall.setLayout(aDirection);
		rh.clearTempPositions(false);
		rh.clearTempPositions(true);
		
		log.send("Position of A" + overall.getA().getAxis().getPosition(rh, true));
		ExitMatrix aMatrix = createMatrix(overall.getA(), overall.getInternalLinkA());
		log.send("A Matrix: " +aMatrix);

		log.send("Position of B" + overall.getB().getAxis().getPosition(rh, true));
		ExitMatrix bMatrix = createMatrix(overall.getB(), overall.getInternalLinkB());
		log.send("B Matrix: " +bMatrix);
		
		// can't understand how this got reversed
		Layout matrixDirection = Layout.reverse(aDirection); 
				
		score = ExitMatrixEvaluator.countOverlaps(aMatrix, bMatrix, matrixDirection, rh);
		double externalDistance = ExitMatrixEvaluator.calculateExtraExternalLinkDistance(aMatrix, bMatrix, matrixDirection, rh);
		double internalDistance = ExitMatrixEvaluator.calculateInternalDistance(overall.getInternalLinkA(), rh);
		log.send("Overlap score: "+score);
		log.send("Internal distance cost: "+internalDistance);
		log.send("External distance cost: "+externalDistance);
		
		score += ((internalDistance + externalDistance) * DISTANCE_COST);
	}

	private ExitMatrix createMatrix(Group with, final LinkDetail ignore) {
		RoutingInfo position = with.getAxis().getPosition(rh, true);
		final ExitMatrix out = new ExitMatrix();
		out.setSize(rh.getBoundsOf(position, true), rh.getBoundsOf(position, false));
		
		with.processAllLeavingLinks(true, DirectedLinkManager.all(), new LinkProcessor() {
			
			@Override
			public void process(Group originatingGroup, Group destinationGroup, LinkDetail ld) {
				if (ld != ignore) {
					ld.processLowestLevel(new LinkProcessor() {
						@Override
						public void process(Group originatingGroup, Group destinationGroup, LinkDetail ld) {
							out.addLink(originatingGroup, destinationGroup, ld, rh);
						}
					});
				}
			}
		});
		
		return out;
	}
	
	


}