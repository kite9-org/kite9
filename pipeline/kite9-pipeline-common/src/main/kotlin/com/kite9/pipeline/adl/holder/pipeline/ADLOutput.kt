package com.kite9.pipeline.adl.holder.pipeline

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat
import com.kite9.pipeline.adl.holder.meta.MetaRead
import org.w3c.dom.Document

/**
 * Third point in the pipeline: ADL is now converted to it's output format.
 *
 * @author robmoffat
 */
interface ADLOutput : XMLBase, MetaRead {

    /**
     * Returns the format used to create this output.
     */
    val format: DiagramWriteFormat

    val asBytes: ByteArray?

    /**
     * Returns the output as a document.  This only works if the output format is a document (i.e. not PDF, PNG etc).
     * And we only ever do this on the client side also.
     */
    val asDocument: Document?

    /*
     * Creator of this.
     */
    val originatingADLDom: ADLDom

}