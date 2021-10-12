<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="flowchart-components.xsl" />
  
  <xsl:template match="/" mode="diagram-script">
    import '/public/templates/flowchart/flowchart.js';
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
    <xsl:next-match />
    @import url('/public/templates/flowchart/flowchart-elements.css');
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    @import url('/public/templates/flowchart/flowchart-textures.css');
  </xsl:template>

  <xsl:template match="/" mode="diagram-palette">
    <adl:palette contains="connected" url="/public/templates/flowchart/palette-inline.adl"/>
    <adl:palette contains="connected" url="/public/templates/flowchart/palette-captioned.adl"/>
    <xsl:next-match/>
  </xsl:template>
  
</xsl:stylesheet>  