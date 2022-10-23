<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:template name="links-port-basic">
		<xsl:param name="class" select="@class"/>
		<xsl:param name="style" select="@style"/>
		<xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style']" />
		<xsl:param name="id" select="@id" />
		
		<xsl:param name="k9-elem" select="local-name()" />
		<xsl:param name="k9-format">formats-shape</xsl:param>
		<xsl:param name="k9-highlight">fill grab</xsl:param>
		<xsl:param name="k9-texture">none</xsl:param>
		<xsl:param name="k9-ui">connect delete drag port direction insert orphan</xsl:param>
	    
	    <xsl:param name="shape">
			<rect x="-12" y="-12" width="24" height="24" />
	    </xsl:param>
    
	    <xsl:param name="content"><xsl:apply-templates /></xsl:param>
   	
   		<xsl:param name="decoration"><xsl:apply-templates mode="port-decoration" select="." /></xsl:param>
    
		<xsl:call-template name="texture-basic">
			<xsl:with-param name="k9-elem" select="$k9-elem"  />
			<xsl:with-param name="k9-format" select="$k9-format"  />
			<xsl:with-param name="k9-highlight" select="$k9-highlight"  />
			<xsl:with-param name="k9-texture" select="$k9-texture"  />
			<xsl:with-param name="k9-palette">port</xsl:with-param>
			<xsl:with-param name="k9-ui" select="$k9-ui" />
			<xsl:with-param name="id" select="$id"  />
			<xsl:with-param name="style" select="$style" />
			<xsl:with-param name="class" select="$class" />
			<xsl:with-param name="attributes" select="$attributes"  />
			<xsl:with-param name="shape" select="$shape" />
			<xsl:with-param name="content" select="$content" />
			<xsl:with-param name="decoration" select="$decoration" />
		</xsl:call-template>
	</xsl:template>


	<xsl:template match="adl:port">
		<xsl:call-template name="links-port-basic" />
	</xsl:template>


</xsl:stylesheet>