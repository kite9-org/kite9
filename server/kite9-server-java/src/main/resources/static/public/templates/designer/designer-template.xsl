<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="2.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="designer-components.xsl" />


  <xsl:template match="/" mode="diagram-css">
    <adl:css>@import url('/public/templates/designer/designer.css');</adl:css>
  </xsl:template>
  
   <xsl:template match="/" mode="diagram-script">
     import '/public/templates/risk-first/risk-first.js';
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
    <adl:palette contains="connected" url="/public/templates/designer/palette.adl"/>
    <xsl:next-match/>
  </xsl:template>
 
</xsl:stylesheet>