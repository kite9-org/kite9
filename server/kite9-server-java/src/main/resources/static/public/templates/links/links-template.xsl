<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
 
 

  <!-- Container template where the container contents is in a specific order -->
  <xsl:template name='links-basic'>
    <xsl:param name="k9-format">links-basic</xsl:param>
    <xsl:param name="k9-palette">link</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">delete link cascade drop</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-indicator">pulse</xsl:param>
    <xsl:param name="k9-containers">*</xsl:param>
    <xsl:param name="k9-contains">end label</xsl:param>
    
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
      <xsl:attribute name="k9-containers"><xsl:value-of select="$k9-containers" /></xsl:attribute>
      <xsl:attribute name="k9-contains"><xsl:value-of select="$k9-contains" /></xsl:attribute>
      <xsl:copy-of select="@*" />
      
      <g k9-elem="link-grab" k9-indicator="bar stroke">
        <path pp:d="$path" d="" k9-animate="link"/>
      </g>
      
      <xsl:apply-templates />
      
      <xsl:variable name="toShape"><xsl:value-of select="adl:to/@shape" /></xsl:variable>
      <xsl:variable name="fromShape"></xsl:variable>
      
      <g k9-elem="link-body">
       <path k9-animate="link" d="" pp:d="$path">
         <xsl:attribute name="marker-start">url(#<xsl:value-of select="adl:from/@shape" />-start-marker)</xsl:attribute>
         <xsl:attribute name="marker-end">url(#<xsl:value-of select="adl:to/@shape" />-end-marker)</xsl:attribute>
       </path>     
      </g>
      
    </g>
  </xsl:template>
   
   
  <xsl:template name="align">
    <xsl:param name="k9-ui">delete cascade</xsl:param>
    
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:copy-of select="@*" />
      
      <g k9-elem="align-grab" k9-indicator="bar stroke">
        <path k9-animate="link" pp:d="$path" d="" />
      </g>
      
      <xsl:apply-templates />
      
      <g k9-elem="align-body" k9-indicator="stroke">
        <path k9-animate="link" pp:d="$path" d="" />
      </g>
    </g>
  </xsl:template>
  
  <xsl:template name="hub">
    <g k9-elem="hub">
      <circle r="16" cx="5" cy="5" />
    </g>
  </xsl:template> 
  
</xsl:stylesheet>