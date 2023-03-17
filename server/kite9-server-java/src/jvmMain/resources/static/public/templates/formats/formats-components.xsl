<xsl:stylesheet xmlns="http://www.w3.org/2000/svg" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl" xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="formats-container-include.xsl"/>
  <xsl:import href="formats-text-shape-inline-include.xsl"/>
  <xsl:import href="formats-text-fixed-include.xsl"/>
  <xsl:import href="formats-shape-include.xsl"/>
  <xsl:import href="formats-text-shape-portrait-include.xsl"/>
  <xsl:import href="formats-text-shape-captioned-include.xsl"/>
  <xsl:import href="formats-image-include.xsl"/>
  <xsl:import href="formats-image-fixed-include.xsl"/>
  <xsl:import href="formats-text-image-portrait-include.xsl"/>

  <xsl:template match="text()" mode="text-decoration"/>
  <xsl:template match="text()" mode="image-decoration"/>


  <xsl:template name="formats-diagram-element-css">
    <adl:css>@import url('/public/templates/formats/formats.css');</adl:css>
  </xsl:template>
  

</xsl:stylesheet>
