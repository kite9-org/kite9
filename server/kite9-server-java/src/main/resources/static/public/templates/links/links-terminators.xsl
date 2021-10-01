<xsl:stylesheet xmlns="http://www.w3.org/2000/svg" xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


  <xsl:attribute-set name="terminators">
    <xsl:attribute name="k9-texture">none</xsl:attribute>
    <xsl:attribute name="k9-palette">end</xsl:attribute>
    <xsl:attribute name="k9-containers">link</xsl:attribute>
    <xsl:attribute name="k9-ui">drag label</xsl:attribute>
    <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()"/></xsl:attribute>

  </xsl:attribute-set>


  <xsl:template name="round-terminator">
    <g xsl:use-attribute-sets="terminators">
      <xsl:copy-of select="@*"/>
      <g>
        <xsl:attribute name="k9-highlight">fill grab</xsl:attribute>
        <ellipse cx='0' cy='0' rx="8" ry="8"/>
        <xsl:apply-templates/>
      </g>
    </g>
  </xsl:template>

  <xsl:template name="up-terminator">
    <g xsl:use-attribute-sets="terminators">
      <xsl:copy-of select="@*"/>
      <g>
        <xsl:attribute name="k9-highlight">fill grab</xsl:attribute>
        <polygon points="-10 12, 0 -8, 10 12"/>
        <xsl:apply-templates/>
      </g>
    </g>
  </xsl:template>


  <xsl:template name="down-terminator">
    <g xsl:use-attribute-sets="terminators">
      <xsl:copy-of select="@*"/>
      <g>
        <xsl:attribute name="k9-highlight">fill grab</xsl:attribute>

        <polygon points="10 -12, 0 8, -10 -12"/>
        <xsl:apply-templates/>
      </g>
    </g>
  </xsl:template>


  <xsl:template name="left-terminator">
    <g xsl:use-attribute-sets="terminators">
      <xsl:copy-of select="@*"/>
      <g>
        <xsl:attribute name="k9-highlight">fill grab</xsl:attribute>

        <polygon points="12 -10, -8 0, 12 10"/>
        <xsl:apply-templates/>
      </g>
    </g>
  </xsl:template>


  <xsl:template name="right-terminator">
    <g xsl:use-attribute-sets="terminators">
      <xsl:copy-of select="@*"/>
      <g>
        <xsl:attribute name="k9-highlight">fill grab</xsl:attribute>

        <polygon points="-12 -10, 8 0, -12 10"/>
        <xsl:apply-templates/>
      </g>
    </g>
  </xsl:template>

  <xsl:template name="null-terminator">
    <g xsl:use-attribute-sets="terminators">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </g>
  </xsl:template>

  <xsl:template match="adl:from">
    <xsl:choose>
      <xsl:when test="parent::adl:link[@drawDirection='UP']">
        <xsl:call-template name="down-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[@drawDirection='DOWN']">
        <xsl:call-template name="up-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[@drawDirection='LEFT']">
        <xsl:call-template name="right-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[@drawDirection='RIGHT']">
        <xsl:call-template name="left-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[contains(@class,'null')]">
        <xsl:call-template name="null-terminator"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="round-terminator"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template match="adl:to">
    <xsl:choose>
      <xsl:when test="parent::adl:link[@drawDirection='UP']">
        <xsl:call-template name="up-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[@drawDirection='DOWN']">
        <xsl:call-template name="down-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[@drawDirection='LEFT']">
        <xsl:call-template name="left-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[@drawDirection='RIGHT']">
        <xsl:call-template name="right-terminator"/>
      </xsl:when>
      <xsl:when test="parent::adl:link[contains(@class,'null')]">
        <xsl:call-template name="null-terminator"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="round-terminator"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

</xsl:stylesheet>