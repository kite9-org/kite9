<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl-all-components.xsl" />
  <xsl:import href="flowchart-components.xsl" />
  
  <xsl:template match="/" mode="diagram-script">
    import '/github/kite9-org/kite9/templates/flowchart/flowchart.js?v=v0.15'
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    <adl:css>@import url('/github/kite9-org/kite9/templates/flowchart/flowchart-textures.css');</adl:css>
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
  	<xsl:call-template name="adl-diagram-palettes" />
    <adl:palette contains="connected" url="/github/kite9-org/kite9/templates/flowchart/palette-inline.adl"/>
    <adl:palette contains="connected" url="/github/kite9-org/kite9/templates/flowchart/palette-captioned.adl"/>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
  	<xsl:call-template name="adl-diagram-element-css" />
    <xsl:call-template name="flowchart-diagram-element-css" />
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-defs">
  	<xsl:call-template name="adl-diagram-defs" />
  </xsl:template>
  
  
</xsl:stylesheet>  