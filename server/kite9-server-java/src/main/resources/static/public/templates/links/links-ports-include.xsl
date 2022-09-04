<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:attribute-set name="ports">
		<xsl:attribute name="k9-texture">none</xsl:attribute>
		<xsl:attribute name="k9-ui">connect delete fill size</xsl:attribute>
		<xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
	</xsl:attribute-set>

	<xsl:template name="links-port-basic">
		<g xsl:use-attribute-sets="ports">
			<xsl:copy-of select="@*" />
			<g>
				<xsl:attribute name="k9-highlight">fill grab</xsl:attribute>
				<rect x="-8" y="-8" width="16" height="16" />
			</g>
		</g>
	</xsl:template>


	<xsl:template match="adl:port">
		<xsl:call-template name="links-port-basic" />
	</xsl:template>


</xsl:stylesheet>