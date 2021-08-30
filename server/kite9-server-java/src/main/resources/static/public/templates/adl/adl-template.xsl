<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">
        
        
  <!-- 
    This common stylesheet brings together everything needed to transform simple
    ADL.
   -->    
  
  <xsl:import href="../back/back-template.xsl" />
  <xsl:import href="../formats/formats-template.xsl" />
  <xsl:import href="../diagram/diagram-template.xsl" />
  <xsl:import href="../containers/containers-template.xsl"/>
  <xsl:import href="../labels/labels-template.xsl" />
  <xsl:import href="../grid/grid-template.xsl" />
  <xsl:import href="../links/links-template.xsl" />
  <xsl:import href="../links/links-markers.xsl" />
  <xsl:import href="../links/links-terminators.xsl" />

  <!-- this rule matches and passes through any svg elements -->
  <xsl:template match="svg:*">
      <xsl:element name="{local-name()}">
          <xsl:copy-of select="@*" />
          <xsl:apply-templates />
      </xsl:element>
  </xsl:template>
  
  <!-- this rule is the default for adl elements -->
  <!-- <xsl:template match="adl:*">
      <g>
          <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
          <xsl:copy-of select="@*" />
          <xsl:apply-templates />
      </g>
  </xsl:template>  -->
  
  <xsl:template match="adl:link">
    <xsl:call-template name="links-basic" />
  </xsl:template>

  <xsl:template match="adl:align">
    <xsl:call-template name="links-align" />
  </xsl:template>
  
  <xsl:template match="adl:from|adl:to">
    <xsl:call-template name="null-terminator" />
  </xsl:template>
  
  <xsl:template match="adl:diagram">
    <xsl:call-template name="diagram-basic" />
  </xsl:template>

  <xsl:template match="adl:container">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">10pt</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:label">
    <xsl:call-template name="labels-basic" />
  </xsl:template>
  
  <xsl:template match="adl:table">
    <xsl:call-template name="grid-table-basic" />
  </xsl:template>
  
  <xsl:template match="adl:cell">
    <xsl:call-template name="grid-cell-basic" />
  </xsl:template>
  
  <xsl:template match="adl:group">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">10pt</xsl:with-param>
      <xsl:with-param name="k9-texture">none</xsl:with-param>
      <xsl:with-param name="k9-highlight">pulse stroke</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
   
  <xsl:template match="adl:hub">
    <xsl:call-template name="links-hub" />
  </xsl:template>


</xsl:stylesheet>