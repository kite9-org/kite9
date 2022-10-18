<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:template match="adl:box">
		<xsl:call-template name="formats-container">
	     	<xsl:with-param name="k9-rounding">5pt</xsl:with-param>	
      		<xsl:with-param name="k9-palette">connected</xsl:with-param>
   			<xsl:with-param name="k9-contains">port</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="adl:socket">
		<xsl:call-template name="links-port-basic">
			<xsl:with-param name="shape">
				<ellipse x="0" y="0" rx="10" ry="10" />
 			</xsl:with-param>
 			<xsl:with-param name="k9-texture">background</xsl:with-param>
 			<xsl:with-param name="k9-highlight">pulse</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="adl:socket[@class='square']">
		<xsl:call-template name="links-port-basic">
			<xsl:with-param name="shape">
				<rect x="-10" y="-10" width="20" height="20" />
 			</xsl:with-param>
 			<xsl:with-param name="k9-texture">background</xsl:with-param>
 			<xsl:with-param name="k9-highlight">pulse</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
 
</xsl:stylesheet>
        
        
        

