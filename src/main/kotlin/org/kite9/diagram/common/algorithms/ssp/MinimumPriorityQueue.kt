package org.kite9.diagram.common.algorithms.ssp

class MinimumPriorityQueue<Key> : Iterable<Key> {

    private var pq: Array<Key?>

    private var n = 0

    constructor(initCapacity: Int) {
        pq = arrayOfNulls<Any?>(initCapacity + 1) as Array<Key?>
    }

    constructor(keys: Array<Key>) : this(keys.size) {
        n = keys.size
        for (i in 0..n - 1) {
            pq[i + 1] = keys[i]
        }
        for (k in n / 2 downTo 1) {
            sink(k)
        }
        assert(isMinHeap())
    }

    fun isEmpty() = n == 0

    fun size() = n

    fun peek(): Key? {
        return pq[1]
    }

    fun insert(x: Key): MinimumPriorityQueue<Key> {
        if (n == pq.size - 1) {
            resize(2 * pq.size)
        }
        pq[++n] = x
        swim(n)
        assert(isMinHeap())
        return this
    }

    fun remove(): Key {
        require(!isEmpty()) { "Cannot retrieve minimum record. Priority queue is empty" }
        val min = pq[1]
        exch(1, n--)
        sink(1)
        pq[n + 1] = null
        assert(isMinHeap())
        return if (min != null) min else throw NullPointerException("'min' must not be null")
    }

    override fun iterator(): Iterator<Key> {
        return HeapIterator(size(), n, pq)
    }

    private fun swim(k: Int) {
        var myK = k
        while (myK > 1 && greater(myK / 2, myK)) {
            exch(myK, myK / 2)
            myK /= 2
        }
    }

    private fun sink(k: Int) {
        var myK = k
        while (2 * myK <= n) {
            var j = 2 * myK
            if (j < n && greater(j, j + 1)) j++
            if (!greater(myK, j)) return
            exch(myK, j)
            myK = j
        }
    }

    private fun greater(i: Int, j: Int): Boolean {
        return (pq[i] as Comparable<Key>) > pq[j]!!
    }

    private fun exch(i: Int, j: Int) {
        pq[i] = pq[j].also { pq[j] = pq[i] }
    }

    private fun isMinHeap(): Boolean = isMinHeap(1)

    private fun isMinHeap(k: Int): Boolean {
        if (k > n) return true
        val left = 2 * k
        val right = 2 * k + 1
        when {
            left <= n && greater(k, left) -> return false
            right <= n && greater(k, right) -> return false
            else -> {
                return isMinHeap(left) && isMinHeap(right)
            }
        }
    }

    private fun resize(capacity: Int) {
        assert(capacity > n)
        val temp = arrayOfNulls<Any>(capacity) as Array<Key?>
        for (i in 1..n) {
            temp[i] = pq[i]
        }
        pq = temp
    }

    class HeapIterator<out Key>(size: Int, n: Int, pq: Array<Key?>) : Iterator<Key> {

        private val copy: MinimumPriorityQueue<Key> = MinimumPriorityQueue<Key>(size)

        override fun hasNext(): Boolean {
            return !copy.isEmpty()
        }

        override fun next(): Key {
            require(hasNext()) {"Queue is empty"}
            return copy.remove()
        }

        init {
            for (i in 1..n)
                copy.insert(pq[i]!!)
        }

    }

}