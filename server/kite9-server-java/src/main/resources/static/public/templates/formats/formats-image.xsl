<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:xlink="https://www.w3.org/1999/xlink"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  <xsl:import href="../back/back-template.xsl" />
  <xsl:import href="generic-formats-template.xsl" />
  
   <!-- 
    Provides some standard formats for images.
    
    
    image
    image-fixed
  
   Introduces the image-decoration mode
  
   -->

  <!-- A basic on-screen image, params: @href, @width @height -->
  <!-- batik hates it if you don't specify height & width -->

<!--  <xsl:template name="image-editable" k9-ui="image" k9-format="image-editable">
  <decal style="kite9-type: svg; kite9-usage: decal;" k9-texture="solid">
   
  </decal>
  <front k9-highlight="stroke pulse" style='kite9-type: svg; ' k9-texture="none">
    
  </front>
</xsl:template>
     -->
    
    
  <xsl:template name="formats-image" match="*[@k9-format='image']">
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

    <xsl:param name="k9-contains"></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">image</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-palette"></xsl:param>
    <xsl:param name="k9-rounding">0pt</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">image</xsl:param>
    
    <xsl:param name="k9-shape">
      <xsl:call-template name="back-basic">
        <xsl:with-param name="highlight" select="$k9-highlight" /> 
        <xsl:with-param name="rounding" select="$k9-rounding" />
      </xsl:call-template>  
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates select="." mode="image-decoration" /></xsl:param>

    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    
    <xsl:call-template name="formats-generic">
      <xsl:with-param name="pre" select="$k9-shape" />
      <xsl:with-param name="content">
         <image x="0" y="0" width="$width" height="$height" xlink:href="@href"/>
         <rect x='0' y='0' width='$width' height='$height' rx='2' ry='2'/>
      </xsl:with-param>
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
    
    </xsl:call-template>
    
  </xsl:template>
  
</xsl:stylesheet>