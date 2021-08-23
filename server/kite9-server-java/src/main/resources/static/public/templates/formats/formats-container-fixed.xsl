<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


  <xsl:import href="../back/back-template.xsl" />
  <xsl:import href="generic-formats-template.xsl" />

  <xsl:template name="formats-container-fixed" match="*[@k9-format='container-fixed']">
    <xsl:param name="k9-format">container-fixed</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect autoconnect cascade</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="layout"><xsl:value-of select="@layout"/></xsl:param>
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
      <xsl:if test="$layout">
        <xsl:attribute name="layout"><xsl:value-of select="$layout"></xsl:value-of></xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="@*" />
       
      <xsl:call-template name="back-basic">
        <xsl:with-param name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:with-param>
        <xsl:with-param name="k9-rounding"><xsl:value-of select="$k9-rounding" /></xsl:with-param>
       </xsl:call-template>
       
      <xsl:apply-templates />
     </g>
  </xsl:template>  

</xsl:stylesheet>
