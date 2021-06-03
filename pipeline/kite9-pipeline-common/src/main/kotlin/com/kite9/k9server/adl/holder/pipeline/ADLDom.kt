package com.kite9.k9server.adl.holder.pipeline

import com.kite9.k9server.adl.format.media.DiagramWriteFormat
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
    fun <X> process(forLocation: URI, format: DiagramWriteFormat<X>): ADLOutput<X>

}