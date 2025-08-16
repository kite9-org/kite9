package org.kite9.diagram.model.position

abstract class AbstractRenderingInformationImpl : RenderingInformation {

    override var rendered = true
    override var position: Dimension2D? = null
    override var size: Dimension2D? = null

}