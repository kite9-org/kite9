package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager

interface CompoundGroup : Group {

    val a: Group
    val b: Group

    val internalLinkA: LinkManager.LinkDetail?

    val internalLinkB: LinkManager.LinkDetail?
}