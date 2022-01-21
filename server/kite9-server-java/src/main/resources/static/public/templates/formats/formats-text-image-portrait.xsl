<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../texture/texture-template.xsl" />
  <xsl:import href="../shape/shape-template.xsl" />

  <xsl:template name="formats-text-image-portrait" match="*[@k9-format='text-image-portrait']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style']" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="k9-containers"></xsl:param>
    <xsl:param name="k9-contains">connected</xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-image-portrait</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">none</xsl:param>
    <xsl:param name="k9-ui">drag edit delete align connect insert autoconnect</xsl:param>

    <xsl:param name="href"><xsl:value-of select="@href" /></xsl:param>
    
    <xsl:param name="width">
      <xsl:choose>
        <xsl:when test="@width"><xsl:value-of select="@width" /></xsl:when>
        <xsl:otherwise>50pt</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    
    <xsl:param name="height">
      <xsl:choose>
        <xsl:when test="@height"><xsl:value-of select="@height" /></xsl:when>
        <xsl:otherwise>50pt</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
        
    <xsl:param name="shape">
      <xsl:apply-templates mode="shape" select=".">
        <xsl:with-param name="k9-rounding" select="$k9-rounding" />
      </xsl:apply-templates>
    </xsl:param>

    <xsl:param name="text"><text><xsl:value-of select="text()" /></text></xsl:param>

    <xsl:param name="image">
      <image x="0" y="0">
        <xsl:attribute name="xlink:href"><xsl:value-of select="$href" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="$width" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="$height" /></xsl:attribute>
      </image>
    </xsl:param>
    
    <xsl:param name="highlight">
      <rect x='0' y='0'>
        <xsl:attribute name="width"><xsl:value-of select="$width" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="$height" /></xsl:attribute>
      </rect>
    </xsl:param>
    
    <xsl:param name="depiction-id"><xsl:value-of select="$id" />@dep</xsl:param>
    
    <xsl:param name="depiction">
      <xsl:call-template name="texture-basic">
        <xsl:with-param name="k9-elem">depiction</xsl:with-param>
        <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
        <xsl:with-param name="k9-texture">none</xsl:with-param>
        <xsl:with-param name="k9-ui">image</xsl:with-param>
        <xsl:with-param name="id" select="$depiction-id"></xsl:with-param>
        <xsl:with-param name="shape"></xsl:with-param>
        <xsl:with-param name="content">
          <xsl:copy-of select="$image" />
          <xsl:copy-of select="$highlight" />
        </xsl:with-param>
      </xsl:call-template>
    </xsl:param>
    
    <xsl:param name="caption">
      <xsl:call-template name="texture-basic">
        <xsl:with-param name="k9-elem">caption</xsl:with-param>
        <xsl:with-param name="k9-texture">foreground</xsl:with-param>
        <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
        <xsl:with-param name="k9-format">text-fixed</xsl:with-param>
        <xsl:with-param name="shape"></xsl:with-param>
        <xsl:with-param name="content" select="$text" />
      </xsl:call-template>
    </xsl:param>
    
    <xsl:param name="content">
    	<xsl:copy-of select="$depiction" />
    	<xsl:copy-of select="$caption" />
    </xsl:param>
    <xsl:param name="decoration"><xsl:apply-templates select="." mode="image-decoration" /></xsl:param>
    
    <xsl:call-template name="texture-basic">
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
</xsl:stylesheet>