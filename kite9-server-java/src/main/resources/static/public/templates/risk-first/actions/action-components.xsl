<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  <xsl:template name="action" match="adl:action">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-ui">size drag delete align connect autoconnect edit vote</xsl:with-param>
      <xsl:with-param name="shape">
        <g k9-texture="background" k9-highlight="pulse" >
          <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px)</xsl:attribute>
          <polygon points="" pp:points="
            0 0, 
            [[$width - (15 * $pt)]] 0,
            [[$width]] [[$height div 2]],
            [[$width - (15 * $pt)]] [[$height]],
            0 [[$height]]"  />
        </g>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="glyph-circle" match="adl:glyph[contains(@class, 'circle')]">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-elem">glyph</xsl:with-param>
      <xsl:with-param name="k9-ui">size drag delete align connect autoconnect edit vote</xsl:with-param>
      <xsl:with-param name="k9-texture">background</xsl:with-param>
      <xsl:with-param name="shape">
      	<ellipse cx="0" cy="0" rx="0" ry="0">
	      <xsl:attribute name="pp:cx">$x + $width div 2</xsl:attribute>
	      <xsl:attribute name="pp:cy">$y + $height div 2</xsl:attribute>
	      <xsl:attribute name="pp:rx">$width div 2</xsl:attribute>
	      <xsl:attribute name="pp:ry">$height div 2</xsl:attribute>
	    </ellipse>
    </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
    
  <xsl:template name="glyph-rect" match="adl:glyph[contains(@class, 'rect')]">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-elem">glyph</xsl:with-param>
      <xsl:with-param name="k9-ui">size drag delete align connect autoconnect edit vote</xsl:with-param>
      <xsl:with-param name="k9-texture">background</xsl:with-param>
      <xsl:with-param name="k9-rounding">0</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="glyph" match="adl:glyph">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-elem">glyph</xsl:with-param>
      <xsl:with-param name="k9-ui">size drag delete align connect autoconnect edit vote</xsl:with-param>
      <xsl:with-param name="k9-texture">background</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="risk-action-left" match="adl:action[contains(@class, 'left')]" priority="5">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-ui">size drag delete align connect autoconnect edit vote</xsl:with-param>
      <xsl:with-param name="shape">
        <g k9-texture="background" k9-highlight="pulse" >
          <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px)</xsl:attribute>
          <polygon points="" pp:points="
            0 [[$height div 2]], 
            [[15 * $pt]] 0, 
            [[$width]] 0, 
            [[$width]] [[$height]], 
            [[15 * $pt]] [[$height]],
            0 [[$height div 2]]" />
        </g>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="actions-risk-first-diagram-element-css">
    <adl:css>@import url('/public/templates/risk-first/actions/action-elements.css');</adl:css>
  </xsl:template>
</xsl:stylesheet>