package com.kite9.pipeline.adl.format.media

import com.kite9.k9server.pipeline.adl.holder.pipeline.ADLDom
import org.kite9.diagram.format.Kite9Transcoder

/**
 * Marks the Format as being one which can express a rendered diagram.
 *
 * @author robmoffat
 */
interface DiagramWriteFormat<T> : com.kite9.pipeline.adl.format.media.Format {

    fun handleWrite(toWrite: ADLDom, t: Kite9Transcoder<T>) : T

}