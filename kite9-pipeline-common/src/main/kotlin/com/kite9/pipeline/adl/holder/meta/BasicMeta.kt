package com.kite9.pipeline.adl.holder.meta

import com.kite9.pipeline.uri.K9URI

/**
 * Handles allowable meta elements, that can be stored in the ADL. These get
 * output either as part of the SVG, or headers of the HTTP request.
 *
 * @author robmoffat
 */
open class BasicMeta(metadata: MutableMap<String, Any>, uri: K9URI?) : MetaReadWrite {

    protected var metadata: MutableMap<String, Any> = HashMap()

    override val metaData: Map<String, Any>
        get() = metadata

    override fun setUser(a: UserMeta) {
        metadata["user"] = a
    }

    override fun setAuthor(a: UserMeta) {
        metadata["author"] = a
    }

    override fun setTopicUri(topic: K9URI) {
        metadata["topic"] = topic
    }


    override fun setCloseUri(close: K9URI) {
        metadata["close"] = close
    }

    override fun setTitle(title: String) {
        metadata["title"] = title
    }

    override fun getTopicUri() = metadata["topic"] as K9URI?
    override fun getTitle() = metadata.getOrDefault("title", "Unnamed") as String
    override fun getUri() = metadata["self"] as K9URI?

    override fun setUri(u: K9URI) {
        metadata["self"] = u
    }

    override fun setCollaborators(collaborators: List<UserMeta>) {
        metadata["collaborators"] = collaborators
    }

    override fun setCommitCount(c: Int) {
        metadata["committing"] = c
    }

    override fun setNotification(message: String) {
        metadata["notification"] = message
    }

    override fun setError(message: String) {
        metadata["error"] = message
    }

    override fun setRole(r: Role) {
        metadata["role"] = r.toString().lowercase()
    }

    override fun set(property: String, value: Any) {
       if (CreateConfig.PROPERTIES.contains(property)) {
           this.metadata.put(property, value);
       }
    }

    override fun setUploadsPath(path: String) {
        metadata["uploadsPath"] = path
    }

    init {
        this.metadata = metadata
        if (uri != null) {
            setUri(uri)
        }
    }
}