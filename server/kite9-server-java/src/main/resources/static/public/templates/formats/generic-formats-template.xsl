<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
   
  <!-- 
    this is the basic format, called by all the others.  It defines the main formatting
    attributes / parameters that can be applied to a format.
   --> 
  <xsl:template name="formats-generic">
    <xsl:param name="pre" />
    <xsl:param name="content" />
    <xsl:param name="post" />

    <xsl:param name="k9-contains" />
    <xsl:param name="k9-elem" />
    <xsl:param name="k9-format" />
    <xsl:param name="k9-palette" />
    <xsl:param name="k9-texture" />
    <xsl:param name="k9-ui" />

    <xsl:param name="class" select="@class"/>
    <xsl:param name="id" select="@id"/>
    <xsl:param name="attributes" select="@*" />
    
  
    <g>      
      <xsl:copy-of select="$attributes" />      

      <xsl:attribute name="k9-contains"><xsl:value-of select="$k9-contains" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
      <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>

      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class" /></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$id">
        <xsl:attribute name="id"><xsl:value-of select="$id" /></xsl:attribute>
      </xsl:if>
   
      <xsl:comment>pre</xsl:comment> 
      <xsl:copy-of select="$pre" />
      <xsl:comment>content</xsl:comment> 
      <xsl:copy-of select="$content"/>
      <xsl:comment>post</xsl:comment> 
      <xsl:copy-of select="$post" />
    </g>
  
  </xsl:template> 

  
</xsl:stylesheet>