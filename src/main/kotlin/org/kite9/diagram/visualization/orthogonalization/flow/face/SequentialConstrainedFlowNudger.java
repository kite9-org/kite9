package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.StateStorage;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.logging.LogicException;
import org.kite9.diagram.logging.Table;

/**
 * Handles nudges one-at-a-time, making the assumption that corners are fixed.
 * 
 * Because corners are fixed, the remaining freedom to nudge means that you should be 
 * able to nudge in either direction.  Given this, we only nudge in the direction that involves
 * least change to the diagram.
 * 
 * @author robmoffat
 *
 */
public class SequentialConstrainedFlowNudger extends AbstractConstraintNudger {

	public SequentialConstrainedFlowNudger(Map<Face, List<PortionNode>> facePortionMap) {
		super(facePortionMap);
	}

	public void processNudges(MappedFlowGraph fg, ConstraintGroup constraintsToNudge, Collection<SubdivisionNode> faceNodes) {
//		 if (true)
//		 return;

		Table nudges = new Table();
		Collection<Node> logNodes = getTableLogNodes(fg);
		nudges.addRow(logNodes, "comment");


		logSizes(logNodes, nudges, "start", "", "");

		Collection<NudgeItem> routesToNudge = createRouteList(constraintsToNudge, fg);
		
		int failed = 0;
		
		log.send(log.go() ? null : "Routes to nudge: ", routesToNudge);
		
		while (routesToNudge.size() > 0) {
			if (!performNextNudge(fg, nudges, routesToNudge, faceNodes, logNodes)) {
				failed++;
				
				//throw new LogicException("Could not introduce all nudges");
				return;
			}
		}

		log.send(log.go() ? null : "Failed "+failed+" nudges.  Nudge summary:\n", nudges);
		if (failed > 0) {
			throw new LogicException("Could not introduce all nudges");
		}
	}

	/**
	 * @return true if the next best nudge could be added
	 */
	private boolean performNextNudge(MappedFlowGraph fg, Table nudges, Collection<NudgeItem> routes, Collection<SubdivisionNode> subdivisions,
			Collection<Node> logNodes) {
		
		NudgeItem ni = getNextNudgeItem(routes);
		int constraintNumber = ni.getId();
		
//		if (constraintNumber==9) {
//			return false;
//		}

		// contains the details of how the faces are divided up
		List<Pair<SubdivisionNode>> splits = new ArrayList<Pair<SubdivisionNode>>();
		log.send(log.go() ? null : "Current nudge: "+ ni);
		subdivideNodes(subdivisions, ni.getPortionsClockwise(), ni.getPortionsAntiClockwise(), splits, constraintNumber, fg);
		log.send(log.go() ? null : "Current subdivisions: ", displaySubdivisions(subdivisions));
		//addSourceAndSink(ni.portionsClockwise, ni.source, ni.portionsAntiClockwise, ni.sink, fg, subdivisions);
		checkFlowGraphIntegrity(fg, ni.getSource(), ni.sink);
		//log.send(log.go() ? null : "Splits: "+splits);
		//log.send(log.go() ? null : "Current subdivisions: ", displaySubdivisions(subdivisions));
		Map<Object, Integer> state = StateStorage.storeState(fg);
		ConstrainedSSP ssp = new ConstrainedSSP(ni.getSource(), ni.sink, splits);

		// now build the two future possible states
		int cornersBest = calculateCornersRequired(ni, true, true);
		int cornersWorst = calculateCornersRequired(ni, false, true);

		NudgeChoice bestChoice = new NudgeChoice(this, fg, state, cornersBest, ni, constraintNumber, subdivisions, ssp);
		NudgeChoice worstChoice = new NudgeChoice(this, fg, state, cornersWorst, ni, constraintNumber, subdivisions, ssp);
		
		// order the choices
		if ((Math.abs(cornersBest) == Math.abs(cornersWorst)) && (worstChoice.evaluate() < bestChoice.evaluate())) {
			NudgeChoice temp = bestChoice;
			bestChoice = worstChoice;
			worstChoice = temp;
		}

		// going to use best route first
		if (bestChoice.evaluate() < Integer.MAX_VALUE) {
			bestChoice.apply();
			logSizes(logNodes, nudges, bestChoice.getNote(), "" + bestChoice.cost, "" + worstChoice.cost);
			return true;
		}

		// ok, best route didn't work - go for worse route
		if (worstChoice.evaluate() < Integer.MAX_VALUE) {
			worstChoice.apply();
			logSizes(logNodes, nudges, worstChoice.getNote(), "" + bestChoice.cost, "" + worstChoice.cost);
			return true;
		}

		log.error("Failed Nudge: "+ni);
		
		// neither route worked, undivide and return
//		StateStorage.restoreState(fg, state);
//		routes.add(ni);
//		undivideNodes(subdivisions, splits, constraintNumber);
		//removeSourceAndSink(fg, ni.source, ni.sink);
		return false;		
	}

	
}
