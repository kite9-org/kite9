package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.position.Direction.Companion.isVertical
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace

class EmbeddingImpl(private val number: Int, override val dartFaces: List<DartFace>) : Embedding {

    override var innerEmbeddings: List<Embedding> = emptyList()
    override var isTopEmbedding = false
    override fun toString(): String {
        return "EmbeddingImpl [number=" + number + ", innerEmbeddings=" + (if (innerEmbeddings == null) 0 else innerEmbeddings!!.size) + ", faces=" + (if (dartFaces == null) 0 else dartFaces.size) + "]"
    }

    override fun getVerticalSegments(c: Compaction): Set<ElementSlideable> {
        return facesToSegments(c) { d: Dart -> isVerticalDart(d) }
    }

    override fun getHorizontalSegments(c: Compaction): Set<ElementSlideable> {
        return facesToSegments(c) { d: Dart -> isHorizontalDart(d) }
    }

    private fun facesToSegments(c: Compaction, p: (Dart) -> Boolean): Set<ElementSlideable> {
        return dartFaces
            .flatMap { it.dartsInFace }
            .map { it.dart }
            .filter { p.invoke( it ) }
            .map { c.getSegmentForDart(it) }
            .toSet()
    }

    companion object {
        fun isVerticalDart(d: Dart): Boolean {
            return isVertical(d.getDrawDirection())
        }

        fun isHorizontalDart(d: Dart): Boolean {
            return isHorizontal(d.getDrawDirection())
        }
    }

}