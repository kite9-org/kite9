<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../texture/texture-template.xsl" />

  <xsl:template name="formats-container" match="*[@format='container']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style']" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="content"><xsl:apply-templates /></xsl:param>
    <xsl:param name="k9-contains">connected</xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="format">container</xsl:param>
    <xsl:param name="highlight">pulse</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="rounding">5pt</xsl:param>
    <xsl:param name="texture">background</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect insert autoconnect</xsl:param>
    
    <xsl:param name="k9-shape">
      <xsl:call-template name="texture-basic">
        <xsl:with-param name="highlight" select="$highlight" />
        <xsl:with-param name="texture" select="$texture" />
        <xsl:with-param name="rounding" select="$rounding" />
      </xsl:call-template>  
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates mode="container-decoration" select="." /></xsl:param>
    
    <g>      
      <xsl:copy-of select="$attributes" />      

      <xsl:attribute name="k9-contains"><xsl:value-of select="$k9-contains" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>
      <xsl:attribute name="format"><xsl:value-of select="$format" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
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
   
      <xsl:comment>shape</xsl:comment> 
      <xsl:copy-of select="$k9-shape" />
      <xsl:comment>content</xsl:comment> 
      <xsl:copy-of select="$content"/>
      <xsl:comment>post</xsl:comment> 
      <xsl:copy-of select="$k9-decoration" />
    </g>
  </xsl:template>
  
  
  <xsl:template match="text()" mode="container-decoration" />
  
  
</xsl:stylesheet>
