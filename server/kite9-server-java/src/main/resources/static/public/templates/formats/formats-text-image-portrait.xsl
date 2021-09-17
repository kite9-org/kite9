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

    <xsl:param name="content"><text><xsl:value-of select="text()" /></text></xsl:param>

    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-image-portrait</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-highlight-depiction">outline</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture-back">background</xsl:param>
    <xsl:param name="k9-texture-image">none</xsl:param>
    <xsl:param name="k9-texture-text">foreground</xsl:param>
    <xsl:param name="k9-ui">drag edit delete align connect insert autoconnect image</xsl:param>
    <xsl:param name="k9-ui-depiction">image</xsl:param>
    
    <xsl:param name="background">
      <xsl:call-template name="texture-basic">
        <xsl:with-param name="k9-highlight" select="$k9-highlight" />
        <xsl:with-param name="k9-texture" select="$k9-texture-back" />
        <xsl:with-param name="shape">
          <xsl:call-template name="shape-round-rect">
            <xsl:with-param name="rounding" select="$k9-rounding" />
          </xsl:call-template>
        </xsl:with-param> 
      </xsl:call-template>  
    </xsl:param>
    
    <xsl:param name="image">
      <image x="0" y="0">
        <xsl:attribute name="xlink:href"><xsl:value-of select="$href" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="$width" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="$height" /></xsl:attribute>
      </image>
    </xsl:param>
    
    <xsl:param name="highlight">
      <g>
        <xsl:attribute name="k9-highlight"><xsl:value-of select="$k9-highlight-depiction" /></xsl:attribute>
        <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture-image" /></xsl:attribute>
        <rect x='0' y='0'>
          <xsl:attribute name="width"><xsl:value-of select="$width" /></xsl:attribute>
          <xsl:attribute name="height"><xsl:value-of select="$height" /></xsl:attribute>
        </rect>
      </g>
    </xsl:param>
    
    <xsl:param name="depiction">
      <g k9-elem="depiction">
        <xsl:if test="$k9-ui-depiction">
          <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui-depiction" /></xsl:attribute>
          <xsl:attribute name="id"><xsl:value-of select="$id" />@dep</xsl:attribute>
        </xsl:if>
        <xsl:copy-of select="$image" />
        <xsl:copy-of select="$highlight" />
      </g>
    </xsl:param>
    
    <xsl:param name="k9-text">
      <g k9-elem="caption">
        <xsl:call-template name="texture-basic">
          <xsl:with-param name="k9-texture" select="$k9-texture-text" />
          <xsl:with-param name="k9-highlight"></xsl:with-param>
          <xsl:with-param name="class" select="$class" />
          <xsl:with-param name="style" select="$style" />
          <xsl:with-param name="shape" select="$content" />
        </xsl:call-template>
      </g>
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates select="." mode="image-decoration" /></xsl:param>
    
    <g>      
      <xsl:copy-of select="$attributes" />      

      <xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>

      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class" /></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$style">
        <xsl:attribute name="style"><xsl:value-of select="$style" /></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$id">
        <xsl:attribute name="id"><xsl:value-of select="$id" /></xsl:attribute>
      </xsl:if>
   
      <xsl:comment>background</xsl:comment> 
      <xsl:copy-of select="$background" />
      <xsl:comment>depiction</xsl:comment> 
      <xsl:copy-of select="$depiction"/>      
      <xsl:comment>text</xsl:comment> 
      <xsl:copy-of select="$k9-text"/>
      <xsl:comment>decoration</xsl:comment> 
      <xsl:copy-of select="$k9-decoration" />
    </g>
  </xsl:template>
</xsl:stylesheet>