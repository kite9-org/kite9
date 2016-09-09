package org.kite9.diagram.visualization.planarization.rhd.links;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Set;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public class RankBasedConnectionQueue extends OriginalConnectionQueue {

	public RankBasedConnectionQueue(RoutableHandler2D rh) {
		
		Comparator<BiDirectional<Connected>> comp = new Comparator<BiDirectional<Connected>>() {
			
			@Override
			public int compare(BiDirectional<Connected> arg0,
					BiDirectional<Connected> arg1) {
				int r0 = getRankFor(arg0);
				int r1 = getRankFor(arg1);
				return ((Integer)r1).compareTo(r0);
			}

			private int getRankFor(BiDirectional<Connected> arg0) {
				if (arg0 instanceof Connection) {
					return ((Connection)arg0).getRank();
				} else {
					return 0;
				}
			}
		};
		
		this.x = new PriorityQueue<BiDirectional<Connected>>(1000, comp);
		this.y = new PriorityQueue<BiDirectional<Connected>>(1000, comp);
		this.u = new LinkedHashSet<BiDirectional<Connected>>();
		
		
	}

	private Set<BiDirectional<Connected>> alreadyAdded = new UnorderedSet<BiDirectional<Connected>>(1000);

	@Override
	protected boolean considerThis(BiDirectional<Connected> c, CompoundGroup cg) {
		if (alreadyAdded.contains(c)) {
			return false;
		}
		
		if (super.considerThis(c, cg)) {
			alreadyAdded.add(c);
			return true;
		} else {
			return false;
		}
	}
	
	
}
