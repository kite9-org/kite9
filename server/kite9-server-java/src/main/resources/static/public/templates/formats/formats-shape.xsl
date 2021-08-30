<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:import href="../back/back-template.xsl" />
  
  <xsl:template name="formats-shape" match="*[@k9-format='shape']">
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">shape</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect insert autoconnect</xsl:param>
    
    <xsl:param name="depiction">
      <xsl:call-template name="back-basic">
        <xsl:with-param name="k9-elem">depiction</xsl:with-param>
        <xsl:with-param name="highlight"><xsl:value-of select="$k9-highlight" /></xsl:with-param>
      </xsl:call-template>
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates mode="shape-decoration" /></xsl:param>
    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    <xsl:param name="id" select="@id" />
    
    <g>      
      <xsl:copy-of select="$attributes" />      

      <xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>

      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class" /></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$id">
        <xsl:attribute name="id"><xsl:value-of select="$id" /></xsl:attribute>
      </xsl:if>
   
      <xsl:comment>content</xsl:comment> 
      <xsl:copy-of select="$depiction" />
      <xsl:comment>post</xsl:comment> 
      <xsl:copy-of select="$k9-decoration" />
    </g>
  </xsl:template>

</xsl:stylesheet>
