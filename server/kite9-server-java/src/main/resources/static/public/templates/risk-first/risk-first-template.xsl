<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="../votes/votes-components.xsl" />
  <xsl:import href="risks/risk-components.xsl" />
  <xsl:import href="artifacts/artifact-components.xsl" />
  <xsl:import href="planner/planner-components.xsl" />
  <xsl:import href="actions/action-components.xsl" />
  <xsl:import href="site/site-components.xsl" />
   
  <xsl:template match="/" mode="diagram-script">
     import '/public/templates/risk-first/risk-first.js';
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    @import url('/public/templates/risk-first/risk-first-textures.css');
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
    <adl:palette contains="connected" url="/public/templates/risk-first/risks/risk-palette.adl"/>
    <adl:palette contains="connected" url="/public/templates/risk-first/artifacts/artifact-palette.adl"/>
    <xsl:next-match/>
  </xsl:template>
 
  
</xsl:stylesheet>