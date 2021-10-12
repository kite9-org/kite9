<xsl:stylesheet
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns="http://www.kite9.org/schema/adl"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:k9="http://www.kite9.org/schema/macros"
        version="2.0">

  <xsl:import href="../formats/formats-components.xsl"/>  
  
  <xsl:template name="labels-basic">
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-texture">none</xsl:with-param>
      <xsl:with-param name="k9-highlight"></xsl:with-param>
      
      <xsl:with-param name="id" />
      <xsl:with-param name="k9-ui">delete label</xsl:with-param>
      
      <xsl:with-param name="content">
        <xsl:call-template name="formats-text-shape-inline">
          <xsl:with-param name="k9-elem">label-inner</xsl:with-param>
          <xsl:with-param name="k9-rounding" select="$k9-rounding" />
        </xsl:call-template>    
      </xsl:with-param>
    </xsl:call-template>
    
  </xsl:template> 
  
  <xsl:template match="adl:label">
    <xsl:call-template name="labels-basic" />
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
    @import url('/public/templates/labels/labels-elements.css');
    <xsl:next-match />
  </xsl:template>
  
</xsl:stylesheet>
