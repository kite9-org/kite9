package com.kite9.pipeline.adl.format.media

/**
 * Indicates that the user has edit control of this format (assuming correct auth).
 */
interface EditableDiagramFormat<T>:
    com.kite9.pipeline.adl.format.media.DiagramWriteFormat<T>