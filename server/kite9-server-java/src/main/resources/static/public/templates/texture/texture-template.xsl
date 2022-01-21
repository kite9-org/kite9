<xsl:stylesheet
		xmlns="http://www.w3.org/2000/svg"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:pp="http://www.kite9.org/schema/post-processor"
		xmlns:adl="http://www.kite9.org/schema/adl"
		version="1.0">

  <!--
    Provides the <g> for a user-defined element which will have a
    texture. Can be used for text or background shapes.  
    
    Also supports style, class, id and other k9-* UI properties.  
    
    shape, content and decoration variables carry 3 layers of svg which this element
    will contain
  -->
  
	<xsl:template name="texture-basic">
		<xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-texture">default</xsl:param>
    <xsl:param name="k9-elem" select="local-name()"/>
    
    <xsl:param name="style"></xsl:param>
    <xsl:param name="class"></xsl:param>
    <xsl:param name="attributes"></xsl:param>
    
    <!-- UI parameters - used if an ID is present -->   
    <xsl:param name="k9-contains"></xsl:param>
    <xsl:param name="k9-containers"></xsl:param>
    <xsl:param name="id"></xsl:param>
    <xsl:param name="k9-format"></xsl:param>
    <xsl:param name="k9-palette"></xsl:param>
    <xsl:param name="k9-ui"></xsl:param>
    <xsl:param name="k9-child"></xsl:param>

    <!--  these specify the content of the element -->
    <xsl:param name="shape"></xsl:param>
    <xsl:param name="content"><xsl:apply-templates /></xsl:param>
    <xsl:param name="decoration"></xsl:param>
    
		<g>
      <xsl:copy-of select="$attributes" />      
      
      <xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>

      <xsl:if test="$k9-format">
        <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$k9-texture">
 			  <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$class">
        <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$style">
        <xsl:attribute name="style"><xsl:value-of select="$style"/></xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$id">
        <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
      
        <xsl:if test="$k9-contains">
          <xsl:attribute name="k9-contains"><xsl:value-of select="$k9-contains" /></xsl:attribute>
        </xsl:if>

        <xsl:if test="$k9-containers">
          <xsl:attribute name="k9-containers"><xsl:value-of select="$k9-containers" /></xsl:attribute>
        </xsl:if>
        
        <xsl:if test="$k9-palette">
          <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
        </xsl:if>

        <xsl:if test="$k9-ui">
          <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
        </xsl:if>
        
        <xsl:if test="$k9-child">
          <xsl:attribute name="k9-child"><xsl:value-of select="$k9-child" /></xsl:attribute>
        </xsl:if>
      </xsl:if>
      
      <xsl:choose>
        <xsl:when test="$shape">
          <!-- in this case, we need to add the highlight to the shape, if there is one -->
          <g>
            <xsl:if test="$k9-highlight">
              <xsl:attribute name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:attribute>
            </xsl:if>
            <xsl:comment>shape</xsl:comment> 
            <xsl:copy-of select="$shape" />
          </g>
          <xsl:if test="$content">
            <g>
              <xsl:comment>content</xsl:comment> 
              <xsl:copy-of select="$content"/>
            </g>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
           <!-- add the highlight to the content -->
           <xsl:if test="$content">
            <g>
              <xsl:if test="$k9-highlight">
                <xsl:attribute name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:attribute>
              </xsl:if>
              <xsl:comment>content</xsl:comment> 
              <xsl:copy-of select="$content"/>
            </g>
           </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    
      <xsl:if test="$decoration">
        <xsl:comment>decoration</xsl:comment> 
        <xsl:copy-of select="$decoration" />
      </xsl:if>
    </g>

	</xsl:template>
  
  
</xsl:stylesheet>