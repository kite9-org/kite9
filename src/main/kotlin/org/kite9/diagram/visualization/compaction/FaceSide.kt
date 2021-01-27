package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.visualization.compaction.segment.Segment

data class FaceSide(val main: Slideable<Segment>?, val all: Set<Slideable<Segment>>) {


}