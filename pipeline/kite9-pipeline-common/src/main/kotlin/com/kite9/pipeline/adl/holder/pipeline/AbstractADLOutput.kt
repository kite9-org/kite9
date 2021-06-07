package com.kite9.pipeline.adl.holder.pipeline

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat

abstract class AbstractADLOutput(
    override val format: DiagramWriteFormat,
    override val originatingADLDom: ADLDom
) : AbstractXMLBase(
    originatingADLDom.uri,
    originatingADLDom.requestHeaders,
    originatingADLDom.metaData.toMutableMap()), ADLOutput