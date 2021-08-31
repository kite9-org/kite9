<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  <xsl:import href="../adl/adl-template.xsl"/>

  <xsl:template match="adl:font-awesome" >
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="content">
        <text>&#xf005;</text>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="/">
    <xsl:call-template name="diagram-root-svg">
      <xsl:with-param name="css">
        @import url('/public/templates/font-awesome/font-awesome.css');
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>