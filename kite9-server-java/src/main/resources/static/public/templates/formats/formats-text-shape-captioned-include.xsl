<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  <xsl:template name="formats-text-shape-captioned" match="*[@k9-format='text-shape-captioned']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style' and name() != 'id' ]" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="k9-containers"></xsl:param>
    <xsl:param name="k9-contains"></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-shape-captioned</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-type">connected</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">none</xsl:param>
    <xsl:param name="k9-ui">drag delete fill size align</xsl:param>
    
    <xsl:param name="shape">
      <xsl:apply-templates mode="shape" select=".">
        <xsl:with-param name="k9-rounding" select="$k9-rounding" />
      </xsl:apply-templates>
    </xsl:param>
    
    <xsl:param name="text"><text><xsl:value-of select="text()" /></text></xsl:param>
    
    <xsl:param name="background">
      <xsl:call-template name="shape-round-rect">
        <xsl:with-param name="k9-rounding" select="$k9-rounding" />
      </xsl:call-template>
    </xsl:param>
    
    <xsl:param name="depiction-id"><xsl:choose>
    	<xsl:when test="$id"><xsl:value-of select="$id" />@dep</xsl:when>
    	<xsl:otherwise></xsl:otherwise>
    	</xsl:choose></xsl:param>
    
    <xsl:param name="caption-id"><xsl:choose>
    	<xsl:when test="$id"><xsl:value-of select="$id" />@cap</xsl:when>
    	<xsl:otherwise></xsl:otherwise>
    	</xsl:choose></xsl:param>
    
    <xsl:param name="depiction">
      <xsl:call-template name="texture-basic">
        <xsl:with-param name="k9-elem">depiction</xsl:with-param>
        <xsl:with-param name="k9-texture">background</xsl:with-param>
		<xsl:with-param name="k9-ui">connect autoconnect align</xsl:with-param>
        <xsl:with-param name="id" select="$depiction-id" />
        <xsl:with-param name="shape"><xsl:copy-of select="$shape" /></xsl:with-param>
        <xsl:with-param name="content"></xsl:with-param>
      </xsl:call-template>
    </xsl:param>    
    
    <xsl:param name="caption">
      <xsl:call-template name="texture-basic">
        <xsl:with-param name="k9-elem">caption</xsl:with-param>
        <xsl:with-param name="k9-texture">foreground</xsl:with-param>
		<xsl:with-param name="k9-ui">edit font</xsl:with-param>
        <xsl:with-param name="id" select="$caption-id" />
        <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
        <xsl:with-param name="k9-format">text-fixed</xsl:with-param>
        <xsl:with-param name="shape"></xsl:with-param>
        <xsl:with-param name="content" select="$text" />
      </xsl:call-template>
    </xsl:param>  
           
    <xsl:param name="alignment">
      <!-- no idea why, but we need an extra param in here otherwise something goes haywire -->
      BOBO
    </xsl:param>
    
    <xsl:param name="align">
      <g k9-elem="shape-align">
        <xsl:attribute name="style">--kite9-direction: right;</xsl:attribute>   
        <g k9-elem="from">
          <xsl:attribute name="reference"><xsl:value-of select="$depiction-id" /></xsl:attribute>
        </g> 
        <g k9-elem="to">
          <xsl:attribute name="reference"><xsl:value-of select="$caption-id" /></xsl:attribute>   
        </g>
      </g>  
    </xsl:param>
    
    <xsl:param name="content">
    	<xsl:copy-of select="$depiction" />
    	<xsl:copy-of select="$caption" />
    	<xsl:copy-of select="$align" />
    </xsl:param>
    
    <xsl:param name="decoration"><xsl:apply-templates mode="text-decoration" select="." /></xsl:param>
    
    <xsl:call-template name="texture-basic">
      <xsl:with-param name="k9-containers" select="$k9-containers"  />
      <xsl:with-param name="k9-contains" select="$k9-contains"  />
      <xsl:with-param name="k9-elem" select="$k9-elem"  />
      <xsl:with-param name="k9-format" select="$k9-format"  />
      <xsl:with-param name="k9-highlight" select="$k9-highlight"  />
      <xsl:with-param name="k9-texture" select="$k9-texture"  />
      <xsl:with-param name="k9-type" select="$k9-type" />
      <xsl:with-param name="k9-ui" select="$k9-ui" />
      <xsl:with-param name="id" select="$id"  />
      <xsl:with-param name="style" select="$style" />
      <xsl:with-param name="class" select="$class" />
      <xsl:with-param name="attributes" select="$attributes"  />
      <xsl:with-param name="shape" select="$background" />
      <xsl:with-param name="content" select="$content" />
      <xsl:with-param name="decoration" select="$decoration" />
    </xsl:call-template>
  </xsl:template>
  
</xsl:stylesheet>