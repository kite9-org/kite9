<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
 
  <xsl:template name='links-link-basic' match="*[@k9-format='link-basic']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style']" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="content"><xsl:apply-templates /></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">link-basic</xsl:param>
    <xsl:param name="k9-palette">link</xsl:param>
    <xsl:param name="k9-highlight">bar stroke</xsl:param>
    <xsl:param name="k9-texture">foreground</xsl:param>
    <xsl:param name="k9-ui">delete link cascade drop</xsl:param>
    
    <xsl:param name="k9-shape">
   		<g k9-elem="link-grab">
        <xsl:attribute name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:attribute>
        <path pp:d="$path" d="" k9-animate="link"/>
      </g>
    </xsl:param>
    
    <xsl:param name="body">
      <g k9-elem="link-body">
        <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
        <path k9-animate="link" d="" pp:d="$path">
          <xsl:attribute name="marker-start">url(#<xsl:value-of select="adl:from/@shape" />-start-marker)</xsl:attribute>
          <xsl:attribute name="marker-end">url(#<xsl:value-of select="adl:to/@shape" />-end-marker)</xsl:attribute>
        </path>     
      </g>
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates mode="link-decoration" select="." /></xsl:param>
    
    
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
      
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
      <xsl:comment>body</xsl:comment> 
      <xsl:copy-of select="$body"/>
      <xsl:comment>decoration</xsl:comment> 
      <xsl:copy-of select="$k9-decoration" />
    </g>
  </xsl:template>
</xsl:stylesheet>