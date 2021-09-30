<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">
 
  <xsl:template name='links-link-basic' match="*[@k9-format='link-basic']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style']" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="k9-containers"></xsl:param>
    <xsl:param name="k9-contains"></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">link-basic</xsl:param>
    <xsl:param name="k9-highlight">bar stroke</xsl:param>
    <xsl:param name="k9-palette">link</xsl:param>
    <xsl:param name="k9-texture">background</xsl:param>
    <xsl:param name="k9-ui">delete link cascade drop</xsl:param>
    
    <xsl:param name="shape">
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

    <xsl:param name="content">
    	<xsl:apply-templates />
    	<xsl:copy-of select="$body" />	
   	</xsl:param>
    <xsl:param name="decoration"><xsl:apply-templates mode="link-decoration" select="." /></xsl:param>
    
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
  
  <xsl:template match="/" mode="diagram-constants">
     <adl:constant name="link-template-uri" url="/public/templates/links/common-links.adl#l1" />
     <xsl:next-match />
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-palettes">
     <adl:palette contains="link" url="/public/templates/links/links-palette.adl" />
     <adl:palette contains="end" url="/public/templates/links/ends-palette.adl" />
     <xsl:next-match />
  </xsl:template>
  
  <xsl:template match="adl:link">
    <xsl:call-template name="links-link-basic" />
  </xsl:template>

</xsl:stylesheet>