package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

public class Temp {


	/**
	 * A pop-out is where meets and par both need to be kept minimal. So, we
	 * introduce 3 new segments, replacing meets, par, link with a three sides
	 * of a new rectangle for which there are no edge limits
	 * 
	 * @param c
	 */
	protected VertexTurn performPopOut(Compaction c, List<Dart> out, VertexTurn meets, VertexTurn link, VertexTurn par,
			VertexTurn ext, Slideable<Segment> parFrom, Slideable<Segment> meetsFrom, List<VertexTurn> stack, Match m) {

		fixSize(c, link, 0);
		fixSize(c, meets, 0);
		fixSize(c, par, 0);
		fixSize(c, ext, 0);
 
		Orthogonalization o = c.getOrthogonalization();

		// create the replacement for link
		Segment newLinkSeg = c.newSegment(link.getSegment().getDimension());
		Vertex parV = c.createCompactionVertex(newLinkSeg, par.getSegment());
		Vertex meetsV = c.createCompactionVertex(newLinkSeg, meets.getSegment());
		Vertex startsWith = m == Match.A ? parV : meetsV;
		Vertex endsWith = m == Match.A ? meetsV : parV;
		Direction d = link.getDirection();
		VertexTurn newLinkTurn = new VertexTurn(newLinkSeg, c, link.getDirection(), link.getChangeCost(), startsWith, endsWith, );
		Dart dLink = o.createDart(parV, meetsV, null, m == Match.A ? link.d : Direction.reverse(link.d), link.getUnderlying().getLength());
		// dLink.setChangeCostChangeEarlyBothEnds(link.getUnderlying().getChangeCost());
		newLinkTurn.setUnderlying(dLink);
		newLinkTurn.d = link.d;

		int ci = stack.indexOf(link);
		stack.set(ci, newLinkTurn);

		// reverse the par segment
		Dart dPar = o.createDart(parFrom, parV, null, m == Match.A ? meets.d : Direction.reverse(meets.d), 0);
		fixDartSize(c, dPar);
		dPar.setChangeCost(Dart.EXTEND_IF_NEEDED, null);
		par.d = Direction.reverse(par.d);
		if (m == Match.A) {
			par.endsWith = parV;
		} else {
			par.startsWith = parV;
		}
		par.setUnderlying(dPar);

		// reverse the meets segment
		Dart dMeets = o.createDart(meetsFrom, meetsV, null, m == Match.A ? meets.d : Direction.reverse(meets.d), 0);
		fixDartSize(c, dMeets);
		dMeets.setChangeCost(Dart.EXTEND_IF_NEEDED, null);
		meets.d = Direction.reverse(meets.d);
		if (m == Match.A) {
			meets.startsWith = meetsV;
		} else {
			meets.endsWith = meetsV;
		}
		meets.setUnderlying(dMeets);
		log.send(log.go() ? null : "Updated par: " + par);
		log.send(log.go() ? null : "Updated link: " + newLinkTurn);
		log.send(log.go() ? null : "Updated meets: " + meets);

		return newLinkTurn;
	}

}
