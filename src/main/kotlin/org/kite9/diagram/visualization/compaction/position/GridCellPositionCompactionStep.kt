package org.kite9.diagram.visualization.compaction.position

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.position.RenderingInformation
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.CompactionStep
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * Sets the details of the cell positions in the parent container (Layout=GRID).
 */
class GridCellPositionCompactionStep : CompactionStep {

    override fun compact(c: Compaction, r: Embedding, cr: Compactor) {
        if (r.isTopEmbedding) {
            val gridContainers = createTopElementSet(c.getOrthogonalization())
            for (de in gridContainers) {
                val rri = de.getRenderingInformation()
                setPostions(rri, de.getContents())
            }
        }
    }

    private fun setPostions(pri: RectangleRenderingInformation, contents: List<DiagramElement>) {
        if (contents == null) {
            pri.cellXPositions = doubleArrayOf()
            pri.cellYPositions = doubleArrayOf()
        } else {
            val parent = pri.position
            val xs: MutableSet<Double> = HashSet()
            val ys: MutableSet<Double> = HashSet()
            contents.stream()
                .filter { c: DiagramElement? -> c is Connected }
                .map { c: DiagramElement -> c.getRenderingInformation() }.forEach { rri: RenderingInformation ->
                    val p = rri.position
                    val s = rri.size
                    xs.add(p!!.w - parent!!.w)
                    xs.add(p.w + s!!.w - parent.w)
                    ys.add(p.h - parent.h)
                    ys.add(p.h + s.h - parent.h)
                }
            val xPositions = xs.stream().mapToDouble { d: Double -> d }.sorted().toArray()
            val yPositions = ys.stream().mapToDouble { d: Double -> d }.sorted().toArray()
            pri.cellXPositions = xPositions
            pri.cellYPositions = yPositions
        }
    }

    private fun createTopElementSet(c: Orthogonalization): Set<Container> {
        val out: MutableSet<Container> = LinkedHashSet()
        for (e in c.getAllDarts()) {
            for (de in e.getDiagramElements().keys) {
                if (de is Container) {
                    if (de.getLayout() === Layout.GRID) {
                        out.add(de)
                    }
                }
            }
        }
        return out
    }
}