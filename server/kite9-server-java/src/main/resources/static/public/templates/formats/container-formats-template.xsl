<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


  <xsl:import href="../back/back-template.xsl" />
  <xsl:import href="generic-formats-template.xsl" />

  <!-- 
    Provides some standard formats for containers.
    
    
    container
    container-fixed
  
  
   -->


 <!-- 
   
    -->
  <xsl:template name="formats-container" match="*[@k9-format='container']">
    <xsl:param name="content"><xsl:apply-templates /></xsl:param>
    <xsl:param name="k9-contains">connected</xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">container</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect insert autoconnect</xsl:param>
    <xsl:param name="k9-shape">
      <xsl:call-template name="back-basic">
        <xsl:with-param name="rounding" select="$k9-rounding" />
        <xsl:with-param name="highlight" select="$k9-highlight" />
      </xsl:call-template>  
    </xsl:param>

    <xsl:param name="k9-decoration">
      <xsl:apply-templates mode="decoration" />
    </xsl:param>

    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    
    <xsl:call-template name="formats-generic">
      <xsl:with-param name="pre" select="$k9-shape" />
      <xsl:with-param name="content" select="$content" />
      <xsl:with-param name="post" select="$k9-decoration" />
      
      <!-- standard params -->
      <xsl:with-param name="k9-contains" select="$k9-contains" />
      <xsl:with-param name="k9-elem" select="$k9-elem" />
      <xsl:with-param name="k9-format" select="$k9-format" />
      <xsl:with-param name="k9-palette" select="$k9-palette" />
      <xsl:with-param name="k9-texture" select="$k9-texture" />
      <xsl:with-param name="k9-ui" select="$k9-ui" />
      <xsl:with-param name="attributes" select="$attributes" />
      <xsl:with-param name="class" select="$class" />
    
    </xsl:call-template>
    
  </xsl:template>

  
  
    
  

  <!-- Container template where the container contents is in a specific order -->
  <xsl:template name="formats-container-fixed" match="//*[@k9-format='container-fixed']">
    <xsl:param name="k9-format">container-fixed</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect autoconnect cascade</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="layout"><xsl:value-of select="@layout"/></xsl:param>
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
      <xsl:if test="$layout">
        <xsl:attribute name="layout"><xsl:value-of select="$layout"></xsl:value-of></xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="@*" />
       
      <xsl:call-template name="back-basic">
        <xsl:with-param name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:with-param>
        <xsl:with-param name="k9-rounding"><xsl:value-of select="$k9-rounding" /></xsl:with-param>
       </xsl:call-template>
       
      <xsl:apply-templates />
     </g>
  </xsl:template>

</xsl:stylesheet>
