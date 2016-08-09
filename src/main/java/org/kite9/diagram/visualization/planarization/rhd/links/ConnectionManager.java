package org.kite9.diagram.visualization.planarization.rhd.links;

import java.util.Collection;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;

public interface ConnectionManager extends Collection<BiDirectional<Connected>> {

	public abstract void handleLinks(CompoundGroup cg);

	public boolean hasContradictions();
}