<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl-all-components.xsl" />
  <xsl:import href="tweet-components.xsl" /> 
  <xsl:import href="card-components.xsl" /> 

  <xsl:template match="/" mode="diagram-element-css">
    <xsl:call-template name="adl-diagram-element-css" />
    <adl:css>@import url('/github/kite9-org/kite9/templates/tweet/tweet-elements.css');</adl:css>
    <adl:css>@import url('/github/kite9-org/kite9/templates/tweet/card-elements.css');</adl:css>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-texture-css">
    <adl:css>@import url('/github/kite9-org/kite9/templates/tweet/tweet-textures.css');</adl:css>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-script">
    import '/github/kite9-org/kite9/templates/tweet/tweet.js';
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-constants">
  	<xsl:call-template name="adl-diagram-constants" />
  </xsl:template>

  <xsl:template match="/" mode="diagram-defs">
  	<xsl:call-template name="adl-diagram-defs" />
  	<xsl:call-template name="twitter-defs" />
  </xsl:template> 
  
</xsl:stylesheet>
        
        
        

