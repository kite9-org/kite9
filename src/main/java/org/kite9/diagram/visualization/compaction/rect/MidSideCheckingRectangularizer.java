package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

/**
 * Does extra calculations of the {@link PrioritisedRectOption} to make sure that it will be
 * respecting middle-alignment of connections.
 */
public abstract class MidSideCheckingRectangularizer extends PrioritizingRectangularizer {

	public MidSideCheckingRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}


	/**
	 * If we have a 'safe' rectangularization, make sure meets can't increase
	 */
	@Override
	protected Action checkRectOptionIsOk(Set<VertexTurn> onStack, RectOption ro, PriorityQueue<RectOption> pq, Compaction c) {
		Action superAction = super.checkRectOptionIsOk(onStack, ro, pq, c);
		
		if (superAction != Action.OK) {
			return superAction;
		}
		
		log.send("Checking: "+ro);
		Direction turnDirection = ro.getTurnDirection(ro.getExtender());
		log.send("Extender: "+ro.getExtender()+" dir= "+turnDirection);
		VertexTurn meets = ro.getMeets();
		VertexTurn link = ro.getLink();
		VertexTurn par = ro.getPar();
		
		
						
		int meetsMinimumLength = checkMinimumLength(meets, link, c);

		int parMinimumLength = checkMinimumLength(par, link, c);
		
		if ((ro.getScore() != ro.getInitialScore())) {
			// change it and throw it back in - priority has changed.
			log.send("Deferring: "+meetsMinimumLength+" for meets="+meets+"\n         "+parMinimumLength+" for par="+par);
			return Action.PUT_BACK;
		}
		 

		return Action.OK; 
//		log.send("Allowing: meets="+ro.getMeets()+"\n          for par="+ro.getPar());						
	}

	private int checkMinimumLength(VertexTurn rect, VertexTurn link, Compaction c) {
		if (rect.getTurnPriority() == TurnPriority.MINIMIZE_RECTANGULAR) {
			if (shouldSetMidpoint(rect, link)) {
				
				// ok, size is needed of overall rectangle then half.
				Rectangular r = getRectangular(rect);
				boolean isHorizontal = !Direction.isHorizontal(rect.getDirection());
				OPair<Slideable<Segment>> along = 
						(isHorizontal ? c.getHorizontalSegmentSlackOptimisation() : c.getVerticalSegmentSlackOptimisation())
								.getSlideablesFor(r);

				OPair<Slideable<Segment>> perp = 
						(!isHorizontal ? c.getHorizontalSegmentSlackOptimisation() : c.getVerticalSegmentSlackOptimisation())
								.getSlideablesFor(r);
				
				alignSingleConnections(c, perp, along, false);
			}
		}
		
		return (int) rect.getLength(true);
	}
		
	private boolean shouldSetMidpoint(VertexTurn vt, VertexTurn link) {
		Set<Connected> connecteds = getConnecteds(vt);
		if (connecteds.size() == 1) {
			if ((link == null) || (link.getSegment().getConnections().size() == 1)) {
				Set<Connection> leavingConnections = vt.getLeavingConnections();
				if (leavingConnections.size() == 1) {
					
					Connected theConnected = connecteds.iterator().next();
					Connection theConnection = leavingConnections.iterator().next();
					
					if (!theConnection.meets(theConnected)) {
						// we should only do a mid-point if we're connecting to this element
						return false;
					}
					
					if ((link == null) || (link.getSegment().getConnections().containsAll(leavingConnections))) {
						return true;
					}
				}
			}
		}
		
		return false;
		
	}
	
	private Set<Connected> getConnecteds(VertexTurn vt) {
		return vt.getSegment().getRectangulars().stream().filter(r -> r instanceof Connected).map(r -> (Connected) r).collect(Collectors.toSet());
	}

	private Rectangular getRectangular(VertexTurn vt) {
		Set<Rectangular> r = vt.getSegment().getRectangulars();
	
		if (r.size() > 1) {
			throw new Kite9ProcessingException();
		} else if (r.size() == 0) {
			return null;
		}
		
		return r.iterator().next();
	}


	/**
	 * Sets up the mid-points as part of secondary sizing.
	 */
	protected void performSecondarySizing(Compaction c, Map<DartFace, List<VertexTurn>> stacks) {
		super.performSecondarySizing(c, stacks);
		stacks.values().stream()
			.flatMap(s -> s.stream())
			.filter(vt -> minimizeConnectedOnly(vt)) 
			.distinct()
			.forEach(vt -> {
				alignSingleConnections(c, vt);
			});
	}


	protected void alignSingleConnections(Compaction c, VertexTurn vt) {
		if (shouldSetMidpoint(vt, null)) {
			Connected underlying = (Connected) vt.getSegment().getUnderlyingInfo().stream()
					.map(ui -> ui.getDiagramElement())
					.filter(de-> de instanceof Connected)
					.findFirst().orElseThrow(() -> new LogicException());
			AlignmentResult out = alignSingleConnections(c, underlying, Direction.isHorizontal(vt.getDirection()), false);
			if (out != null) {
				vt.ensureMinLength(out.midPoint);
				vt.setNonExpandingLength(out.safe);
			}
		}
	}


	protected static boolean minimizeConnectedOnly(VertexTurn vt) {
		boolean out = vt.getSegment().getUnderlyingInfo().stream()
			.map(ui -> ui.getDiagramElement())
			.filter(underlying -> (underlying instanceof Connected))
			.filter(underlying -> (underlying instanceof Container)) // && (((Container) underlying).getSizing() == DiagramElementSizing.MINIMIZE))
			.filter(underlying -> { 
				return matchesPattern((Container) underlying, vt.getStartsWith().getUnderlying(), vt.getEndsWith().getUnderlying()) 
					|| matchesPattern((Container) underlying, vt.getEndsWith().getUnderlying(), vt.getStartsWith().getUnderlying());
			})
			.count() > 0;

		return out;
	}
	
	private static boolean matchesPattern(Container underlying, Segment underlyingEnd, Segment connectionEnd) {
		return underlyingEnd.hasUnderlying(underlying) && connectionEnd.getConnections().size()==1;
	}
	
}
