package com.kite9.pipeline.adl.format.media

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput
import org.kite9.diagram.format.Kite9Transcoder

/**
 * Marks the Format as being one which can express a rendered diagram.
 *
 * @author robmoffat
 */
interface DiagramWriteFormat : Format {

    fun handleWrite(toWrite: ADLDom, t: Kite9Transcoder) : ADLOutput

}