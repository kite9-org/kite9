<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="../votes/votes-template.xsl" />
  <xsl:import href="risks/risks.xsl" />
  <xsl:import href="artifacts/artifacts.xsl" />
  <xsl:import href="planner/planner.xsl" />
  <xsl:import href="actions/actions.xsl" />
   
  <xsl:template match="/" mode="diagram-script">
     import '/public/templates/risk-first/risk-first.js';
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-css">
    @import url('/public/templates/risk-first/risk-first.css');
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
    <adl:palette contains="connected" url="/public/templates/risk-first/risks/risk-palette.adl"/>
    <adl:palette contains="connected" url="/public/templates/risk-first/artifacts/artifact-palette.adl"/>
    <xsl:next-match/>
  </xsl:template>
 
  
</xsl:stylesheet>