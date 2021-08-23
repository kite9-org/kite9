<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../back/back-template.xsl" />
  <xsl:import href="generic-formats-template.xsl" />
  
  <xsl:template name='formats-text-inline' match="*[@k9-format='text-inline']">
    <xsl:param name="content"><text><xsl:value-of select="./text()" /></text></xsl:param>

    <xsl:param name="k9-contains"></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-inline</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-palette"></xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag edit delete align connect insert autoconnect</xsl:param>
    <xsl:param name="k9-shape">
      <rect x="10" y="10" width="15" height="15" 
         pp:x="$x" pp:y="$y" pp:width="$width" pp:height="$height" />
    </xsl:param>
    <xsl:param name="k9-decoration"><xsl:apply-templates mode="text-decoration" /></xsl:param>

    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    <xsl:param name="id" select="@id" />
    
    <xsl:call-template name="formats-generic">
      <xsl:with-param name="pre" select="$k9-shape" />
      <xsl:with-param name="content" select="$content" />
      <xsl:with-param name="post" select="$k9-decoration" />
      
      <!-- standard params -->
      <xsl:with-param name="k9-contains" select="$k9-contains" />
      <xsl:with-param name="k9-elem" select="$k9-elem" />
      <xsl:with-param name="k9-format" select="$k9-format" />
      <xsl:with-param name="k9-palette" select="$k9-palette" />
      <xsl:with-param name="k9-texture" select="$k9-texture" />
      <xsl:with-param name="k9-ui" select="$k9-ui" />
      <xsl:with-param name="attributes" select="$attributes" />
      <xsl:with-param name="class" select="$class" />
      <xsl:with-param name="id" select="$id" />
    
    </xsl:call-template>
    
  </xsl:template>

  
  
</xsl:stylesheet>