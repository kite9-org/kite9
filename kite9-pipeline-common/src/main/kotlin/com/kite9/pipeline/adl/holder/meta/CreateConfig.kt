package com.kite9.pipeline.adl.holder.meta

/**
 * This contains properties that say where diagrams are to be created and the format they're in.
 */
interface CreateConfig {

    fun setUploadsPath(path: String)

    companion object {
        val PROPERTIES = setOf("uploadsPath", "templatePath", "templates", "defaultFormat", "allowedFormats" );
    }
}