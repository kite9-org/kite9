<xsl:stylesheet
		xmlns="http://www.w3.org/2000/svg"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:pp="http://www.kite9.org/schema/post-processor"
		xmlns:adl="http://www.kite9.org/schema/adl"
		version="1.0">



	<xsl:template name="back-round-rect">
		<xsl:param name="k9-rounding">0pt</xsl:param>
		<xsl:param name="k9-indicator">pulse</xsl:param>
		<g>
			<xsl:attribute name="k9-elem">back</xsl:attribute>
			<xsl:attribute name="k9-indicator"><xsl:value-of select="$k9-indicator" /></xsl:attribute>
			<rect x="0" y="0" width="0" height="0">
				<xsl:attribute name="rx"><xsl:value-of select="$k9-rounding" /></xsl:attribute>
				<xsl:attribute name="ry"><xsl:value-of select="$k9-rounding" /></xsl:attribute>
				<xsl:attribute name="pp:width">$width</xsl:attribute>
				<xsl:attribute name="pp:height">$height</xsl:attribute>
			</rect>
		</g>
	</xsl:template>

	<xsl:template name="back-ellipse">
		<g>
			<xsl:attribute name="k9-elem">back</xsl:attribute>
			<xsl:attribute name="k9-indicator">pulse</xsl:attribute>
			<ellipse pp:cx="$width div 2" pp:cy="$height div 2" pp:rx="$width div 2" pp:ry="$height div 2" />
		</g>
	</xsl:template>

</xsl:stylesheet>