<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:template name="formats-container" match="*[@k9-format='container']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style']" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="k9-child"></xsl:param>
    <xsl:param name="k9-containers"></xsl:param>
    <xsl:param name="k9-contains">connected</xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">container</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">background</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect insert autoconnect fill stroke size align</xsl:param>
    
    <xsl:param name="shape">
      <xsl:apply-templates mode="shape" select=".">
        <xsl:with-param name="k9-rounding" select="$k9-rounding" />
      </xsl:apply-templates>
    </xsl:param>
    <xsl:param name="content"><xsl:apply-templates select="*" /></xsl:param>
    <xsl:param name="decoration"><xsl:apply-templates mode="container-decoration" select="." /></xsl:param>
    
    <xsl:call-template name="texture-basic">
      <xsl:with-param name="k9-child" select="$k9-child" />
      <xsl:with-param name="k9-containers" select="$k9-containers"  />
      <xsl:with-param name="k9-contains" select="$k9-contains"  />
      <xsl:with-param name="k9-elem" select="$k9-elem"  />
      <xsl:with-param name="k9-format" select="$k9-format"  />
      <xsl:with-param name="k9-highlight" select="$k9-highlight"  />
      <xsl:with-param name="k9-texture" select="$k9-texture"  />
      <xsl:with-param name="k9-palette" select="$k9-palette" />
      <xsl:with-param name="k9-ui" select="$k9-ui" />
      <xsl:with-param name="id" select="$id"  />
      <xsl:with-param name="style" select="$style" />
      <xsl:with-param name="class" select="$class" />
      <xsl:with-param name="attributes" select="$attributes"  />
      <xsl:with-param name="shape" select="$shape" />
      <xsl:with-param name="content" select="$content" />
      <xsl:with-param name="decoration" select="$decoration" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="text()" mode="container-decoration" />
    
</xsl:stylesheet>