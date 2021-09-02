<xsl:stylesheet xmlns="http://www.w3.org/2000/svg" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl" 
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../formats/formats-shape.xsl"/>

  <xsl:template name="votes">
    <xsl:param name="count" select="@count"/>
    <xsl:if test="$count">
      <xsl:call-template name="formats-shape">
        <xsl:with-param name="k9-shape">
          <rect x="-5pt" y="-5pt" width="52pt" height="26pt" rx="8pt" ry="8pt" class="back"/>
          <text x="8pt" y="9pt" class="font-awesome">&#xf005;</text>
          <text x="30pt" y="9pt" class="count"><xsl:value-of select="$count" /></text>
        </xsl:with-param>
        <xsl:with-param name="k9-elem">votes</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>