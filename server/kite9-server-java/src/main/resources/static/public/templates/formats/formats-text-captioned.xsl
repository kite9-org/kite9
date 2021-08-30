<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

<xsl:import href="../back/back-template.xsl" />
  
  <xsl:template name="formats-text-captioned" match="*[@k9-format='text-captioned']">
    <xsl:param name="text"><text><xsl:value-of select="text()" /></text></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-captioned</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag delete</xsl:param>
    
    <xsl:param name="k9-shape">
      <xsl:call-template name="back-basic">
        <xsl:with-param name="highlight" select="$k9-highlight" />
        <xsl:with-param name="shape">
          <xsl:call-template name="back-round-rect">
            <xsl:with-param name="rounding" select="$k9-rounding" />
          </xsl:call-template>
        </xsl:with-param> 
      </xsl:call-template>  
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates mode="text-decoration" /></xsl:param>
    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    <xsl:param name="id" select="@id" />
    
    <xsl:param name="depiction">
      <xsl:variable name="shape">
        <xsl:apply-templates mode="shape" select=".">
          <xsl:with-param name="rounding" select="$k9-rounding" />
        </xsl:apply-templates>
      </xsl:variable>
    
      <g>
        <xsl:attribute name="k9-elem">depiction</xsl:attribute>
        <xsl:attribute name="k9-ui">connect autoconnect align</xsl:attribute>
        <xsl:attribute name="id"><xsl:value-of select="$id" />@dep</xsl:attribute>
        <g>
          <xsl:attribute name="k9-highlight">pulse</xsl:attribute>
          <xsl:copy-of select="$shape" />
        </g>
      </g>
    </xsl:param>
        
    <xsl:param name="align">
    <!--  <g k9-elem="shape-align">
        <g k9-elem="from">
          <xsl:attribute name="reference"><xsl:value-of select="$id" />@dep</xsl:attribute>
        </g> 
        <g k9-elem="to">
          <xsl:attribute name="reference"><xsl:value-of select="$id" />@caption</xsl:attribute>   
        </g>
      </g>  -->
    </xsl:param>
    
    
    <xsl:param name="caption">
      <g k9-elem="caption">
        <xsl:attribute name="id"><xsl:value-of select="$id" />@caption</xsl:attribute>   
        <xsl:attribute name="k9-ui">edit</xsl:attribute>
        <g>
          <xsl:attribute name="k9-highlight">pulse</xsl:attribute>
          <xsl:copy-of select="$text"/>
        </g>
      </g>
    </xsl:param>
    
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
      <xsl:copy-of select="$k9-shape" />
      <xsl:comment>content</xsl:comment> 
      <xsl:copy-of select="$depiction" />
      <xsl:copy-of select="$caption" />
      <xsl:comment>align</xsl:comment> 
      <g k9-elem="shape-align">
        <g k9-elem="from">
          <xsl:attribute name="reference"><xsl:value-of select="$id" />@dep</xsl:attribute>
        </g> 
        <g k9-elem="to">
          <xsl:attribute name="reference"><xsl:value-of select="$id" />@caption</xsl:attribute>   
        </g>
      </g>
      <xsl:copy-of select="$align" />
      
      
      <xsl:comment>post</xsl:comment> 
      <xsl:copy-of select="$k9-decoration" />
    </g>
  </xsl:template>

</xsl:stylesheet>
