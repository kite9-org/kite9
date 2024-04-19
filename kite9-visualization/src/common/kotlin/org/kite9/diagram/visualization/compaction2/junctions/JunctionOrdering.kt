package org.kite9.diagram.visualization.compaction2.junctions

interface Entry<X> {

    fun getItems() : Set<X>
}

data class SingleItemEntry<X>(val item: X) : Entry<X> {
    override fun getItems(): Set<X> {
        return setOf(item)
    }

    override fun toString(): String {
        return "(${item})"
    }
}

data class RunPairEntry<X>(val p1: List<X>, val p2: List<X>) : Entry<X> {
    override fun getItems(): Set<X> {
        return p1.toSet().plus(p2.toSet())
    }

    override fun toString(): String {
        return "[(${p1.joinToString(", ")}, ${p2.joinToString(", ")}]"
    }
}

data class JunctionOrdering<X>(val entries: List<Entry<X>>) {

    fun after(e: X) : Set<X> {
        return emptySet()
    }

    fun before(e: X): Set<X> {
        return emptySet()
    }

    fun merge(e1: X, e2: X) : JunctionOrdering<X> {
        return this
    }

    override fun toString(): String {
        return entries.joinToString(prefix = "<", postfix = ">", separator = "")
    }
}