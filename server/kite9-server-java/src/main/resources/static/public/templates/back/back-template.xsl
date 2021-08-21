<xsl:stylesheet
		xmlns="http://www.w3.org/2000/svg"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:pp="http://www.kite9.org/schema/post-processor"
		xmlns:adl="http://www.kite9.org/schema/adl"
		version="1.0">

  <!--
    Provides the background <g> for a user-defined element.  Defaults to a rounded rectangle 
    unless the caller gives a shape, or one is defined by templates.
  -->
  
	<xsl:template name="back-basic">
		<xsl:param name="rounding">0pt</xsl:param>
		<xsl:param name="highlight">pulse</xsl:param>
    <xsl:param name="shape">
      <xsl:apply-templates mode="back" select=".">
        <xsl:with-param name="rounding" select="$rounding" />
      </xsl:apply-templates>
    </xsl:param>
    
		<g>
			<xsl:attribute name="k9-elem">back</xsl:attribute>
			<xsl:attribute name="k9-highlight"><xsl:value-of select="$highlight" /></xsl:attribute>
      <xsl:copy-of select="$shape" />
		</g>
    
	</xsl:template>
  
  <!-- default background is a rounded-rectangle -->
  
  <xsl:template match="*" mode="back">
    <xsl:param name="rounding">0pt</xsl:param>
    <rect x="0" y="0" width="0" height="0">
      <xsl:attribute name="rx"><xsl:value-of select="$rounding" /></xsl:attribute>
      <xsl:attribute name="ry"><xsl:value-of select="$rounding" /></xsl:attribute>
      <xsl:attribute name="pp:width">$width</xsl:attribute>
      <xsl:attribute name="pp:height">$height</xsl:attribute>
    </rect>
  </xsl:template>

</xsl:stylesheet>