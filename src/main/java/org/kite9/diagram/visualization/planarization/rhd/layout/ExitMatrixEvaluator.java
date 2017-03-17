package org.kite9.diagram.visualization.planarization.rhd.layout;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.layout.ExitMatrix.RelativeSide;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos;

/**
 * Tries to work out the likelihood of an overlap.
 * 
 * @author robmoffat
 * 
 */
public class ExitMatrixEvaluator {

	private static final float OCCLUDED_COST = .5f;
	private static final float STRAIGHT_VS_CORNER_COST = .5f;

	/**
	 * A heuristic calculation for the likely number of crossings due to this arragement
	 */
	public static double countOverlaps(ExitMatrix aMatrix, ExitMatrix bMatrix, Layout aDirection, RoutableHandler2D rh) {
		float sideA = sumOneSide(aMatrix, bMatrix, aDirection, -1);
		float sideB = sumOneSide(aMatrix, bMatrix, aDirection, +1);
		float occlusionA = addOneOccludedSide(aMatrix, bMatrix, aDirection, rh);
		float occlusionB = addOneOccludedSide(bMatrix, aMatrix, Layout.reverse(aDirection), rh);

		return sideA + sideB + occlusionA + occlusionB;
	}
	
	/**
	 * Works out the extra distance of external links due to this arrangement
	 */
	public static double calculateExtraExternalLinkDistance(ExitMatrix aMatrix, ExitMatrix bMatrix, Layout aDirection, RoutableHandler2D rh) {
		return calcEEDistanceOneSide(aMatrix, bMatrix.getSizeInDirectionOfLayout(aDirection), aDirection) + 
			calcEEDistanceOneSide(bMatrix, aMatrix.getSizeInDirectionOfLayout(aDirection), Layout.reverse(aDirection)); 
	}
	
	
	

	private static double calcEEDistanceOneSide(ExitMatrix aMatrix, Bounds distance, Layout d) {
		float count = aMatrix.getLinkCount(d, RelativeSide.OPPOSITE, -1)+
			aMatrix.getLinkCount(d, RelativeSide.OPPOSITE, 0)+
			aMatrix.getLinkCount(d, RelativeSide.OPPOSITE, 1);
		
		double incrementalDistance = distance.getDistanceMax() - distance.getDistanceMin();
		
		return count * incrementalDistance;
	}

	/**
	 * Works out the cost score for the distance travelled by the internal links
	 */
	public static double calculateInternalDistance(LinkDetail internal, final RoutableHandler2D rh) {
		if (internal == null)
			return 0;
		
		final double out[] = { 0 };
		internal.processLowestLevel(new LinkProcessor() {
			
			@Override
			public void process(Group from, Group to, LinkDetail ld) {
				RoutingInfo aRI = from.getAxis().getPosition(rh, true);
				RoutingInfo bRI = to.getAxis().getPosition(rh, true);
				double cost = rh.cost(aRI, bRI) * ld.getNumberOfLinks();
				out[0] += cost;
//				log.send("Evaluating: "+cost+
//						"\n\tfrom "+((LeafGroup)from).getContained()+" at "+aRI+
//						"\n\tto "+((LeafGroup)to).getContained()+" at "+bRI);
			}
		});
		
		return out[0];
	}

	/**
	 * Figures out a cost associated with one side of the matrix based on the
	 * likelihood of leaving edges crossing one another.
	 */
	private static float sumOneSide(ExitMatrix aMatrix, ExitMatrix bMatrix, Layout ad, int rank) {
		float bFacing = bMatrix.getLinkCount(ad, RelativeSide.FACING, rank);
		float aMid = aMatrix.getLinkCount(ad, RelativeSide.MIDDLE, rank);
		float bMid = bMatrix.getLinkCount(ad, RelativeSide.MIDDLE, rank);
		float aOpp = aMatrix.getLinkCount(ad, RelativeSide.OPPOSITE, rank);

		float cornerCross = bFacing * aOpp;
		float midCrossB = (aOpp * bMid) * STRAIGHT_VS_CORNER_COST;
		float midCrossA = (bFacing * aMid) * STRAIGHT_VS_CORNER_COST;

		return cornerCross + midCrossB + midCrossA;
	}

	/**
	 * Work out how many edges you will have to cross to get from the occluded square to where you want to go.
	 * 
	 * When a matrix with a small span is occluded by one with a large span, we need to 
	 * add crossings in order to "break in" to the space of the larger one.
	 */
	private static float addOneOccludedSide(ExitMatrix aMatrix, final ExitMatrix bMatrix, Layout ad,
			RoutableHandler2D rh) {
		float aOccluded = aMatrix.getLinkCount(ad, RelativeSide.OPPOSITE, 0); 
		
		if (aOccluded == 0) {
			return 0;
		}

		
		float facingOccluding = bMatrix.getLinkCount(ad, RelativeSide.FACING, 0) / 2;
		float lowerOccluding = getLinkCountOnSide(bMatrix, ad, -1) + facingOccluding;
		float higherOccluding = getLinkCountOnSide(bMatrix, ad, 1)+ facingOccluding;
		
		lowerOccluding *= aOccluded;
		higherOccluding *= aOccluded;
		
		// large span check
		Bounds aSpan = aMatrix.getSpanInDirectionOfLayout(Layout.reverse(ad));
		Bounds bSpan = bMatrix.getSpanInDirectionOfLayout(Layout.reverse(ad));
		
		if (rh.compareBounds(aSpan, bSpan) == DPos.OVERLAP) {
			float bOccluding = bMatrix.getLinkCount(ad, RelativeSide.OPPOSITE, 0);
			
			// works out the occluded span
			double occludedMin = Math.max(aSpan.getDistanceMin(), bSpan.getDistanceMin());
			double occludedMax = Math.min(aSpan.getDistanceMax(), bSpan.getDistanceMax());
			double topOccludedFrac = ( occludedMax - occludedMin ) / ((aSpan.getDistanceMax() - aSpan.getDistanceMin()));
			
			// work out how much of b you would have to cross to get back to where you started from
			double lowerTravel = occludedMax - bSpan.getDistanceMin();
			double higherTravel = bSpan.getDistanceMax() - occludedMin;
				
			// work out travel as a fraction of b
			double lowerFrac = lowerTravel / (bSpan.getDistanceMax() - bSpan.getDistanceMin());
			double higherFrac = higherTravel / (bSpan.getDistanceMax() - bSpan.getDistanceMin());
			
			// work out how many edge crossings that's likely to be
			double lowerAmt = topOccludedFrac * lowerFrac * bOccluding * aOccluded;
			double higherAmt = topOccludedFrac * higherFrac * bOccluding* aOccluded;
			lowerOccluding += lowerAmt;
			higherOccluding += higherAmt;
		}
		
		return Math.min(lowerOccluding, higherOccluding) * OCCLUDED_COST;
	}

	private static float getLinkCountOnSide(final ExitMatrix mat, Layout ad, int side) {
		return mat.getLinkCount(ad, RelativeSide.FACING, side) + mat.getLinkCount(ad, RelativeSide.MIDDLE, side)
				+ mat.getLinkCount(ad, RelativeSide.OPPOSITE, side);
	}

}
