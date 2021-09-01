<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../texture/texture-template.xsl" />
  <xsl:import href="../shape/shape-template.xsl" />
  
  <xsl:template name="formats-text-shape-captioned" match="*[@k9-format='text-shape-captioned']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style']" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="content"><text><xsl:value-of select="text()" /></text></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-shape-captioned</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">foreground</xsl:param>
    <xsl:param name="k9-ui">drag delete</xsl:param>
    
     <xsl:param name="background">
      <xsl:call-template name="texture-basic">
        <xsl:with-param name="k9-highlight" select="$k9-highlight" />
        <xsl:with-param name="k9-texture">background</xsl:with-param>
        <xsl:with-param name="shape">
          <xsl:call-template name="shape-round-rect">
            <xsl:with-param name="rounding" select="$k9-rounding" />
          </xsl:call-template>
        </xsl:with-param> 
      </xsl:call-template>  
    </xsl:param>
    
    <xsl:param name="k9-shape">
      <g k9-elem="depiction">
        <xsl:attribute name="k9-ui">connect autoconnect align</xsl:attribute>
        <xsl:attribute name="id"><xsl:value-of select="$id" />@dep</xsl:attribute>
        <xsl:call-template name="texture-basic">
          <xsl:with-param name="k9-highlight" select="$k9-highlight" />
          <xsl:with-param name="k9-texture" select="$k9-texture" />
          <xsl:with-param name="rounding" select="$k9-rounding" />
        </xsl:call-template>  
      </g>
    </xsl:param>
    
    <xsl:param name="k9-text">
      <g k9-elem="caption">
        <xsl:attribute name="id"><xsl:value-of select="$id" />@caption</xsl:attribute>   
        <xsl:attribute name="k9-ui">edit</xsl:attribute>
        <xsl:call-template name="texture-basic">
          <xsl:with-param name="k9-texture" select="$k9-texture" />
          <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
          <xsl:with-param name="class" select="$class" />
          <xsl:with-param name="style" select="$style" />
          <xsl:with-param name="shape" select="$content" />
        </xsl:call-template>
      </g>
    </xsl:param>    
    
    <xsl:param name="k9-decoration"><xsl:apply-templates mode="text-decoration" /></xsl:param>
        
    <xsl:param name="alignment">
      <!-- no idea why, but we need an extra param in here otherwise something goes haywire -->
      BOBO
    </xsl:param>
    
    <xsl:param name="k9-align">
      <g k9-elem="shape-align">
        <g k9-elem="from">
          <xsl:attribute name="reference"><xsl:value-of select="$id" />@dep</xsl:attribute>
        </g> 
        <g k9-elem="to">
          <xsl:attribute name="reference"><xsl:value-of select="$id" />@caption</xsl:attribute>   
        </g>
      </g>  
    </xsl:param>
    
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
      <xsl:comment>shape</xsl:comment> 
      <xsl:copy-of select="$k9-shape" />
      <xsl:comment>text</xsl:comment> 
      <xsl:copy-of select="$k9-text"/>
      <xsl:comment>align</xsl:comment>
      <xsl:copy-of select="$k9-align"/> 
      <xsl:comment>decoration</xsl:comment> 
      <xsl:copy-of select="$k9-decoration" />
    </g>
  </xsl:template>
</xsl:stylesheet>