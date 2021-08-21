<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


  <xsl:import href="text-formats-template.xsl" />
  <xsl:import href="container-formats-template.xsl" />
<!--   <xsl:import href="image-formats-template.xsl"/> -->  



  <xsl:template match="*|text()" mode="decoration" />

</xsl:stylesheet>
