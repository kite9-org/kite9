<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../basic/basic-template.xsl" />   
  
  <xsl:template match="/" mode="diagram-script">
    import '/public/templates/basic/basic.js'
    import '/public/templates/integration-test/basic-integration.js'
  </xsl:template>
  
</xsl:stylesheet>