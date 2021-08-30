<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


  <xsl:import href="formats-container.xsl" />
  <xsl:import href="formats-text-captioned.xsl" />
  <xsl:import href="formats-text-portrait.xsl" />
  <xsl:import href="formats-text-inline.xsl" />
  <xsl:import href="formats-text-inline-fixed.xsl" />
  <xsl:import href="formats-shape.xsl" />
  <xsl:import href="formats-image.xsl" />
  <xsl:import href="formats-image-fixed.xsl" />
  
  
  <xsl:template match="text()" mode="text-decoration" />
  
</xsl:stylesheet>
