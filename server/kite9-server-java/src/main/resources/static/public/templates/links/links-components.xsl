<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
 
 <xsl:import href="links-basic-include.xsl"/>
 <xsl:import href="links-align-include.xsl"/>
 <xsl:import href="links-terminators-include.xsl"/>
 <xsl:import href="links-markers-include.xsl"/>
 <xsl:import href="links-ports-include.xsl"/>
 
  
  <xsl:template name="links-hub">
    <xsl:call-template name="formats-shape" />
  </xsl:template> 
  
  <xsl:template match="adl:hub">
    <xsl:call-template name="links-hub" />
  </xsl:template>
  
  <xsl:template name="links-diagram-element-css">
    <adl:css>@import url('/public/templates/links/links-elements.css');</adl:css>
  </xsl:template>
  
  <xsl:template name="links-diagram-constants">
     <adl:constant name="link-template-uri" url="/public/templates/links/links-palette.adl#l2" />
     <adl:constant name="align-template-uri" url="/public/templates/links/links-palette.adl#align" />
     <adl:constant name="port-template-uri" url="/public/templates/links/ports-palette.adl#p-top" />
  </xsl:template>
  
  <xsl:template name="links-diagram-palettes">
     <adl:palette contains="link" url="/public/templates/links/links-palette.adl" />
     <adl:palette contains="end" url="/public/templates/links/ends-palette.adl" />
     <adl:palette contains="port" url="/public/templates/links/ports-palette.adl" />
  </xsl:template>
  
  <xsl:template name="links-diagram-defs">
  	<xsl:call-template name="links-markers" />
  </xsl:template>
  
</xsl:stylesheet>