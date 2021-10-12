<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="uml-components.xsl" />
  
  <xsl:template match="/" mode="diagram-script">
     import '/public/templates/uml/uml.js';
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
    @import url('/public/templates/uml/uml.css');
    <xsl:next-match />
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
    <adl:palette contains="connected" url="public/templates/uml/palette.adl"/>
    <xsl:next-match/>
  </xsl:template>
 
 
</xsl:stylesheet>
