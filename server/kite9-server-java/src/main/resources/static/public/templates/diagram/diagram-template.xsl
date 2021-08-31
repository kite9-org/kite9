<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

    <xsl:import href="../formats/formats-template.xsl"/>

    <xsl:template name="diagram-root-svg">
        <xsl:param name="css"></xsl:param>
        <xsl:param name="script"></xsl:param>
        <xsl:param name="constants"></xsl:param>
        <xsl:param name="defs"></xsl:param>
        <xsl:param name="content"><xsl:apply-templates /></xsl:param>
        <svg>
            <xsl:attribute name="pp:width">$width</xsl:attribute>
            <xsl:attribute name="pp:height">$height</xsl:attribute>
            <defs>
                <style type="text/css">
                    <xsl:copy-of select="$css"/>
                </style>
                <xsl:copy-of select="$defs"/>
            </defs>

            <xsl:copy-of select="$content" />

            <defs>
                <script>
                    <xsl:copy-of select="$constants"/>
                </script>
                <script type="module">
                    <xsl:copy-of select="$script"/>
                </script>
            </defs>
        </svg>
    </xsl:template>

    <xsl:template name="diagram-basic">
      <xsl:call-template name="formats-container">
        <xsl:with-param name="k9-texture">background</xsl:with-param>
        <xsl:with-param name="k9-ui">layout label</xsl:with-param>
        <xsl:with-param name="k9-rounding">0</xsl:with-param>
      </xsl:call-template>
    </xsl:template>


</xsl:stylesheet>