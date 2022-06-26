<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">
  
  <!-- this decorates the formats-template for a container, so that the container has a 
       layout direction indicator
  -->  

  <xsl:template name="containers-indicator" match="*[@layout and @id]" mode="container-decoration" priority="1">
    <xsl:param name="layout" select="@layout" />
    <g k9-highlight="stroke">
      <rect y="0" x="0" pp:x="$width - 25" pp:y="$height - 25" width="20" height="20" rx="4" ry="4" fill="none"/>
      <xsl:choose>
        <xsl:when test="$layout = 'right'">
          <path d="" pp:d="concat('M', $width - 20,' ',$height - 15,' l10 0')" marker-end='url(#indicator-arrow)'/>
        </xsl:when>
        <xsl:when test="$layout = 'left'">
          <path d="" pp:d="concat('M ', $width - 10,' ', $height - 15,' l-10 0')"
            marker-end='url(#indicator-arrow)'/>
        </xsl:when>
        <xsl:when test="$layout = 'up'">
          <path d="" pp:d="concat('M ',$width - 15,' ', $height - 10,'l0 -10')" marker-end='url(#indicator-arrow)'/>
        </xsl:when>
        <xsl:when test="$layout = 'down'">
          <path d="" pp:d="concat('M ', $width - 15,' ',$height - 20,' l0 10')" marker-end='url(#indicator-arrow)'/>
        </xsl:when>
        <xsl:when test="$layout = 'horizontal'">
          <path d="" pp:d="concat('M ', $width - 20,' ',$height - 15, ' l10 0')"/>
        </xsl:when>
        <xsl:when test="$layout = 'vertical'">
          <path d="" pp:d="concat('M ',$width - 15,' ',$height - 20,'l0 10')"/>
        </xsl:when>

        <xsl:otherwise>
        </xsl:otherwise>
      </xsl:choose>
    </g>

  </xsl:template>
   
      
  <xsl:template match="adl:container">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">10pt</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete align connect insert autoconnect layout</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:group">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">10pt</xsl:with-param>
      <xsl:with-param name="k9-texture">none</xsl:with-param>
      <xsl:with-param name="k9-highlight">pulse stroke</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete align connect insert autoconnect layout</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
   
  <xsl:template name="containers-diagram-element-css">
    <adl:css>@import url('/github/kite9-org/kite9/templates/containers/containers-elements.css');</adl:css>
  </xsl:template>
  
  <xsl:template name="containers-diagram-palettes">
     <adl:palette contains="connected" url="/github/kite9-org/kite9/templates/containers/palette.adl" />
  </xsl:template>
  
   <xsl:template name="containers-diagram-defs">
      <defs id="container-indicators">
        <marker id="indicator-arrow" markerWidth="15" markerHeight="15" refX="3" refY="3" orient="auto"
                    stroke="none" fill="none">
            <path k9-highlight="stroke" d="M0,0 L3,3 L0,6"/>
        </marker>
      </defs>
  </xsl:template>
    
</xsl:stylesheet>
