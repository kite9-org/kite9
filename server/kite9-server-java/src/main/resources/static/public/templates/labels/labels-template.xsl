<xsl:stylesheet
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns="http://www.kite9.org/schema/adl"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:k9="http://www.kite9.org/schema/macros"
        version="1.0">

  <xsl:import href="../formats/formats-template.xsl"/>  
  
  <xsl:template name="labels-basic">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-ui">layout label</xsl:with-param>
      <xsl:with-param name="k9-rounding">0</xsl:with-param>
      <xsl:with-param name="k9-texture">background</xsl:with-param>
    </xsl:call-template>
  </xsl:template> 
  
  
</xsl:stylesheet>
