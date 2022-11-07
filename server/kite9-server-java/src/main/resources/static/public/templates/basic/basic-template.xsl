<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl-all-components.xsl" />
  <xsl:import href="basic-components.xsl" /> 

  <xsl:template match="/" mode="diagram-element-css">
    <xsl:call-template name="adl-diagram-element-css" />
    <adl:css>@import url('/public/templates/basic/basic-elements.css');</adl:css>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    <adl:css>@import url('/public/templates/basic/basic-textures.css');</adl:css>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-font-families">
    <adl:font-family weights="100 200 300 400 500 600 700 800 900" styles="regular italic">Metropolis</adl:font-family>
    <adl:font-family weights="400 500 700 900" styles="regular">Chirp</adl:font-family>
    <adl:font-family weights="400 900">Font Awesome 5 Free</adl:font-family>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-script">
    import '/public/templates/basic/basic.js'
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-constants">
  	<xsl:call-template name="adl-diagram-constants" />
  </xsl:template>

  <xsl:template match="/" mode="diagram-defs">
  	<xsl:call-template name="adl-diagram-defs" />
  </xsl:template> 
  
  <xsl:template match="/" mode="diagram-palettes">
    <adl:palette url="/public/templates/basic/palette1.adl" />
    <adl:palette url="/public/templates/basic/palette2.adl" />
  	<xsl:call-template name="adl-diagram-palettes" />
  </xsl:template>
  
</xsl:stylesheet>
        
        
        

