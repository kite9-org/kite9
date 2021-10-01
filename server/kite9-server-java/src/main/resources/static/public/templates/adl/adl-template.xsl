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
  
  <xsl:import href="../formats/formats-components.xsl" />
  <xsl:import href="../diagram/diagram-components.xsl" />
  <xsl:import href="../containers/containers-components.xsl"/>
  <xsl:import href="../labels/labels-components.xsl" />
  <xsl:import href="../grid/grid-components.xsl" />
  <xsl:import href="../links/links-components.xsl" />

  <!-- this rule matches and passes through any svg elements -->
  <xsl:template match="svg:*">
      <xsl:element name="{local-name()}">
          <xsl:copy-of select="@*" />
          <xsl:apply-templates />
      </xsl:element>
  </xsl:template>
  
  <xsl:template match="adl:textarea">
    <xsl:call-template name="formats-text-fixed">
      <xsl:with-param name="k9-texture">foreground</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  

</xsl:stylesheet>