package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.planarization.mgt.ContainerConnectionTransform1
import org.kite9.diagram.visualization.planarization.mgt.ContainerConnectionTransform2
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructor
import org.kite9.diagram.visualization.planarization.transform.ExcessVertexRemovalTransform
import org.kite9.diagram.visualization.planarization.transform.LayoutSimplificationTransform
import org.kite9.diagram.visualization.planarization.transform.OuterFaceIdentificationTransform
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform

/**
 * This class defines the basic process of planarization, which is then extended
 * by the sub-packages.
 *
 * @author moffatr
 */
abstract class AbstractPlanarizer(val elementMapper: ElementMapper) : Logable, Planarizer {

    protected val log = Kite9Log(this)

    val gridPositioner: GridPositioner
        get() = elementMapper.getGridPositioner()

    override fun planarize(c: Diagram): Planarization {
        val pln = buildPlanarization(c)
        return try {
            for (pt in planarizationTransforms) {
                log.send("PLan:" + pln.toString())
                checkIntegrity(pln)
                log.send("Applying transform: " + pt.javaClass)
                pt.transform(pln)
            }
            checkIntegrity(pln)
            log.send(
                if (log.go()) null else """
                 Completed Planarization: 
                 ${pln.toString()}
                 """.trimIndent())
            pln
        } catch (e: Exception) {
            throw PlanarizationException("Planarization incomplete", pln, e)
        }
    }

    protected open fun buildPlanarization(c: Diagram): Planarization {
        val po = planarizationBuilder
        return po.planarize(c)
    }

    private fun checkIntegrity(pln: Planarization) {
        for ((key, value) in pln.edgeFaceMap) {
            for (f in value) {
                if (!f!!.contains(key)) {
                    throw LogicException("Face doesn't contain edge that map says it does: $key $f")
                }
            }
        }
        for ((key, value) in pln.vertexFaceMap) {
            for (f in value) {
                if (!f.contains(key)) {
                    throw LogicException("Face doesn't contain vertex that map says it does: $key $f")
                }
            }
        }
        for (f in pln.faces) {
            for (e in f.edgeIterator()) {
                if (!pln.edgeFaceMap[e]!!.contains(f)) {
                    throw LogicException("Face contains edge, map says it doesn't: $e $f")
                }
            }
            for (v in f.cornerIterator()) {
                val faces = pln.vertexFaceMap[v]
                if (!faces!!.contains(f)) {
                    throw LogicException("Face contains vertex, map says it doesn't: $v  $f")
                }
            }
        }
    }

    protected val planarizationTransforms: List<PlanarizationTransform>
        protected get() {
            val out: MutableList<PlanarizationTransform> = ArrayList()
            out.add(ExcessVertexRemovalTransform())
            out.add(ContainerConnectionTransform1(elementMapper))
            out.add(LayoutSimplificationTransform())
            out.add(ContainerConnectionTransform2(elementMapper))
            out.add(OuterFaceIdentificationTransform())
            return out
        }
    protected abstract val planarizationBuilder: PlanarizationBuilder
    protected abstract val faceConstructor: FaceConstructor?
    override val prefix: String
        get() = "PLAN"
    override val isLoggingEnabled: Boolean
        get() = true
}