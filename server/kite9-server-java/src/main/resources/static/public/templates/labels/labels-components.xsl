<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        version="1.0">
  
  <xsl:template name="labels-basic">
    <xsl:param name="k9-rounding" >5pt</xsl:param>
    <xsl:param name="text"><text><xsl:value-of select="text()" /></text></xsl:param>
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-texture">none</xsl:with-param>
      <xsl:with-param name="k9-elem">label</xsl:with-param>
      <xsl:with-param name="k9-palette">label</xsl:with-param>
      <xsl:with-param name="k9-ui">delete edit xml place fill stroke size font</xsl:with-param>
    </xsl:call-template>
    
  </xsl:template> 
  
  <xsl:template match="adl:label">
    <xsl:call-template name="labels-basic" />
  </xsl:template>
  
  <xsl:template name="labels-diagram-element-css">
    <adl:css>@import url('/public/templates/labels/labels-elements.css');</adl:css>
  </xsl:template>
    
  <xsl:template name="labels-diagram-constants">
     <adl:constant name="label-template-uri" url="/public/templates/labels/palette.adl#label" />
  </xsl:template>
    
  <xsl:template name="labels-diagram-palettes">
     <adl:palette contains="label" url="/public/templates/labels/palette.adl" />
  </xsl:template>  
  
</xsl:stylesheet>
