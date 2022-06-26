<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">  

  <xsl:template name="uml-divider">
    <path d="" pp:d="M 0 0 H [[$width]]" />      
  </xsl:template>

  <xsl:template name="uml-actor" match="adl:actor" mode="shape">
    <g>
      <xsl:attribute name="pp:style">transform: scale([[$width div 50]],[[$height div 50]]);</xsl:attribute>
      <ellipse cx="25" cy="10" rx="10" ry="10"/>
      <path
        d="M 10 20 
    			H 40
    			v 14 
    			h -5 
    			L 40 50 
    			h -14 
    			L 25 44 
    			L 24 50
    			h -14
    			L 15 34
    			h -5
    			z"/>
    </g>
  </xsl:template>

  <xsl:template name="uml-component" match="adl:component" mode="shape">
    <g>
      <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px);</xsl:attribute>
  		<path d="" pp:d="
  			M 5 0
  			H [[$width]]
  			V [[$height]]
  			H 5 
  			V 20
  			H 10
  			V 15
  			H 5
  			V 10
  			H 10
  			V 5
  			H 5
  			V 0
  			z" />
  		<rect x="0" y="5" width="10" height="5" />
  		<rect x="0" y="15" width="10" height="5" />
    </g>
  </xsl:template> 

  <xsl:template name="uml-note" match="adl:note" mode="shape">
    <g>
      <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px);</xsl:attribute>
  		<path d="" pp:d="
  			M 0 0
  			H [[$width - 10]]
  			L [[$width]] 10
  			V [[$height]]
  			H 0
  			V 0" />
  		<path d="" pp:d="
  			M [[$width - 10]] 0
  			v 10
  			h 10" />
    </g>
  </xsl:template> 

  <xsl:template name="uml-interface" match="adl:interface" mode="shape">
    <g>
      <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px);</xsl:attribute>
		  <ellipse cx="0" cy="0" rx="0" ry="0" pp:cx="[[$width div 2]]" pp:cy="[[$height div 2]]" pp:rx="[[$width div 2]]"  pp:ry ="[[$height div 2]]" />
    </g>
  </xsl:template> 

  <xsl:template name="uml-use-case" match="adl:use-case" mode="shape">
    <g>
      <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px);</xsl:attribute>
		  <ellipse cx="0" cy="0" rx="0" ry="0" pp:cx="[[$width div 2]]" pp:cy="[[$height div 2]]" pp:rx="[[$width div 2]]" pp:ry ="[[$height div 2]]" />
    </g>
  </xsl:template> 

  <xsl:template name="uml-package" match="adl:package" mode="shape">
    <g>
      <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px);</xsl:attribute>
  		<path
        p=""
  			pp:d="
  			M 0 0
  			L [[$width div 2 - 10]] 0
  			C [[$width div 2]] 0 [[$width div 2 + 10]] 10 [[$width div 2 + 20]] 10 
  			H [[$width]] 
  			V [[$height]]
  			H 0
  			Z
  		" />
    </g>
  </xsl:template> 

  <xsl:template name="uml-container" match="adl:container" mode="shape">
    <g>
      <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px);</xsl:attribute>
  		<rect x="0" y="5" width="0" height="0" pp:width="[[$width - 5]]" pp:height="[[$height - 5]]" />
  		<path 
        d=""
        pp:d="
  			M 0, 5 
  			l 5 -5
  			H [[$width]]
  			V [[$height - 5]]
  			l -5 5
  			V 5
  			H 0
  			z" />
  		<path 
        d=""
        pp:d="
  			M [[$width -5]] 5
  			l 5 -5" />
    </g>
  </xsl:template>
  
  
  
 <xsl:template match="
    adl:property | 
    adl:method">
    <xsl:call-template name="formats-text-fixed" />
  </xsl:template>
   
  <xsl:template match="
    adl:use-case | 
    adl:note">
    <xsl:call-template name="formats-text-shape-inline" />
  </xsl:template>

  <xsl:template match="
    adl:interface | 
    adl:actor">
    <xsl:call-template name="formats-text-shape-captioned">
      <xsl:with-param name="k9-texture">none</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="adl:uml-container | 
    adl:component |
    adl:package |
    adl:properties |
    adl:methods">
    <xsl:call-template name="formats-container" />
  </xsl:template>
  
  <xsl:template match="adl:class-name | adl:stereotype">
    <xsl:call-template name="formats-text-fixed" />
  </xsl:template>

  <xsl:template match="adl:class">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="content">
        <xsl:apply-templates select="adl:stereotype" />
        <xsl:apply-templates select="adl:class-name" />
        <xsl:call-template name="formats-container">
          <xsl:with-param name="k9-elem">properties</xsl:with-param>
          <xsl:with-param name="content">
            <xsl:apply-templates select="./adl:property" />
          </xsl:with-param>
          <xsl:with-param name="shape">
            <xsl:call-template name="uml-divider" />
          </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="formats-container">
          <xsl:with-param name="k9-elem">methods</xsl:with-param>
          <xsl:with-param name="content">
            <xsl:apply-templates select="./adl:method" />
          </xsl:with-param>
          <xsl:with-param name="shape">
            <xsl:call-template name="uml-divider" />
          </xsl:with-param>
        </xsl:call-template> 
      </xsl:with-param>
    </xsl:call-template>
  
  </xsl:template>

  <xsl:template match="/" mode="diagram-element-css" name="uml-diagram-element-css">
    <adl:css>@import url('/github/kite9-org/kite9/templates/uml/uml-elements.css');</adl:css>
  </xsl:template>

</xsl:stylesheet>