<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">
      
  <xsl:template match="adl:container">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">10pt</xsl:with-param>
      <xsl:with-param name="k9-contains">connected port label terminator</xsl:with-param>
      <xsl:with-param name="k9-palette">connected</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete align connect insert autoconnect layout label fill stroke size align</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:group">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-rounding">10pt</xsl:with-param>
      <xsl:with-param name="k9-texture">none</xsl:with-param>
      <xsl:with-param name="k9-contains">connected label</xsl:with-param>
      <xsl:with-param name="k9-palette">connected</xsl:with-param>
      <xsl:with-param name="k9-highlight">pulse stroke</xsl:with-param>
      <xsl:with-param name="k9-ui">drag delete align insert autoconnect layout label fill stroke size align</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
   
  <xsl:template name="containers-diagram-element-css">
    <adl:css>@import url('/public/templates/containers/containers-elements.css');</adl:css>
  </xsl:template>
  
  <xsl:template name="containers-diagram-palettes">
     <adl:palette url="/public/templates/containers/palette.adl" />
  </xsl:template>
  
  <xsl:template name="containers-diagram-defs">
      <defs id="container-indicators">
		<filter id="container-indicator-color">
		    <feColorMatrix
		      type="matrix"
		      values="0 1.0 0 0 0
		              0 1.0 0 0 0
		              0 0.6 1 0 0
		              0 0 0 1 0 "/>
		  </filter>
      </defs>
  </xsl:template>
    
</xsl:stylesheet>
