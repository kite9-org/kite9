<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

    <xsl:import href="../formats/formats-components.xsl" />
    <xsl:import href="../labels/labels-components.xsl" />


    <xsl:template match="adl:glyph">
      <xsl:call-template name="formats-container" />
    </xsl:template>

    <xsl:template match="adl:context">
        <xsl:call-template name="formats-container" />
    </xsl:template>

    <xsl:template match='adl:context/adl:label | adl:cell/adl:label | adl:fromLabel | adl:toLabel'>
        <xsl:call-template name="labels-basic" />
    </xsl:template>

    <xsl:template match="adl:glyph/adl:label | adl:glyph/adl:stereotype" >
        <xsl:call-template name="formats-text-fixed" />
    </xsl:template>
    
    <xsl:template match="adl:arrow" >
        <xsl:call-template name="formats-text-shape-inline" />
    </xsl:template>

    <xsl:template match="adl:glyph/adl:text-lines | adl:key/adl:text-lines">
        <xsl:call-template name="formats-container" />
    </xsl:template>

    <!-- symbol -->
    <xsl:template match="adl:symbol[@shape='CIRCLE']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <circle style="fill: #00A070;  stroke: none" cx="6" cy="6" r="6" />
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:symbol[@shape='HEXAGON']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <polygon points="0,3 6,0 12,3 12,9 6,12 0,9" style="fill: #AA2030;  stroke: none"/>
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:symbol[@shape='DIAMOND']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <polygon points="6,0 12,6 6,12 0,6"  style="fill: #BB8040;  stroke: none" />
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)"  /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:symbol[@shape='SQUARE']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <rect width="12" height="12" x="0" y="0"  style="fill: #CC8050; stroke: none" />
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:key">
        <xsl:call-template name="formats-container" />
    </xsl:template>

    <xsl:template match="adl:key/adl:boldText">
        <xsl:call-template name="formats-text-fixed" />
    </xsl:template>

    <xsl:template match="adl:key/adl:bodyText">
        <xsl:call-template name="formats-text-fixed" />
    </xsl:template>


    <xsl:template match='adl:grid'>
      <xsl:call-template name="grid-table-basic" />
    </xsl:template>

    <xsl:template match='adl:cell'>
       <xsl:call-template name="grid-cell-basic" />
    </xsl:template>

    <xsl:template match="adl:socket">
        <g k9-elem="socket" >
            <xsl:copy-of select="@*" />
            <ellipse cx="0" cy="0" rx="3" ry="3" class="socket-circle" />
        </g>
        <xsl:apply-templates/>
    </xsl:template>

</xsl:stylesheet>