<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

    <xsl:import href="/public/templates/common/back/back-template.xsl" />

    <xsl:variable name="container-defs">
        <svg:marker id="indicator-arrow" markerWidth="15" markerHeight="15" refX="3" refY="3" orient="auto"
                    stroke="none" fill="none">
            <svg:path k9-indicator="stroke" d="M0,0 L3,3 L0,6"/>
        </svg:marker>
    </xsl:variable>

    <xsl:template name="container">
        <xsl:param name="rounding">15pt</xsl:param>
        <g k9-texture="outline"
           k9-palette="connected"
           k9-contains="connected"
           k9-ui="drag delete align connect layout label">
            <xsl:copy-of select="@*" />
            <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
            <xsl:call-template name="back-round-rect">
                <xsl:with-param name="k9-indicator">pulse</xsl:with-param>
                <xsl:with-param name="rounding"><xsl:value-of select="$rounding"/></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="indicator"/>  -->
            <xsl:apply-templates />
        </g>
    </xsl:template>

    <xsl:template name="indicator">
        <xsl:param name="layout"></xsl:param>
            <g k9-indicator="stroke">
                   <xsl:choose>
                       <xsl:when test="$layout = 'right'">
                           <svg:rect y="0" x="0" pp:x="$width - 25" pp:y="$height - 25" width="20" height="20" rx="4" ry="4"
                                     fill="none"/>
                           <svg:path d="" pp:d="'M' + ($width - 20) ($height - 15) l10 0" marker-end='url(#indicator-arrow)'/>
                       </xsl:when>
                       <xsl:when test="$layout = 'left'">
                           ... some output ...
                       </xsl:when>
                       <xsl:otherwise>
                       </xsl:otherwise>
                   </xsl:choose>
               </g>

    </xsl:template>
</xsl:stylesheet>
