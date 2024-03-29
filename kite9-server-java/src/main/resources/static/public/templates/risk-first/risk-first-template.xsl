<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="/public/templates/adl/adl-all-components.xsl" />
  <xsl:import href="risks/risk-components.xsl" />
  <xsl:import href="artifacts/artifact-components.xsl" />
  <xsl:import href="planner/planner-components.xsl" />
  <xsl:import href="actions/action-components.xsl" />
  <xsl:import href="site/site-components.xsl" />
   
  <xsl:template match="/" mode="diagram-script">
     import '/public/templates/risk-first/risk-first.js?v=v0.15'
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    <adl:css>@import url('/public/templates/risk-first/risk-first-textures.css');</adl:css>
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
    <adl:palette contains="connected" url="/public/templates/risk-first/risks/risk-palette.adl"/>
    <adl:palette contains="connected" url="/public/templates/risk-first/artifacts/artifact-palette.adl"/>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
    <xsl:call-template name="adl-diagram-element-css" />
    <xsl:call-template name="actions-risk-first-diagram-element-css" />
    <xsl:call-template name="artifacts-risk-first-diagram-element-css" />
    <xsl:call-template name="planner-risk-first-diagram-element-css" />
    <xsl:call-template name="risks-risk-first-diagram-element-css" />
    <xsl:call-template name="site-risk-first-diagram-element-css" />
  </xsl:template>
 
  <xsl:template match="/" mode="diagram-defs">
  	<xsl:call-template name="adl-diagram-defs" />
  	<xsl:call-template name="background-risk-first-diagram-defs" />
  	<xsl:call-template name="site-risk-first-diagram-defs" />
  </xsl:template>
  
  
  <xsl:template match="/" mode="diagram-palettes">
    <adl:palette url="/public/templates/risk-first/risks/risk-palette.adl" />
    <adl:palette url="/public/templates/risk-first/artifacts/artifact-palette.adl" />
  	<xsl:call-template name="adl-diagram-palettes" />
  </xsl:template>
  
  
  <xsl:template match="/" mode="diagram-font-families">
    <adl:font-family weights="100 200 300 400 500 600 700 800 900" styles="regular italic">Metropolis</adl:font-family>
  </xsl:template>
  
  
  <xsl:template match="/" mode="diagram-constants">
  	<xsl:call-template name="adl-diagram-constants" />
  </xsl:template>
  
  
</xsl:stylesheet>