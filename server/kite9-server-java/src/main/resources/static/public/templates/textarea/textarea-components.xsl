<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="2.0">
    
  <xsl:import href="../formats/formats-components.xsl" />
  
  <xsl:template match="adl:textarea">
    <xsl:call-template name="formats-text-fixed">
      <xsl:with-param name="k9-texture">foreground</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
    <adl:css>@import url('/public/templates/textarea/textarea-elements.css');</adl:css>
    <xsl:next-match />
  </xsl:template>
  

</xsl:stylesheet>