package com.kite9.pipeline.adl.holder.pipeline

import java.net.URI

/**
 * Second point in the pipeline, the DOM can be worked on by commands.
 *
 * @author robmoffat
 */
interface ADLDom : XMLDom {

    /**
     * Does the actual processing: transforms into SVG and renders it.
     */
    fun <X> process(forLocation: URI, format: com.kite9.pipeline.adl.format.media.DiagramWriteFormat<X>): ADLOutput<X>

}