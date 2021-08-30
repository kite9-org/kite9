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
    <xsl:param name="k9-elem">back</xsl:param>
    <xsl:param name="shape">
      <xsl:apply-templates mode="shape" select=".">
        <xsl:with-param name="rounding" select="$rounding" />
      </xsl:apply-templates>
    </xsl:param>
    
		<g>
			<xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>
			<xsl:attribute name="k9-highlight"><xsl:value-of select="$highlight" /></xsl:attribute>
      <xsl:copy-of select="$shape" />
		</g>
    
	</xsl:template>
  
  <!-- default background is a rounded-rectangle -->
  
  <xsl:template name="back-round-rect" match="*" mode="shape">
    <xsl:param name="rounding">0pt</xsl:param>
    <rect x="5" y="0" width="0" height="0">
      <xsl:attribute name="rx"><xsl:value-of select="$rounding" /></xsl:attribute>
      <xsl:attribute name="ry"><xsl:value-of select="$rounding" /></xsl:attribute>
      <xsl:attribute name="pp:width">$width</xsl:attribute>
      <xsl:attribute name="pp:height">$height</xsl:attribute>
      <xsl:attribute name="pp:x">$x</xsl:attribute>
      <xsl:attribute name="pp:y">$y</xsl:attribute>
    </rect>
  </xsl:template>
  
  
  <xsl:template name="back-ellipse" match="*[@k9-shape='ellipse']" mode="shape" priority="2">
    <ellipse cx="0" cy="0" rx="0" ry="0">
      <xsl:attribute name="pp:cx">$x + $width div 2</xsl:attribute>
      <xsl:attribute name="pp:cy">$y + $height div 2</xsl:attribute>
      <xsl:attribute name="pp:rx">$width div 2</xsl:attribute>
      <xsl:attribute name="pp:ry">$height div 2</xsl:attribute>
    </ellipse>
  </xsl:template>
  
  <xsl:template name="back-image" match="*[@href]" mode="shape" priority="2">
  
  
  </xsl:template>
  
</xsl:stylesheet>