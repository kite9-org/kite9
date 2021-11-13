<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="basic-components.xsl" />

  <xsl:template match="/" mode="diagram-element-css">
    <xsl:next-match />
    @import url('/public/templates/basic/basic-elements.css');
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    @import url('/public/templates/basic/basic-textures.css');
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-script">
    import '/public/templates/basic/basic.js';
  </xsl:template>

</xsl:stylesheet>
        
        
        

