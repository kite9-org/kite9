<xsl:stylesheet
		xmlns="http://www.w3.org/2000/svg"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:pp="http://www.kite9.org/schema/post-processor"
		xmlns:adl="http://www.kite9.org/schema/adl"
		version="1.0">

  <!--
    Handles calls for the 'shape' mode, observing the k9-shape attribute.
    'shape' mode is used for generating (usually) the background shape of an object,
    but it could also be a shape displayed in a portrait mode or alongside some text 
    (see formats).
  -->  
  
  <xsl:template name="shape-ellipse" match="*[@k9-shape='ellipse']" mode="shape" priority="2">
    <ellipse cx="0" cy="0" rx="0" ry="0">
      <xsl:attribute name="pp:cx">$x + $width div 2</xsl:attribute>
      <xsl:attribute name="pp:cy">$y + $height div 2</xsl:attribute>
      <xsl:attribute name="pp:rx">$width div 2</xsl:attribute>
      <xsl:attribute name="pp:ry">$height div 2</xsl:attribute>
    </ellipse>
  </xsl:template>
  
  <!-- default background is a rounded-rectangle -->
  
  <xsl:template name="shape-round-rect" match="*" mode="shape">
    <xsl:param name="k9-rounding">0pt</xsl:param>
    <rect x="0" y="0" width="0" height="0">
      <xsl:attribute name="rx"><xsl:value-of select="$k9-rounding" /></xsl:attribute>
      <xsl:attribute name="ry"><xsl:value-of select="$k9-rounding" /></xsl:attribute>
      <xsl:attribute name="pp:width">$width</xsl:attribute>
      <xsl:attribute name="pp:height">$height</xsl:attribute>
      <xsl:attribute name="pp:x">$x</xsl:attribute>
      <xsl:attribute name="pp:y">$y</xsl:attribute>
    </rect>
  </xsl:template>
  
  <xsl:template match="text()" mode="shape" />
  
  
</xsl:stylesheet>