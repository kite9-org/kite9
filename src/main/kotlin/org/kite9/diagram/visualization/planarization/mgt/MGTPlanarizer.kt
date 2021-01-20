package org.kite9.diagram.visualization.planarization.mgt


import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.planarization.AbstractPlanarizer
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.PlanarizationBuilder
import org.kite9.diagram.visualization.planarization.mgt.builder.HierarchicalPlanarizationBuilder
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructor
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructorImpl
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class MGTPlanarizer(elementMapper: ElementMapper) : AbstractPlanarizer(elementMapper) {

    override val faceConstructor: FaceConstructor = FaceConstructorImpl()

    protected val routableHandler: RoutableHandler2D = PositionRoutableHandler2D()
    override val planarizationBuilder: PlanarizationBuilder = HierarchicalPlanarizationBuilder(elementMapper, gridPositioner)

    override fun buildPlanarization(c: Diagram): Planarization {
        val pln = super.buildPlanarization(c)
        faceConstructor.createFaces((pln as MGTPlanarization))
        return pln
    }
}