<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl-all-components.xsl" />
  <xsl:import href="tweet-components.xsl" /> 

  <xsl:template match="/" mode="diagram-element-css">
    <xsl:call-template name="adl-diagram-element-css" />
    <adl:css>@import url('/public/templates/tweet/tweet-elements.css');</adl:css>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    <adl:css>@import url('/public/templates/tweet/tweet-textures.css');</adl:css>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-script">
    import '/public/templates/tweet/tweet.js';
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-constants">
  	<xsl:call-template name="adl-diagram-constants" />
  </xsl:template>

  <xsl:template match="/" mode="diagram-defs">
  	<xsl:call-template name="adl-diagram-defs" />
  </xsl:template> 
  
</xsl:stylesheet>
        
        
        

