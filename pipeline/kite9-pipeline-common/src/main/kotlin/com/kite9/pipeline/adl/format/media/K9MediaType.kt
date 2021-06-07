package com.kite9.pipeline.adl.format.media

interface K9MediaType {
    val type: String
    val subtype: String
    val subtypeSuffix: String?
        get() {
            val suffixIndex = subtype.lastIndexOf('+')
            return if (suffixIndex != -1 && subtype.length > suffixIndex) {
                subtype.substring(suffixIndex + 1)
            } else null
        }
    val parameters: Map<String, String>

    val isWildcardType: Boolean
        get() = WILDCARD_TYPE == type

    val isWildcardSubtype: Boolean
        get() = WILDCARD_TYPE == subtype || subtype.startsWith("*+")

    fun isCompatibleWith(other: K9MediaType?): Boolean {
        if (other == null) {
            return false
        }
        if (isWildcardType || other.isWildcardType) {
            return true
        } else if (type == other.type) {
            if (subtype == other.subtype) {
                return true
            }
            if (isWildcardSubtype || other.isWildcardSubtype) {
                val thisSuffix = subtypeSuffix
                val otherSuffix = other.subtypeSuffix
                if (subtype == WILDCARD_TYPE || other.subtype == WILDCARD_TYPE) {
                    return true
                } else if (isWildcardSubtype && thisSuffix != null) {
                    return thisSuffix == other.subtype || thisSuffix == otherSuffix
                } else if (other.isWildcardSubtype && otherSuffix != null) {
                    return subtype == otherSuffix || otherSuffix == thisSuffix
                }
            }
        }
        return false
    }

    companion object {

        fun parseMediaType(mimeType: String): K9MediaType {
            var index = mimeType.indexOf(';')
            var fullType = (if (index >= 0) mimeType.substring(0, index) else mimeType).trim { it <= ' ' }
            require(!fullType.isEmpty()) { "'mimeType' must not be empty" }

            // java.net.HttpURLConnection returns a *; q=.2 Accept header
            if (WILDCARD_TYPE == fullType) {
                fullType = "*/*"
            }
            val subIndex = fullType.indexOf('/')
            require(subIndex != -1) { "does not contain '/'" }
            require(subIndex != fullType.length - 1) { "does not contain subtype after '/'" }
            val type = fullType.substring(0, subIndex)
            val subtype = fullType.substring(subIndex + 1)
            require(!(WILDCARD_TYPE == type && WILDCARD_TYPE != subtype)) { "wildcard type is legal only in '*/*' (all mime types)" }
            val parameters: MutableMap<String, String> = LinkedHashMap()
            do {
                var nextIndex = index + 1
                var quoted = false
                while (nextIndex < mimeType.length) {
                    val ch = mimeType[nextIndex]
                    if (ch == ';') {
                        if (!quoted) {
                            break
                        }
                    } else if (ch == '"') {
                        quoted = !quoted
                    }
                    nextIndex++
                }
                val parameter = mimeType.substring(index + 1, nextIndex).trim { it <= ' ' }
                if (parameter.length > 0) {
                    val eqIndex = parameter.indexOf('=')
                    if (eqIndex >= 0) {
                        val attribute = parameter.substring(0, eqIndex).trim { it <= ' ' }
                        val value = parameter.substring(eqIndex + 1).trim { it <= ' ' }
                        parameters[attribute] = value
                    }
                }
                index = nextIndex
            } while (index < mimeType.length)

            return object : K9MediaType {
                override val type: String
                    get() = type
                override val subtype: String
                    get() = subtype
                override val parameters: Map<String, String>
                    get() = parameters
            }
        }

        const val WILDCARD_TYPE = "*"
    }
}