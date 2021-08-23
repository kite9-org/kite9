<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  
	<xsl:template mode="shape" match="adl:card" priority="2">
		<path xmlns="http://www.w3.org/2000/svg"
			d="M80,34.5H30c-0.1,0-0.3,0.1-0.4,0.1l-10,10c-0.1,0.1-0.1,0.2-0.1,0.4v20c0,0.3,0.2,0.5,0.5,0.5h60c0.3,0,0.5-0.2,0.5-0.5V35  C80.5,34.7,80.3,34.5,80,34.5z M79.5,64.5h-59V45.2l9.7-9.7h49.3V64.5z" />
	</xsl:template>

	<xsl:template mode="shape" match="adl:data" priority="2">
		<path xmlns="http://www.w3.org/2000/svg"
			d="M90,29.5H30c-0.2,0-0.4,0.1-0.4,0.3l-20,40c-0.2,0.3,0.1,0.7,0.4,0.7h60c0.2,0,0.4-0.1,0.4-0.3l20-40  C90.6,29.9,90.4,29.5,90,29.5z M69.7,69.5H10.8l19.5-39h58.9L69.7,69.5z" />
	</xsl:template>

	<xsl:template mode="shape" match="adl:process" priority="2">
    <g>
      <rect x="0" y="0" width="0" height="0" pp:width="$width" pp:height="$height" />
    </g>
	</xsl:template>
 
	<xsl:template mode="shape" match="adl:document">
		<path d=""
			pp:d="
       concat(
			   ' M 0 0',
         ' V ', ($height - 3),
			   ' q ',($width div 4), ' -6 ',($width div 2), ' 0',
         ' q ',($width div 4), ' 6 ', ($width div 2), ' 0',
			   ' V 0',
			   ' H 0')" />
	</xsl:template>
  
  
  <xsl:template mode="shape" match="adl:decision">
    <path d=""  pp:d="concat(
      ' M ',$width div 2,' 0',
      ' L ', $width,' ',$height div 2,
      ' L ', $width div 2,' ',$height,
      ' L 0 ',$height div 2,
      ' z')" />
  </xsl:template>
  
<!-- 
	<xsl:template mode="shape" match="adl:delay">
		<path
			d="
			M 0 0
			V #{$height}
			H #{$width - 20}
			Q #{$width} #{$height} #{$width} #{$height div 2}
			Q #{$width} 0 #{$width - 20} 0
			H 0
		" />
	</xsl:template>

	

	<xsl:template mode="shape" match="adl:manual">
		<path d="
			M 0 0
			H #{$width}
			L #{$width -5} #{$height}
			H 5
			z" />
	</xsl:template>

	<xsl:template mode="shape" match="adl:terminator">
		<path
			d="
			M 20 0
			Q 0 0 0 #{$height div 2}
			Q 0 #{$height} 20 #{$height} 
			H #{$width - 20}
			Q #{$width} #{$height} #{$width} #{$height div 2}
			Q #{$width} 0 #{$width - 20} 0
			z
		" />
	</xsl:template>

	<xsl:template mode="shape" match="adl:input">
		<path d="
			M 0 10
			L #{$width} 0
			V #{$height}
			H 0
			z" />
	</xsl:template>


	<xsl:template mode="shape" match="adl:database">
		<path d="
			M 0 5
			L 0 #{$height - 5}
			Q 0 #{$height} #{$width div 2} #{$height}
			Q #{$width} #{$height} #{$width} #{$height - 5}
			L #{$width} #{$height - 5}
			L #{$width} 5" />
		<ellipse cx="#{$width div 2}" cy="5" rx="#{$width div 2}" ry="5" />
	</xsl:template>

	<xsl:template mode="shape" match="adl:preparation">
		<path d="
			M 10 0
			H #{$width - 10}
			L #{$width} #{$height div 2}
			L #{$width - 10} #{$height}
			H 10
			L 0 #{$height div 2}
			z" />
	</xsl:template>
	<xsl:template mode="shape" match="adl:internal">
		<rect x="0" y="0" width="#{$width}" height="#{$height}" />
		<path d="M 0 5 H #{$width}" />
		<path d="M 5 0 V #{$height}" />
	</xsl:template>

	<xsl:template mode="shape" match="adl:off-page">
		<path d="
			M 0 0
			H #{$width}
			V #{$height - 10}
			L #{$width div 2} #{$height}
			L 0 #{$height - 10}
			z" />
	</xsl:template>

	<xsl:template mode="shape" match="adl:direct">
		<path d="
			M 5 0
			H #{$width - 5}
			M #{$width - 5} #{$height}
			H 5
			Q 0 #{$height} 0 #{$height div 2}
			Q 0 0 5 0" />
		<ellipse cx="#{$width - 5}" cy="#{$height div 2}" rx="5" ry="#{$height div 2}" />
	</xsl:template>
	<xsl:template mode="shape" match="adl:display">
		<path d="
			M 0 #{$height div 2}
			L 5 5
			L #{$width - 10} 0
			Q #{$width} 0 #{$width} #{$height div 2}
			Q #{$width} #{$height} #{$width - 10} #{$height}
			L 5 #{$height - 5}
			z" />
	</xsl:template>
	<xsl:template mode="shape" match="adl:loop-limit">
		<path d="
			M 5 0
			H #{$width - 5}
			L #{$width} 5
			V #{$height}
			H 0
			V 5
			z" />
			
	</xsl:template>
	<xsl:template mode="shape" match="adl:reference">
		<ellipse cx="#{$width div 2}" cy="#{$height div 2}"
			rx="#{$width div 2}" ry="#{$height div 2}" />
	</xsl:template>
	<xsl:template mode="shape" match="adl:sequential">
		<path d="
			M 0 #{$height div 2}
			Q 0 #{$height} #{$width div 2} #{$height}
			L #{$width} #{$height}
			v -10
			h -4 
			Q #{$width} #{$height - 10} #{$width} #{$height div 2}
			Q #{$width} 0 #{$width div 2} 0
			Q 0 0 0 #{$height div 2}" />	
	</xsl:template>
	<xsl:template mode="shape" match="adl:predefined">
		<rect x="0" y="0" width="#{$width}" height="#{$height}" />
		<path d="M 5 0 V #{$height} 
			M #{$width - 5} 0 V #{$height}" />
	</xsl:template>


	<xsl:template mode="shape" match="adl:stored">
		<path
			d="
			M 5 0
			H #{$width} 
			Q #{$width - 5} 0 #{$width - 5} #{$height div 2} 
			Q #{$width - 5} #{$height} #{$width} #{$height}
			H 5
			Q 0 #{$height} 0 #{$height div 2} 
			Q 0 0 5 0" />
	</xsl:template>

	<shape id="start">
		<ellipse cx="#{$width div 2}" cy="#{$height div 2}"
			rx="#{$width div 2}" ry="#{$height div 2}" />
	</shape> -->

</xsl:stylesheet>