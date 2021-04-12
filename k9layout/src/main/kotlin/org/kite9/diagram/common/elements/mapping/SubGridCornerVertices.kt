package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Port
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * Adds vertices to the parent container vertices, and operates in a narrower range.
 *
 * This is used for containers embedded in a grid layout, which add their container vertices to the parent container.
 *
 * @author robmoffat
 */
class SubGridCornerVertices(
    c: DiagramElement,
    x: OPair<LongFraction>,
    y: OPair<LongFraction>,
    parentCV: BaseGridCornerVertices,
    depth: Int
) : AbstractCornerVertices(parentCV.getGridContainer(), getXSpan(x, parentCV), getYSpan(y, parentCV), depth) {

    var baseGrid: BaseGridCornerVertices
    private val elements: MutableMap<OPair<LongFraction>, MultiCornerVertex>

    override fun mergeDuplicates(cv: MultiCornerVertex, rh: RoutableHandler2D): MultiCornerVertex? {
        return if (elements.values.contains(cv)) {
            val out = getTopContainerVertices().findOverlappingVertex(cv, rh)
            if (out != null) {
                // merge the anchors
                for (a in cv.getAnchors()) {
                    out.addAnchor(a.lr, a.ud, a.de)
                }

                // replace the element in the map
                elements[OPair(cv.xOrdinal, cv.yOrdinal)] = out
                return null
            }
            cv
        } else {
            baseGrid.mergeDuplicates(cv, rh)
        }
    }

    override fun getTopContainerVertices(): AbstractCornerVertices {
        return (baseGrid as AbstractCornerVertices).getTopContainerVertices()
    }

    override fun createVertex(x: LongFraction, y: LongFraction, p: Port?): MultiCornerVertex {
        var x = x
        var y = y
        x = scale(x, xRange)
        y = scale(y, yRange)
        val out = (baseGrid as AbstractCornerVertices).createVertex(x, y, p)
        elements[OPair(x, y)] = out
        return out
    }

    override fun getAllAscendentVertices(): MutableCollection<MultiCornerVertex> {
        val out: MutableCollection<MultiCornerVertex> = ArrayList()
        out.addAll(baseGrid.getAllAscendentVertices())
        out.addAll(elements.values)
        return out
    }

    override fun getAllDescendentVertices(): MutableCollection<MultiCornerVertex> {
        val out = super.getAllDescendentVertices()
        out.addAll(elements.values)
        return out
    }

    override fun getVerticesAtThisLevel(): Collection<MultiCornerVertex> {
        return elements.values
    }

    fun getGridContainer() : DiagramElement {
        return baseGrid.getGridContainer();
    }

    companion object {
        private fun getYSpan(y: OPair<LongFraction>, parentCV: CornerVertices): OPair<LongFraction> {
            return OPair(
                scale(y.a, (parentCV as AbstractCornerVertices).yRange),
                scale(y.b, parentCV.yRange)
            )
        }

        private fun getXSpan(x: OPair<LongFraction>, parentCV: CornerVertices): OPair<LongFraction> {
            return OPair(
                scale(x.a, (parentCV as AbstractCornerVertices).xRange),
                scale(x.b, parentCV.xRange)
            )
        }
    }

    init {
        (parentCV as AbstractCornerVertices).children.add(this)
        baseGrid = parentCV
        elements = HashMap()
        createInitialVertices(c)
    }
}