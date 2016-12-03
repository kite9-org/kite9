package org.kite9.diagram.visualization.planarization.rhd.links;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;

/**
 * Contains code for managing/checking for contradictions in links.
 * 
 * @author robmoffat
 *
 */
public interface ContradictionHandler {

	public abstract void checkOrdinalContradiction(Layout l, Direction d,
			Connected from, Connected to, Container fromC, Connection c);

	public abstract void checkForContainerContradiction(Connection c);

	public abstract Direction checkContradiction(Direction ad, boolean aOrdering,
			int aRank, Iterable<BiDirectional<Connected>> ac, Direction bd, boolean bOrdering, int bRank,
			Iterable<BiDirectional<Connected>> bc, Layout containerLayout);

	public abstract Direction checkContradiction(LinkDetail ld1, LinkDetail ld2,
			Layout containerLayout);

	public abstract void setContradiction(BiDirectional<Connected> bic);

	public abstract void setContradicting(Iterable<BiDirectional<Connected>> connections);

}
