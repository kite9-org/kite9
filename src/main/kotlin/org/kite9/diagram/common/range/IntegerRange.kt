package org.kite9.diagram.common.range

import java.util.*

interface IntegerRange {
    val from: Int
    val to: Int

    companion object {

		fun notSet(r: IntegerRange?): Boolean {
            return if (r!=null) r.from > r.to else false
        }
    }
}