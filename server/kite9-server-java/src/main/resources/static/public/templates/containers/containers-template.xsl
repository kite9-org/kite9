<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

    <xsl:import href="/public/templates/back/back-template.xsl" />

    <xsl:variable name="container-indicators">
        <marker id="indicator-arrow" markerWidth="15" markerHeight="15" refX="3" refY="3" orient="auto"
                    stroke="none" fill="none">
            <path k9-highlight="stroke" d="M0,0 L3,3 L0,6"/>
        </marker>
    </xsl:variable>

  <!-- Container template where the container contents is in a specific order -->
  <xsl:template name='containers-basic'>
    <xsl:param name="content"><xsl:apply-templates /></xsl:param>

    <xsl:param name="k9-format">container</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="k9-contains">connected</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect insert autoconnect</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>

    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-layout" select="@layout" />
    
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
      <xsl:attribute name="k9-contains"><xsl:value-of select="$k9-contains" /></xsl:attribute>
      
      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class" /></xsl:attribute>
      </xsl:if>
            
      <xsl:copy-of select="$attributes" />      
        
      <xsl:call-template name="back-round-rect">
        <xsl:with-param name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:with-param>
        <xsl:with-param name="k9-rounding"><xsl:value-of select="$k9-rounding" /></xsl:with-param>
      </xsl:call-template>
       
      <xsl:copy-of select="$content"/>
      
      
      <xsl:if test="$k9-layout">
        <xsl:call-template name="containers-indicator">
          <xsl:with-param name="layout"><xsl:value-of select="@layout" /></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
     </g>
  </xsl:template>

    <xsl:template name="containers-indicator">
        <xsl:param name="layout"></xsl:param>
            <g k9-highlight="stroke">
              <rect y="0" x="0" pp:x="$width - 25" pp:y="$height - 25" width="20" height="20" rx="4" ry="4" fill="none"/>
                   <xsl:choose>
                       <xsl:when test="$layout = 'right'">
                           <path d="" pp:d="concat('M', $width - 20,' ',$height - 15,' l10 0')" marker-end='url(#indicator-arrow)'/>
                       </xsl:when>
                       <xsl:when test="$layout = 'left'">
                           <path d="" pp:d="concat('M ', $width - 10,' ', $height - 15,' l-10 0')" marker-end='url(#indicator-arrow)'/>
                       </xsl:when>
                       <xsl:when test="$layout = 'up'">
                           <path d="" pp:d="concat('M ',$width - 15,' ', $height - 10,'l0 -10')" marker-end='url(#indicator-arrow)'/>
                       </xsl:when>
                       <xsl:when test="$layout = 'down'">
                           <path d="" pp:d="concat('M ', $width - 15,' ',$height - 20,' l0 10')" marker-end='url(#indicator-arrow)'/>
                       </xsl:when>
                       <xsl:when test="$layout = 'horizontal'">
                        <path d="" pp:d="concat('M ', $width - 20,' ',$height - 15, ' l10 0')" />            
                       </xsl:when>
                       <xsl:when test="$layout = 'vertical'">
                           <path d="" pp:d="concat('M ',$width - 15,' ',$height - 20,'l0 10')" />
                       </xsl:when>
                       
                       <xsl:otherwise>
                       </xsl:otherwise>
                   </xsl:choose>
               </g>

    </xsl:template>
   

    
</xsl:stylesheet>
