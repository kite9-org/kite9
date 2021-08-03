<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

    <xsl:import href="/public/templates/common/back/back-template.xsl"/>

    <xsl:template name="diagram-root-svg">
        <xsl:param name="css"/>
        <xsl:param name="script"/>
        <xsl:param name="constants"/>
        <xsl:param name="defs"/>
        <svg>
            <xsl:attribute name="pp:width">$width</xsl:attribute>
            <xsl:attribute name="pp:height">$height</xsl:attribute>
            <defs>
                <style type="text/css">
                    <xsl:value-of select="$css"/>
                </style>
                <xsl:copy-of select="$defs"/>
            </defs>

            <xsl:apply-templates/>

            <defs>
                <script>
                    <xsl:value-of select="$constants"/>
                </script>
                <script type="module">
                    <xsl:value-of select="$script"/>
                </script>
            </defs>
        </svg>
    </xsl:template>

    <xsl:template name="diagram-basic">
        <xsl:param name="content"><xsl:apply-templates /></xsl:param>
        <g>
            <xsl:attribute name="k9-elem">diagram</xsl:attribute>
            <xsl:attribute name="k9-texture">background</xsl:attribute>
            <xsl:attribute name="k9-ui">layout label</xsl:attribute>
            <xsl:attribute name="k9-shape">rect</xsl:attribute>
            <xsl:attribute name="k9-rounding">0pt</xsl:attribute>
            <xsl:attribute name="k9-contains">connected</xsl:attribute>
            <xsl:attribute name="pp:width">$width</xsl:attribute>
            <xsl:attribute name="pp:height">$height</xsl:attribute>
            <xsl:copy-of select="@*" />

            <xsl:call-template name="back-round-rect">
                <xsl:with-param name="rounding">0pt</xsl:with-param>
            </xsl:call-template>
            <!--indicator -->
            <xsl:copy-of select="$content" />
        </g>
    </xsl:template>


</xsl:stylesheet>