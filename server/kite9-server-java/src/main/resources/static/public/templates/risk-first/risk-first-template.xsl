<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="../votes/votes-template.xsl" />
  
  <xsl:variable name="risk-background">
    <linearGradient id='risk-background' x1='0%' x2='0%' y1='0%' y2='100%'>
      <stop offset='0%' stop-color='#FFF' />
      <stop offset='100%' stop-color='#DDD' />
    </linearGradient>    
  </xsl:variable>

  <xsl:template match="/">
    <xsl:call-template name="diagram-root-svg">
      <xsl:with-param name="css">
        @import url('/public/templates/risk-first/risk-first.css');
      </xsl:with-param>
      <xsl:with-param name="constants">
        document.params = {
        'align-template-uri' : '/public/templates/links/links-palette.adl#align',
        'link-template-uri' : '/public/templates/links/links-palette.adl#l1',
        'cell-template-uri' : '/public/templates/grid/palette.adl#t1',
        'label-template-uri' : '/public/templates/labels/palette.adl#label',
        'palettes' : [
       /* '/public/templates/links/links-palette.adl', 'link',
        '/public/templates/links/ends-palette.adl', 'end',
        '/public/templates/designer/palette.adl', 'connected symbol',
        '/public/templates/flowchart/palette-inline.adl', 'connected',
        '/public/templates/flowchart/palette-captioned.adl', 'connected',
        '/public/templates/uml/palette.adl', 'connected',
        '/public/templates/containers/palette.adl', 'connected',
        '/public/templates/grid/palette.adl', 'cell table' */

        ]
        }; 
<!--         
@params {
  palettes: 
    url('/public/templates/links/common-links.adl') "link"
    url('/public/templates/links/common-ends.adl') "end"
    url('/public/templates/risk-first/artifact-palette.adl') "connected risk"
    url('/public/templates/risk-first/risk-palette.adl') "connected risk"  
    url('/public/templates/containers/common-containers.adl') "connected"
    url('/public/templates/grid/common-grid.adl') "cell table";
  link-template-uri:   url('/public/templates/links/common-links.adl#l6');
} -->
      </xsl:with-param>
      <xsl:with-param name="script">
        import '/public/templates/risk-first/risk-first.js';
      </xsl:with-param>
      <xsl:with-param name="defs">
        <xsl:copy-of select="$links-markers" />
        <xsl:copy-of select="$container-indicators" />
        <xsl:copy-of select=".//svg:defs" />
        <xsl:copy-of select="$risk-background" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <!-- standard stuff that goes inside a risk-box -->
  <xsl:template name="risk-content">
    <xsl:apply-templates select="adl:code" />
    <xsl:call-template name="formats-image">
      <xsl:with-param name="k9-elem">image</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="votes" />
    <xsl:apply-templates select="adl:title" />
  </xsl:template>
  
  <xsl:template match="adl:title | adl:code">
    <xsl:call-template name="formats-text-fixed" />
  </xsl:template>
 
  <xsl:template name="risk-generic" match="adl:risk[@class='generic']">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="k9-ui">drag delete align connect autoconnect vote</xsl:with-param>
      <xsl:with-param name="k9-palette">connected risk</xsl:with-param>
      <xsl:with-param name="content">
        <xsl:call-template name="risk-content" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
      
<!--   
      <back /> 
      <contents xpath="adl:code" />
      <image width="50pt" height="50pt" href="pre:#{@href}" id="pre:#{@id}@image"/>
      <votes count="pre:#{count(adl:vote)}" />
      <contents xpath="adl:title" />
  </template>
  
  <template id="fixed" k9-ui="drag delete align connect autoconnect edit vote" k9-palette="connected risk " k9-texture="solid">
      <back />
      <code>#{$template-1}</code>
      <image width="50pt" height="50pt" href="pre:#{$template-2}" />
      <votes count="pre:#{count(adl:vote)}" />
      <title k9-ui="text">#{$template-3}</title>
  </template>
  
  <template id="action" k9-ui="drag delete align connect autoconnect edit vote" k9-palette="connected " k9-texture="solid">
    <shape k9-highlight="pulse">
      <svg:polygon points='0 0, #{$width - (15 * $pt)} 0, #{$width} #{$height div 2}, #{$width - (15 * $pt) } #{$height}, 0 #{$height}'  />
    </shape>
    <votes count="pre:#{count(adl:vote)}" />
    <front style="kite9-usage: regular; kite9-type: container; kite9-layout: down; 	kite9-padding: 10pt; kite9-padding-right: 40pt; ">
      <title k9-ui="text">
      	<contents optional="true" />
      </title>x
    </front>
  </template>
  
  <template id="action-left" k9-ui="drag delete align connect autoconnect edit vote" k9-palette="connected "  k9-texture="solid">
    <shape style='kite9-usage: decal; kite9-type: svg; fill: url(#risk-background); ' k9-highlight="fill">
      <svg:polygon points='0 #{$height div 2}, #{15 * $pt} 0, #{$width} 0, #{$width} #{$height}, #{15 * $pt} #{$height} 0, #{$height div 2}' />
    </shape>
    <votes count="pre:#{count(adl:vote)}" />
    <front style="kite9-usage: regular; kite9-type: container; kite9-layout: down; 	kite9-padding: 10pt; kite9-padding-left: 40pt; ">
      <title k9-ui="text">
      	<contents optional="true" />
      </title>
    </front>
  </template>
  
  <template id="artifact" k9-ui="drag delete align connect autoconnect edit" k9-palette="connected " k9-texture="none" >
    <back />
    <image width="80pt" height="80pt" href="#{$template-1}" />
    <title><contents optional="true" /></title>
  </template>
  
  <template id="generic-artifact" k9-ui="drag delete align connect autoconnect edit" k9-palette="connected " k9-texture="none">
    <back />
    <image width="80pt" height="80pt" href="pre:#{@href}" id="pre:#{@id}@image" />
    <title><contents optional="true" /></title>
  </template>
  
  <template id='mitigated' k9-ui="drag delete align connect autoconnect edit" k9-palette="connected " k9-texture="none" k9-contains="risk">
    <back />
    <contents optional="true" />
    <front style='kite9-usage: decal; kite9-type: svg;  '>
      <svg:line x1="0" y1="#{$height}" x2="#{$width}" y2="0" stroke="black" stroke-width="10pt" stroke-opacity="0.2"/>
    </front>
  </template>
  
  <template id='hidden' k9-ui="drag delete align connect autoconnect edit" k9-palette="connected " k9-texture="none" k9-contains="risk">
    <back />
    <contents optional="true" />
    <front style='kite9-usage: decal; kite9-type: svg; fill-opacity: 0.2; fill: black; kite9-transform: position;' class="tranparent">
      <svg:g transform="scale(#{$width div 230}) translate(140,-20)" style="kite9-template: url(/public/templates/risk-first/redesign/decals/hidden_risk_v2.svg);" />
	</front>
  </template>

  
  <template id='planner' k9-ui="" k9-contains="planner">
    <back />
    <contents optional="true" />
  </template>
  
 
 <template id="planner-cell" k9-ui="cascade orphan label delete" layout="right" k9-contains="connected" >
    <back k9-highlight="pulse" />
    <indicator />
    <contents optional="true" />
  </template> 
  
  <planner-cell id="child-planner-cell"/> -->
  
</xsl:stylesheet>