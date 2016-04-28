package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.StateStorage;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.logging.Table;

/**
 * Adds the constraints to the flow graph one at a time. This is done by
 * subdividing the faces (which are modelled using {@link SubdivisionNode}'s
 * further and further down, to model the fact that we want to constrain the
 * number of edges within a portion or sets of portions.
 * 
 * A face starts off with 4 (inner face) or -4 corners from the main
 * {@link AbstractFlowOrthogonalizer} run.  To introduce a constraint to the
 * face, the portions in the face are divided into a clockwise and
 * anti-clockwise pair, both with a known number of edges.
 * 
 * We compare the number of edges with the number required, and then pump new
 * edges into one face-side and out of the other.
 * 
 * The way the faces are split is very important: a face subdivision can only be
 * subdivided further if it contains both clockwise and anti-clockwise portions.
 * Otherwise, it is constant for that nudge.
 * 
 * One important consideration is how many corners to pump into a subdivision:
 * clearly to create a certain angle between two edges you can add or reduce the
 * number of corners. So for any given constraint, you increase or decrease. For
 * n constraints this means 2^n potential paths. To reduce this, we tackle
 * constraints in order, dealing with single corners and face-bound routes. 
 * 
 * Should it be impossible to nudge in one direction (e.g. adding corners), we
 * try to nudge in the other (e.g. remove them).
 * 
 * @author robmoffat
 * 
 */
public class HierarchicalConstrainedFlowNudger extends AbstractConstraintNudger {

	public HierarchicalConstrainedFlowNudger(Map<Face, List<PortionNode>> facePortionMap) {
		super(facePortionMap);
	}

	public void processNudges(MappedFlowGraph fg, ConstraintGroup constraintsToNudge, Collection<SubdivisionNode> faceNodes) {
		Table nudges = new Table();
		Collection<Node> logNodes = getTableLogNodes(fg);
		nudges.addRow(logNodes, "comment");


		logSizes(logNodes, nudges, "start", "", "");

		Collection<NudgeItem> routesToNudge = createRouteList(constraintsToNudge, fg);
		log.send(log.go() ? null : "Routes to nudge: ", routesToNudge);

		boolean done = performNextNudge(fg, nudges, routesToNudge, 0, faceNodes, logNodes);

		if (!done) {
			throw new LogicException("Could not introduce nudges");
		}

		log.send(log.go() ? null : "Nudge summary:\n", nudges);
	}

	

	/**
	 * @return true if the next best nudge could be added
	 */
	private boolean performNextNudge(MappedFlowGraph fg, Table nudges, Collection<NudgeItem> routes,
			int constraintNumber, Collection<SubdivisionNode> subdivisions,
			Collection<Node> logNodes) {

		if (routes.size() == 4)
			return true;

		NudgeItem ni = getNextNudgeItem(routes);
		log.send(log.go() ? null : "Current nudge: "+ ni);

		// contains the details of how the faces are divided up
		List<Pair<SubdivisionNode>> splits = new ArrayList<Pair<SubdivisionNode>>();
		subdivideNodes(subdivisions, ni.portionsClockwise, ni.portionsAntiClockwise, splits, constraintNumber, fg);
		log.send(log.go() ? null : "Current subdivisions: ", displaySubdivisions(subdivisions));
		//addSourceAndSink(ni.portionsClockwise, ni.source, ni.portionsAntiClockwise, ni.sink, fg, subdivisions);
		checkFlowGraphIntegrity(fg, ni.source, ni.sink);
		log.send(log.go() ? null : "Splits: "+splits);
		log.send(log.go() ? null : "Current subdivisions: ", displaySubdivisions(subdivisions));
		Map<Object, Integer> state = StateStorage.storeState(fg);
		ConstrainedSSP ssp = new ConstrainedSSP(ni.source, ni.sink, splits);

		// now build the two future possible states
		int cornersBest = calculateCornersRequired(ni, true, true);
		int cornersWorst = calculateCornersRequired(ni, false, true);

		NudgeChoice bestChoice = new NudgeChoice(fg, state, cornersBest, ni, constraintNumber, subdivisions, ssp);
		NudgeChoice worstChoice = new NudgeChoice(fg, state, cornersWorst, ni, constraintNumber, subdivisions, ssp);
		
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
			boolean ok = performNextNudge(fg, nudges, routes, constraintNumber + 1, subdivisions,
					logNodes);
			if (ok) {
				return ok;
			} else {
				unlogSizes(nudges);
			}
		}

		// ok, best route didn't work - go for worse route
		if (worstChoice.evaluate() < Integer.MAX_VALUE) {
			worstChoice.apply();
			logSizes(logNodes, nudges, worstChoice.getNote(), "" + bestChoice.cost, "" + worstChoice.cost);
			boolean ok = performNextNudge(fg, nudges, routes, constraintNumber + 1, subdivisions,
					logNodes);
			if (ok) {
				return ok;
			} else {
				unlogSizes(nudges);				
			}
		}

		// neither route worked, undivide and return
		StateStorage.restoreState(fg, state);
		routes.add(ni);
		undivideNodes(subdivisions, splits, constraintNumber);
		//removeSourceAndSink(fg, ni.source, ni.sink);
		return false;		
	}
}
