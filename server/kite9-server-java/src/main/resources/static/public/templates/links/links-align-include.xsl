<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
 
  <xsl:template name='links-link-align' match="*[@k9-format='link-align']">
    <xsl:param name="id" select="@id" />

    <xsl:param name="content"><xsl:apply-templates /></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">link-basic</xsl:param>
    <xsl:param name="k9-ui">delete link cascade drop</xsl:param>
    
    <xsl:param name="k9-shape">
   		<g k9-elem="link-grab">
        <xsl:attribute name="k9-highlight">pulse</xsl:attribute>
        <path pp:d="$path" d="" k9-animate="link"/>
      </g>
    </xsl:param>
    
    <xsl:param name="body">
      <g k9-elem="align-body">
        <xsl:attribute name="k9-highlight">stroke</xsl:attribute>
        <path k9-animate="link" d="" pp:d="$path" />
      </g>
    </xsl:param>
      
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
 
      <xsl:if test="$id">
        <xsl:attribute name="id"><xsl:value-of select="$id" /></xsl:attribute>
      </xsl:if>

	    <xsl:comment>shape</xsl:comment> 
      <xsl:copy-of select="$k9-shape" />
      <xsl:comment>content</xsl:comment>
      <xsl:copy-of select="$content" />
      <xsl:comment>body</xsl:comment> 
      <xsl:copy-of select="$body"/>
    </g>    
  </xsl:template>
  
  <xsl:template match="adl:align">
    <xsl:call-template name="links-link-align">
      <xsl:with-param name="content">
        <g k9-elem="from" reference="{./adl:from/@reference}" />
        <g k9-elem="to" reference="{adl:to/@reference}" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
</xsl:stylesheet>