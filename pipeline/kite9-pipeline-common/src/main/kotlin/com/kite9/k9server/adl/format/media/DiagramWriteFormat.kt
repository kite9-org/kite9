package com.kite9.k9server.adl.format.media

import com.kite9.k9server.adl.holder.pipeline.ADLDom
import org.kite9.diagram.format.Kite9Transcoder

/**
 * Marks the Format as being one which can express a rendered diagram.
 *
 * @author robmoffat
 */
interface DiagramWriteFormat<T> : Format {

    fun handleWrite(toWrite: ADLDom, t: Kite9Transcoder<T>) : T

}