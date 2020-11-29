package org.kite9.diagram.visualization.planarization.rhd;

import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager;

public interface GroupBuilder {

	public GroupAxis createAxis();

	public LinkManager createLinkManager();
	
}
