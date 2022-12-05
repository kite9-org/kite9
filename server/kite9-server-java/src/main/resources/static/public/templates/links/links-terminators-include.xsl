<xsl:stylesheet 
	xmlns="http://www.w3.org/2000/svg" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:adl="http://www.kite9.org/schema/adl"
    xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:attribute-set name="terminators">
    <xsl:attribute name="k9-texture">none</xsl:attribute>
    <xsl:attribute name="k9-ui">drag label direction</xsl:attribute>
    <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()"/></xsl:attribute>
    <xsl:attribute name="k9-palette">terminator</xsl:attribute>
  </xsl:attribute-set>

  <xsl:template name="terminator">
    <g xsl:use-attribute-sets="terminators">
      <xsl:copy-of select="@*"/>
      <g>
        <xsl:attribute name="k9-highlight">fill</xsl:attribute>
        <ellipse cx='0' cy='0' rx="8" ry="8"/>
        <xsl:apply-templates/>
      </g>
    </g>
  </xsl:template>

  <xsl:template match="adl:from | adl:to">
	<xsl:call-template name="terminator"/>
  </xsl:template>

</xsl:stylesheet>