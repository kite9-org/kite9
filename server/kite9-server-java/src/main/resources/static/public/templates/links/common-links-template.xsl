<xsl:stylesheet xmlns="http://www.w3.org/2000/svg" xmlns:svg="http://www.w3.org/2000/svg"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:adl="http://www.kite9.org/schema/adl"
 xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


 <xsl:import href="/public/templates/links/links-markers.xsl"/>
 <xsl:import href="/public/templates/links/links-terminators.xsl"/>
 <xsl:import href="/public/templates/links/links-template.xsl"/>


 <xsl:template match="adl:link">
  <xsl:call-template name="links-basic" />
 </xsl:template>

 <xsl:template match="adl:from|adl:to">
   <xsl:call-template name="null-terminator" />
 </xsl:template>


</xsl:stylesheet> 