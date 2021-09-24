<xsl:stylesheet
		xmlns="http://www.w3.org/2000/svg"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:pp="http://www.kite9.org/schema/post-processor"
		xmlns:adl="http://www.kite9.org/schema/adl"
		version="1.0">

  <!--
    Provides the <g> for a user-defined element which will have a
    texture. Can be used for text or background shapes.  
    
    Generally, the visible/highlightable part of any element is the texture.
  -->
  
	<xsl:template name="texture-basic">
		<xsl:param name="k9-rounding">0pt</xsl:param>
		<xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-texture">default</xsl:param>
    <xsl:param name="style"></xsl:param>
    <xsl:param name="class"></xsl:param>

    <xsl:param name="shape">
      <xsl:apply-templates mode="shape" select=".">
        <xsl:with-param name="k9-rounding" select="$k9-rounding" />
      </xsl:apply-templates>
    </xsl:param>
    
		<g>
      <xsl:if test="$k9-highlight">
        <xsl:attribute name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:attribute>
      </xsl:if>
      <xsl:if test="$k9-texture">
 			  <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      </xsl:if>
      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="$style">
        <xsl:attribute name="style"><xsl:value-of select="$style"/></xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="$shape" />
		</g>
    
	</xsl:template>
  
  
</xsl:stylesheet>