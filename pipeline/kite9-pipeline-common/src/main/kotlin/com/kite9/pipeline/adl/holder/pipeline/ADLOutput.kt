package com.kite9.pipeline.adl.holder.pipeline

import com.kite9.k9server.pipeline.adl.holder.meta.MetaRead

/**
 * Third point in the pipeline: ADL is now converted to it's output format.
 *
 * @author robmoffat
 */
interface ADLOutput<X> : XMLBase, MetaRead {

    /**
     * Returns the format used to create this output.
     */
    val format: com.kite9.pipeline.adl.format.media.DiagramWriteFormat<X>
    val asBytes: ByteArray

    /**
     * Return as UTF-8 string, unless this is a binary format [DiagramFileFormat].isBinaryFormat();
     */
    val asString: String

    /**
     * Returns the produced format itself.
     */
    val value: X

    /**
     * Creator of this.
     */
    val originatingADLDom: ADLDom
}