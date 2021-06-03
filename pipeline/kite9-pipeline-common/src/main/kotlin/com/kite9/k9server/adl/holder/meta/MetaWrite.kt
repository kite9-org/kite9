package com.kite9.k9server.adl.holder.meta

import com.kite9.k9server.uri.URI

interface MetaWrite {

    fun setUser(a: UserMeta)
    fun setUser()
    fun setAuthor(a: UserMeta)
    fun setTopicUri(topic: URI)
    fun setCloseUri(close: URI)
    fun setTitle(title: String)
    fun setUri(u: URI)
    fun setCollaborators(collaborators: List<UserMeta>)
    fun setNotification(message: String)
    fun setError(message: String)
    fun setCommitCount(c: Int)
    fun setRole(r: Role)
    fun setUploadsPath(u: String)

    companion object {
        const val CONTENT_CHANGED = "content-changed"
    }
}