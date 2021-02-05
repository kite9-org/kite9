package org.kite9.diagram.common

object Collections {

    fun <T> leftShift(l: Array<T>, d: Int): Array<T> {
        val newList = l.copyOf()
        var shift = d
        if (shift > l.size) shift %= l.size
        l.forEachIndexed { index, value ->
            val newIndex = (index + (l.size - shift)) % l.size
            newList[newIndex] = value
        }
        return newList
    }

    /**
     * Stolen from java.util.Collections
     */
    fun <T> rotate(list: MutableList<T>, distance: Int) {
        var distance = distance
        val size = list.size
        if (size == 0) return
        distance = distance % size
        if (distance < 0) distance += size
        if (distance == 0) return
        var cycleStart = 0
        var nMoved = 0
        while (nMoved != size) {
            var displaced = list[cycleStart]
            var i = cycleStart
            do {
                i += distance
                if (i >= size) i -= size
                displaced = list.set(i, displaced)
                nMoved++
            } while (i != cycleStart)
            cycleStart++
        }
    }
}