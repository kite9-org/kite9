<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
     
  <xsl:import href="../../formats/formats-template.xsl" />
     
  <xsl:template match="adl:planner">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-contains">planner</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:planner-cell">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-containers">planner</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
</xsl:stylesheet>