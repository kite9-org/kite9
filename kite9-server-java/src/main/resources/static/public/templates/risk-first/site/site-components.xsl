<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:svg="http://www.w3.org/2000/svg" 
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:template name="site-risk-first-diagram-defs">
		<filter id="dropshadow" height="130%">
			<feGaussianBlur in="SourceAlpha" stdDeviation="4" />
			<feOffset dx="6" dy="6" />
			<feColorMatrix type="matrix"
				values="
            0 0 0 0 0
                0 0 0 0 0
                0 0 0 0 0
                0 0 0 .4 0" />
			<feMerge>
				<feMergeNode />
				<feMergeNode in="SourceGraphic" />
			</feMerge>
		</filter>
	</xsl:template>

	<xsl:template match="adl:big-image">
		<xsl:call-template name="formats-image">
			<xsl:with-param name="href" select="@imgsrc" />
			<xsl:with-param name="width">
				200pt
			</xsl:with-param>
			<xsl:with-param name="height">
				200pt
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="adl:celltext">
		<xsl:call-template name="formats-text-fixed" />
	</xsl:template>
	
	<xsl:template match="adl:tick">
		<xsl:call-template name="formats-image-fixed">
			<xsl:with-param name="k9-elem">image</xsl:with-param>
			<xsl:with-param name="href">/public/templates/risk-first/posts/tick.svg</xsl:with-param>
			<xsl:with-param name="width">30pt</xsl:with-param>
			<xsl:with-param name="height">30pt</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="risk-point"
		match="*[@k9-shape='risk-point']" mode="shape" priority="2">
		<ellipse cx="0" cy="0" rx="15" ry="15" fill="#ddd">
			<xsl:attribute name="pp:cx">$x + $width div 2</xsl:attribute>
			<xsl:attribute name="pp:cy">$y + $height div 2</xsl:attribute>
		</ellipse>
	</xsl:template>

	<xsl:template name="site-risk-first-diagram-element-css">
		<adl:css>@import
			url('/public/templates/risk-first/site/site-elements.css');</adl:css>
	</xsl:template>

	<xsl:template match="adl:risk-first-logo">
		<xsl:call-template name="formats-image" />
	</xsl:template>

	<xsl:template match="adl:page-title | adl:page-summary">
		<xsl:call-template name="formats-text-fixed" />
	</xsl:template>

	<!--  steve mcconnel stuff -->

   <xsl:template match="adl:leg">
   		<xsl:call-template name="formats-text-shape-inline"> 
     	</xsl:call-template>
  	</xsl:template>
 
    <xsl:template match="adl:plinth">
   		<xsl:call-template name="formats-text-shape-inline"> 
     		<xsl:with-param name="shape">triangle</xsl:with-param>
     	</xsl:call-template>
  	</xsl:template>
  	
  	<xsl:template match="adl:cloud">
   		<xsl:call-template name="formats-text-fixed"> 
     		<xsl:with-param name="shape">
     		     <svg:polygon points="0, 100 50,0 100, 100" style='fill: url(#risk-background); ' class="glyph-back" />
     		</xsl:with-param>
     	</xsl:call-template>
  	</xsl:template>
 
  <xsl:template name="shape-triangle" match="*[@k9-shape='triangle']" mode="shape">
    <svg:polygon points="0, 100 50,0 100, 100" style='fill: url(#risk-background); ' class="glyph-back" />
  </xsl:template> 

</xsl:stylesheet>