package org.kite9.diagram.visualization.orthogonalization.flow.balanced

import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.model.Terminator

class TerminatorPair(a: Terminator?, b: Terminator?) : Pair<Terminator?>(a, b) {

    override fun elementEquals(a2: Any, a3: Any): Boolean {
        return (a2 as Terminator?)?.styleMatches((a3 as Terminator?)) ?: true
    }
}