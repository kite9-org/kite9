<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  <xsl:import href="/public/templates/containers/containers-template.xsl"/>  


  <!-- tables -->
  
  <xsl:template name="grid-table-basic">
    <xsl:call-template name="containers-basic">
      <xsl:with-param name="k9-rounding">0pt</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete layout</xsl:with-param>
      <xsl:with-param name="k9-palette">connected table</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="grid-cell-basic">
    <xsl:call-template name="containers-basic">
      <xsl:with-param name="k9-rounding">0pt</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete cascade orphan layout grid</xsl:with-param>
      <xsl:with-param name="k9-highlight">pulse stroke</xsl:with-param>
      <xsl:with-param name="k9-palette">cell</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
