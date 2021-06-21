<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">


    <!-- this rule matches and passes through any svg elements -->
    <xsl:template match="svg:*">
        <xsl:element name="{local-name()}">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates select="*" />
        </xsl:element>
    </xsl:template>

    <!-- this rule is the default for adl elements -->
    <xsl:template match="adl:*">
        <g>
            <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
            <xsl:apply-templates />
        </g>
    </xsl:template>

</xsl:stylesheet>