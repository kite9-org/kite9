package org.kite9.diagram.visualization.planarization.rhd.layout;

import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public class DirectionLayoutStrategy extends AbstractTopDownLayoutStrategy {

	public DirectionLayoutStrategy(RoutableHandler2D rh) {
		super(rh);
	}

	@Override
	public PlacementApproach createPlacementApproach(GroupPhase gp, GroupPhase.CompoundGroup gg, Layout ld,
			boolean setHoriz, boolean setVert, boolean natural) {
		return new DirectionPlacementApproach(log, gp, ld, gg, rh, setHoriz, setVert, natural);
	}

}
