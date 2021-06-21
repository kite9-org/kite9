package org.kite9.diagram.dom.ns

/**
 * This tracks the different namespaces used in Kite9
 */
object Kite9Namespaces {

    /**
     * This is used for metadata.  it is generally sent within the SVG document in the meta section,
     * contains details of authors, editors, url, role of user etc.  Not rendered.
     */
    const val META_NAMESPACE = "http://www.kite9.org/schema/metadata"

    /**
     * ADL Is "abstract diagram language".  It is a set of XML objects that can be used to construct diagrams.
     * e.g. <link></link>  or <diagram></diagram>.   Generally, adl is very permissive and you can use your own elements
     * all over the place.
     */
    const val ADL_NAMESPACE = "http://www.kite9.org/schema/adl"

    /**
     * Namespace used everywhere for SVG.
     */
    const val SVG_NAMESPACE = "http://www.w3.org/2000/svg"

    /**
     * This is prefixed to attributes when they contain an xpath.  The attribute's xpath is
     * post-processed after diagram rendering and replaces the original svg attribute namespace.
     *
     * e.g. pp:width="$width - 10" -> width="45"
     */
    const val POSTPROCESSOR_NAMESPACE = "http://www.kite9.org/schema/post-processor"

    /**
     * This is used to prefix the template attribute, e.g. xslt:template="bob.xsl".  It is used on the
     * document element of some XML to indicate the xslt transformer to be used.
     */
    const val XSLT_NAMESPACE = "http://www.kite9.org/schema/xslt"
}