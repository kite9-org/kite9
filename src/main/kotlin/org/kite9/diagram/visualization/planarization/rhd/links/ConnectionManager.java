package org.kite9.diagram.visualization.planarization.rhd.links;

import java.util.Collection;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;

public interface ConnectionManager extends Collection<BiDirectional<Connected>> {

	public abstract void handleLinks(Group g);

	public boolean hasContradictions();
}