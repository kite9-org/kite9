<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="2.0">

  <xsl:import href="../../formats/formats-components.xsl" />
  
  <xsl:template match="/" mode="diagram-defs">
    <filter id="dropshadow" height="130%">
      <feGaussianBlur in="SourceAlpha" stdDeviation="4" />
      <feOffset dx="6" dy="6" />
      <feColorMatrix type="matrix" values="
            0 0 0 0 0
                0 0 0 0 0
                0 0 0 0 0
                0 0 0 .4 0" />
      <feMerge>
        <feMergeNode />
        <feMergeNode in="SourceGraphic" />
      </feMerge>
    </filter>
	<xsl:next-match />
  </xsl:template>
  
  <!-- 
  <template id="big-image">
    <image width="280pt" height="280pt" href="pre:#{@imgsrc}" />
    <title><contents optional="true"/></title>
  </template>
  
 
  <template id="image-artifact">
    <image href="pre:#{@imgsrc}" width="80pt" height="80pt" style="kite9-vertical-align: center; kite9-min-size: 80pt 80pt;" />
    <title><contents /></title>
  </template>
 
  
  <template id="celltext">
    <text-label style="kite9-type: text;">
      <svg:flowRegion>
        <svg:rect width="950pt" height="400pt" />
      </svg:flowRegion>
      <contents />
    </text-label>
  </template>
  
  <template id="bulleted" style="kite9-type: container; kite9-layout: right;" >
   	<text-label style="kite9-type: text; kite9-vertical-align: top; kite9-padding-right: 15pt;">
   	  &#8226; 
   	</text-label> 
    <text-label style="kite9-type: text; kite9-vertical-align: top;">
      <svg:flowRegion>
        <svg:rect width="950pt" height="400pt" />
      </svg:flowRegion>
      <contents />
    </text-label>
  </template> -->
 
  <!-- Steve McConnell Stuff -->


<!-- 
 
   <template id="leg">
   <back style='kite9-usage: decal; kite9-type: svg; '>
     <svg:rect x='0' y='0' width='#{$width}' height='#{$height}' style='fill: url(#risk-background); ' class="glyph-back" />
   </back>     
   <leg-text style="kite9-type: text; kite9-padding: 10pt; kite9-padding-top: 50pt; text-align: middle; ">
     <contents />
   </leg-text>
  </template>
 
 <plinth id="plinth" style="kite9-type: container;">
   <back style='kite9-usage: decal; kite9-type: svg; '>
     <svg:polygon points="0, #{$height} #{$width div 2},0 #{$width}, #{$height}" style='fill: url(#risk-background); ' class="glyph-back" />
   </back>
   <plinth-text style="kite9-type: text; kite9-padding: 10pt; kite9-padding-top: 15pt; "><contents /></plinth-text>
 </plinth>
 
  -->
 
 <!-- risk matrix -->
 
 <!--  
 <template id="symbol">
   <svg:g transform="translate(#{../@ox * $pt }, #{../@oy * $pt})">
    <svg:circle style="fill: #A0A0A0;  stroke: none" cx="6pt" cy="6pt" r="8pt" />
    <svg:text x=".35pt" y="1pt" class="symbol-text"><contents xpath="substring(@theChar,1, 1)" type="string"/></svg:text>
   </svg:g>
  </template>
  
  <template id="box">
  	<back style='kite9-usage: decal; kite9-type: svg; '>
      <svg:rect x='0' y='0' width='#{$width}' height='#{$height}' class="glyph-back" />
    </back>   
    <contents />
  </template>
  
  <template id='connection-label'>
    <svg:g class="container-label-front">
      <front style="kite9-type: text; ">
        <contents />
      </front>
    </svg:g>
  </template>
  
   -->
  
  <!-- singles for website article icons-->

  <xsl:template match="adl:diagram[contains(@class,'bg')]">
  	<xsl:param name="imageurl">
  		<xsl:choose>
  			<xsl:when test="@class = 'bg1'">/public/templates/risk-first/backgrounds/vecteezy-5.jpg</xsl:when>
  			<xsl:when test="@class = 'bg2'">/public/templates/risk-first/backgrounds/vecteezy-1.jpg</xsl:when>
  			<xsl:when test="@class = 'bg3'">/public/templates/risk-first/backgrounds/vecteezy-3.jpg</xsl:when>
  			<xsl:otherwise>/public/templates/risk-first/backgrounds/vecteezy-2.jpg</xsl:otherwise>
  		</xsl:choose>
  	</xsl:param>
  	
  	<xsl:call-template name="formats-container">
  	  <xsl:with-param name="k9-texture">none</xsl:with-param>
      <xsl:with-param name="shape">
	      <image x="0" y="0" width="100" height="100" pp:width="$width" pp:height="$height">
	      	<xsl:attribute name="xlink:href" select="$imageurl" />
	      </image>
	  </xsl:with-param>
	  <xsl:with-param name="content">
  	     <g filter="url(#dropshadow)">
    	  <xsl:apply-templates select="*" />
    	</g>
      </xsl:with-param>    
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="/" mode="diagram-element-css">
    <xsl:next-match />
    <adl:css>@import url('/public/templates/risk-first/site/site-elements.css');</adl:css>
  </xsl:template>
  
  <xsl:template match="adl:risk-first-logo">
  	<xsl:call-template name="formats-image" />
  </xsl:template>
  
   <xsl:template match="adl:page-title | adl:page-summary">
     <xsl:call-template name="formats-text-fixed" />
  </xsl:template>
  
   
  
</xsl:stylesheet>