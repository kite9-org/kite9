<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="designer-components.xsl" />


  <xsl:template match="/" mode="diagram-css">
    <adl:css>@import url('/github/kite9-org/kite9/templates/designer/designer.css');</adl:css>
  </xsl:template>
  
   <xsl:template match="/" mode="diagram-script">
     import '/github/kite9-org/kite9/templates/risk-first/risk-first.js?v=v0.4'
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
    <adl:palette contains="connected" url="/github/kite9-org/kite9/templates/designer/palette.adl"/>
  </xsl:template>
 
</xsl:stylesheet>