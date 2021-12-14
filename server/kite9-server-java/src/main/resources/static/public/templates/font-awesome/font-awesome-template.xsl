<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  <xsl:import href="../adl/adl-template.xsl"/>
  <xsl:import href="font-awesome-components.xsl" />

  <xsl:template match="/" mode="diagram-texture-css">
    <adl:css>@import url('/public/templates/font-awesome/font-awesome.css');</adl:css>
  </xsl:template>

</xsl:stylesheet>