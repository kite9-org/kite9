<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
          
  <xsl:template name="formats-image" match="*[@k9-format='image-fixed']">
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
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">image-fixed</xsl:param>
    <xsl:param name="k9-texture">none</xsl:param>
    <xsl:param name="k9-ui"></xsl:param>
    <xsl:param name="content">
      <image x="0" y="0">
        <xsl:attribute name="xlink:href"><xsl:value-of select="$href" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="$width" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="$height" /></xsl:attribute>
      </image>
    </xsl:param>
    <xsl:param name="back">
      <g>
        <xsl:attribute name="k9-elem">back</xsl:attribute>
        <rect x='0' y='0'>
          <xsl:attribute name="width"><xsl:value-of select="$width" /></xsl:attribute>
          <xsl:attribute name="height"><xsl:value-of select="$height" /></xsl:attribute>
        </rect>
      </g>
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates select="." mode="image-decoration" /></xsl:param>

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
   
      <xsl:comment>pre</xsl:comment> 
      <xsl:copy-of select="$back" />
      <xsl:comment>content</xsl:comment> 
      <xsl:copy-of select="$content"/>
      <xsl:comment>post</xsl:comment> 
      <xsl:copy-of select="$k9-decoration" />
    </g>
  </xsl:template>
</xsl:stylesheet>