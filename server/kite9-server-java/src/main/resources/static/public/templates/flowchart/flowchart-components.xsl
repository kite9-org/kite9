<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
  
  <xsl:import href="../formats/formats-components.xsl" />
  
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
      <rect x="0" y="0" pp:x="$x" pp:y="$y" width="0" height="0" pp:width="$width" pp:height="$height" />
    </g>
	</xsl:template>
 
	<xsl:template mode="shape" match="adl:document" priority="2">
		<path d=""
			pp:d="M [[$x]] [[$y]]
            v [[$height - 3]]
			      q [[$width div 4]] -6 [[$width div 2]] 0
            q [[$width div 4]] 6 [[$width div 2]] 0
			      V [[$y]]
			      H [[$x]]" />
	</xsl:template>
  
  
  <xsl:template mode="shape" match="adl:decision" priority="2">
    <path d=""  pp:d="concat(
      ' M ',$x + $width div 2,' ',$y,
      ' l ', $width div 2,' ',$height div 2,
      ' l -', $width div 2,' ',$height div 2,
      ' l -',$width div 2,' -',$height div 2,
      ' z')" />
  </xsl:template>
  
	<xsl:template mode="shape" match="adl:delay" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path
  			d=""
        pp:d="
          concat(
           ' M 0 0 ',
           ' V ',$height,
           ' H ',$width - 20,
           ' Q ',$width,' ',$height,' ',$width,' ',$height div 2,
           ' Q ',$width,' 0 ',$width - 20,' 0 ',
           ' H 0')
  		" />
    </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:manual" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
        pp:d="concat(
			 ' M 0 0 ',
			 ' H ',$width,
			 ' L ',$width -5,' ',$height,
			 ' H 5 z')" />
    </g>  
	</xsl:template>

	<xsl:template mode="shape" match="adl:terminator" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
		  <path
			d=""
      pp:d="concat(
			' M 20 0 ',
			' Q 0 0 0 ',$height div 2,
			' Q 0 ',$height,' 20 ',$height, 
			' H ',$width - 20,
			' Q ',$width,' ',$height,' ',$width,' ',$height div 2,
			' Q ',$width,' 0 ',$width - 20,' 0z')" />
      </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:input" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
      <path d=""
        pp:d="concat(
        ' M 0 10 ',
        ' L ',$width,' 0',
        ' V ',$height,
        ' H 0z')">
      </path>
    
    </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:database" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
        pp:d="concat(
  			' M 0 5',
  			' L 0 ',$height - 5,
  			' Q 0 ',$height,' ',$width div 2,' ',$height,
  			' Q ',$width,' ',$height,' ',$width,' ',$height - 5,
  			' L ',$width,' ',$height - 5,
  			' L ',$width,' 5')" />
  		<ellipse cx="0" cy="5" rx="0" pp:cx="$width div 2" pp:rx="$width div 2" ry="5" />
    </g>
	</xsl:template>


	<xsl:template mode="shape" match="adl:preparation" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
        pp:d="concat(
  			' M 10 0',
  			' H ',$width - 10,
  			' L ',$width,' ',$height div 2,
  			' L ',$width - 10,' ',$height,
  			' H 10',
  			' L 0 ',$height div 2,
  			'z')" />
      </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:internal" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<rect x="0" y="0" width="0" height="0" pp:width="$width" pp:height="$height" />
  		<path d="" pp:d="concat('M 0 5 H ',$width)" />
  		<path d="" pp:d="concat('M 5 0 V ',$height)" />
    </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:off-page" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
        pp:d="concat(
  			' M 0 0 ',
  			' H ',$width,
  			' V ',$height - 10,
  			' L ',$width div 2,' ',$height,
  			' L 0 ',$height - 10,
  			'z')" />
    </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:direct" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
        pp:d="concat(
  			' M 5 0 ',
  			' M ',$width - 5,' ',$height,
  			' H 5 ',
  			' Q 0 ',$height,' 0 ',$height div 2,
  			' Q 0 0 5 0',
        ' H ',$width - 5)
        " />
  		<ellipse cx="0" pp:cx="$width - 5" cy="0" pp:cy="$height div 2" rx="5" ry="0" pp:ry="$height div 2" />
    </g>
	</xsl:template>
  
	<xsl:template mode="shape" match="adl:display" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
      pp:d="concat(
			' M 0 ',$height div 2,
			' L 5 5',
			' L ',$width - 10,' 0',
			' Q ',$width,' 0 ',$width,' ',$height div 2,
			' Q ',$width,' ',$height,' ',$width - 10,' ',$height,
			' L 5 ',$height - 5,
			' z')" />
    </g>
	</xsl:template>
  
	<xsl:template mode="shape" match="adl:loop-limit" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d="" 
        pp:d="concat(
  			' M 5 0',
  			' H ',$width - 5,
  			' L ',$width,' 5',
  			' V ',$height,
  			' H 0',
  			' V 5',
  			'z')" />
    </g>			
	</xsl:template>
  
	<xsl:template mode="shape" match="adl:sequential" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
        pp:d="concat(
  			' M 0 ',$height div 2,
  			' Q 0 ',$height,' ',$width div 2,' ',$height,
  			' L ',$width,' ',$height,
  			' v -10',
  			' h -4',
  			' Q ',$width,' ',$height - 10,' ',$width,' ',$height div 2,
  			' Q ',$width,' 0 ',$width div 2,' 0',
  			' Q 0 0 0 ',$height div 2)" />	
    </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:predefined" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<rect x="0" y="0" width="0" height="0" pp:width="$width" pp:height="$height" />
  		<path d="" pp:d="concat(
        ' M 5 0 V ',$height, 
  			' M ',$width - 5,' 0 V ',$height)" />
    </g>
	</xsl:template>

	<xsl:template mode="shape" match="adl:stored" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
  		<path d=""
  			pp:d="concat(
  			' M 5 0',
  			' H ',$width,
  			' Q ',$width - 5,' 0 ',$width - 5,' ',$height div 2,
  			' Q ',$width - 5,' ',$height,' ',$width,' ',$height,
  			' H 5 ',
  			' Q 0 ',$height,' 0 ',$height div 2, 
  			' Q 0 0 5 0')" />
    </g>
      
	</xsl:template>

  <xsl:template mode="shape" match="adl:start" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
      <ellipse cx="0" pp:cx="$width div 2" cy="0" pp:cy="$height div 2"
        rx="0" pp:rx="$width div 2" ry="0" pp:ry="$height div 2" />
    </g>
  </xsl:template>

  <xsl:template mode="shape" match="adl:reference" priority="2">
    <g>
      <xsl:attribute name="pp:style">concat('transform: translate(', $x,'px,',$y,'px);')</xsl:attribute>
      <ellipse cx="0" pp:cx="$width div 2" cy="0" pp:cy="$height div 2"
        rx="0" pp:rx="$width div 2" ry="0" pp:ry="$height div 2" />
    </g>
  </xsl:template>
  
  
  <xsl:template match="adl:document | adl:decision | adl:terminal | 
    adl:terminator | adl:process | adl:delay | 
    adl:direct | adl:display | adl:stored | 
    adl:predefined | adl:preparation | adl:manual | 
    adl:decision | adl:loop-limit | adl:internal | 
    adl:database | adl:reference | adl:sequential | 
    adl:input | adl:start | adl:document | adl:off-page"> 
    <xsl:choose>
      <xsl:when test="@k9-format='text-shape-captioned'">
        <xsl:call-template name="formats-text-shape-captioned">
           <xsl:with-param name="k9-texture">flowchart-outline</xsl:with-param>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:call-template name="formats-text-shape-inline">
           <xsl:with-param name="k9-texture">flowchart-symbol</xsl:with-param>
         </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="flowchart-diagram-element-css">
    <adl:css>@import url('/public/templates/flowchart/flowchart-elements.css');</adl:css>
  </xsl:template>

</xsl:stylesheet>