<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">
 
 <xsl:import href="links-basic-template.xsl"/>
 <xsl:import href="links-align-template.xsl"/>
 <xsl:import href="links-terminators.xsl"/>
 <xsl:import href="links-markers.xsl"/>
 <xsl:import href="../formats/formats-components.xsl" />
  
  <xsl:template name="links-hub">
    <xsl:call-template name="formats-shape" />
  </xsl:template> 
  
  <xsl:template match="adl:hub">
    <xsl:call-template name="links-hub" />
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
    <adl:css>@import url('/public/templates/links/links-elements.css');</adl:css>
    <xsl:next-match />
  </xsl:template>
  
</xsl:stylesheet>