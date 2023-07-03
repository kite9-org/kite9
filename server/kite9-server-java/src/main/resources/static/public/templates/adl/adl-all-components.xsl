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
  
  <xsl:import href="../texture/texture-components.xsl" />
  <xsl:import href="../shape/shape-components.xsl" />
  <xsl:import href="../formats/formats-components.xsl" />
  <xsl:import href="../containers/containers-components.xsl"/>
  <xsl:import href="../labels/labels-components.xsl" />
  <xsl:import href="../grid/grid-components.xsl" />
  <xsl:import href="../links/links-components.xsl" />
  <xsl:import href="../textarea/textarea-components.xsl" /> 
  <xsl:import href="../highlight/highlight-components.xsl" />
  <xsl:import href="../diagram/diagram-components.xsl" />

  <!-- this rule matches and passes through any svg elements -->
  <xsl:template match="svg:*">
      <xsl:element name="{local-name()}">
          <xsl:copy-of select="@*" />
          <xsl:apply-templates />
      </xsl:element>
  </xsl:template> 
   
  <xsl:template name="adl-diagram-element-css">
  	<xsl:call-template name="formats-diagram-element-css" />
  	<xsl:call-template name="diagram-diagram-element-css" />
  	<xsl:call-template name="containers-diagram-element-css" />
  	<xsl:call-template name="labels-diagram-element-css" />
  	<xsl:call-template name="grid-diagram-element-css" />
  	<xsl:call-template name="links-diagram-element-css" />
  	<xsl:call-template name="textarea-diagram-element-css" />
  	<xsl:call-template name="highlight-diagram-element-css" />
  </xsl:template>
  
  <xsl:template name="adl-diagram-constants">
  	<xsl:call-template name="links-diagram-constants" />
  	<xsl:call-template name="labels-diagram-constants" />
  	<xsl:call-template name="grid-diagram-constants" />
  </xsl:template>
  
  <xsl:template name="adl-diagram-palettes">
  	<xsl:call-template name="links-diagram-palettes" />
  	<xsl:call-template name="labels-diagram-palettes" />
  	<xsl:call-template name="containers-diagram-palettes" />
  	<xsl:call-template name="grid-diagram-palettes" />
  </xsl:template>
  
  <xsl:template name="adl-diagram-defs">
  	<xsl:call-template name="links-diagram-defs" />
  	<xsl:call-template name="highlight-diagram-defs" />
  </xsl:template>
 
</xsl:stylesheet>