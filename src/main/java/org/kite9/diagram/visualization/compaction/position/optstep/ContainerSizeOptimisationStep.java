package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.framework.logging.Logable;

/**
 * Makes containers as large as possible, within existing constraints. </ul>
 * 
 * @author robmoffat
 * 
 */
public class ContainerSizeOptimisationStep implements OptimisationStep, Logable {
	
	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		sizeContainerRule(xo);
		sizeContainerRule(yo);
	}

	private void sizeContainerRule(SegmentSlackOptimisation xo) {
		Map<Container, Pair<List<Slideable>>> containerMap = new HashMap<Container, Pair<List<Slideable>>>();

		for (Slideable s : xo.getCanonicalOrder()) {
			Segment segment = (Segment)s.getUnderlying();
			DiagramElement de = segment.getUnderlying();
			if (de instanceof Container) {
				Container con = (Container) de;
				Direction d = segment.getUnderlyingSide();

				if (d != null) {
					Pair<List<Slideable>> mapItem = containerMap.get(con);
					if (mapItem == null) {
						mapItem = new Pair<List<Slideable>>(new ArrayList<Slideable>(), new ArrayList<Slideable>());
						containerMap.put(con, mapItem);
					}

					switch (d) {
					case LEFT:
					case UP:
						mapItem.getA().add(s);
						break;
					case RIGHT:
					case DOWN:
						mapItem.getB().add(s);
						break;
					}
				}
			}
		}

		// ok, now work through the items removing slack
		for (Pair<List<Slideable>> p : containerMap.values()) {
			for (Slideable from : p.getA()) {
				for (Slideable to : p.getB()) {
					maximizeDistance(xo, from, to);
				}
			}
		}

	}
	
	private void maximizeDistance(SegmentSlackOptimisation xo, Slideable from, Slideable to) {
		int slackAvailable = to.getMaximumPosition() - from.getMinimumPosition();
		from.decreaseMaximum(from.getMinimumPosition());
		xo.ensureMinimumDistance(from, to, slackAvailable, true);
	}
	

	public String getPrefix() {
		return "MCSO";
	}

	public boolean isLoggingEnabled() {
		return true;
	}


}
