package com.kite9.pipeline.adl.holder.meta

import com.kite9.pipeline.uri.K9URI


interface MetaWrite {

    fun setUser(a: UserMeta)
    fun setAuthor(a: UserMeta)
    fun setTopicUri(topic: K9URI)
    fun setCloseUri(close: K9URI)
    fun setTitle(title: String)
    fun setUri(u: K9URI)
    fun setCollaborators(collaborators: List<UserMeta>)
    fun setNotification(message: String)
    fun setError(message: String)
    fun setCommitCount(c: Int)
    fun setRole(r: Role)
    fun setUploadsPath(u: String)

    fun setTemplatePath(u: String)

    companion object {
        const val CONTENT_CHANGED = "content-changed"
    }
}