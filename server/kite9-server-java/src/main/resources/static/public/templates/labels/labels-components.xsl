<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        version="1.0">
  
  <xsl:template name="labels-basic">
    <xsl:param name="k9-rounding" >5pt</xsl:param>
    <xsl:param name="text"><text><xsl:value-of select="text()" /></text></xsl:param>
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-texture">none</xsl:with-param>
      <xsl:with-param name="k9-highlight"></xsl:with-param>
      <xsl:with-param name="k9-elem">label</xsl:with-param>
      
      <xsl:with-param name="id" />
      <xsl:with-param name="k9-ui">delete label</xsl:with-param>
      
      <xsl:with-param name="content">
        <xsl:call-template name="formats-text-shape-inline">
          <xsl:with-param name="k9-elem">label-inner</xsl:with-param>
          <xsl:with-param name="k9-rounding" select="$k9-rounding" />
          <xsl:with-param name="k9-texture">none</xsl:with-param>
          <xsl:with-param name="text" select="$text" />
        </xsl:call-template>    
      </xsl:with-param>
    </xsl:call-template>
    
  </xsl:template> 
  
  <xsl:template match="adl:label">
    <xsl:call-template name="labels-basic" />
  </xsl:template>
  
  <xsl:template name="labels-diagram-element-css">
    <adl:css>@import url('/public/templates/labels/labels-elements.css');</adl:css>
  </xsl:template>
    
  <xsl:template name="labels-diagram-palettes">
     <adl:palette contains="connected cell table" url="/public/templates/grid/palette.adl" />
  </xsl:template>  
  
</xsl:stylesheet>
