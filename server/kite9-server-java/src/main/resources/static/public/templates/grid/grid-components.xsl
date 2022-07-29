<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <!-- tables -->
  
  <xsl:template name="grid-table-basic">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">0pt</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete layout</xsl:with-param>
      <xsl:with-param name="k9-palette">connected table</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="grid-cell-basic">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">0pt</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete cascade orphan layout grid</xsl:with-param>
      <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
      <xsl:with-param name="k9-palette">cell</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  
  <xsl:template match="adl:table">
    <xsl:call-template name="grid-table-basic" />
  </xsl:template>
  
  <xsl:template match="adl:cell">
    <xsl:call-template name="grid-cell-basic" />
  </xsl:template>
  
  
  <xsl:template name="grid-diagram-element-css">
    <adl:css>@import url('/github/kite9-org/kite9/templates/grid/grid-elements.css');</adl:css>
  </xsl:template>
    
  <xsl:template name="grid-diagram-palettes">
     <adl:palette contains="cell table" url="/github/kite9-org/kite9/templates/grid/palette.adl" />
  </xsl:template>  

</xsl:stylesheet>
