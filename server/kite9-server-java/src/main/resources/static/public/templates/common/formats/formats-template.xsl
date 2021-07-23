<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:svg="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:import href="/public/templates/common/back/back-template.xsl" />

	<!-- simple text area, can only be edited, not moved, deleted etc. -->
	<xsl:template name="textarea">
	  <xsl:param name="k9-format">textarea</xsl:param>
    <xsl:param name="k9-elem"><xsl:value-of select="local-name()" /></xsl:param>
    <xsl:param name="k9-texture">none</xsl:param>
    <xsl:param name="k9-ui">orphan edit</xsl:param>
    <xsl:param name="k9-rounding">0pt</xsl:param>
    <xsl:param name="text"><xsl:value-of select="text()" /></xsl:param>
    
		<g>
			 <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
			 <xsl:attribute name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:attribute>
			 <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
			 <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
       <xsl:copy-of select="@*" />
      
       <xsl:if test="$k9-texture != 'none'">
         <xsl:call-template name="back-round-rect">
          <xsl:with-param name="k9-rounding"><xsl:value-of select="$k9-rounding" /></xsl:with-param>
         </xsl:call-template>
       </xsl:if>
      
			 <text>
			 	 <xsl:value-of select="$text" />
			 </text>
			
		</g>
	</xsl:template>
  
  <!-- Inline text, where the k9-shape attribute defaults to the tag name, 
    and the user can edit the text, drag it around etc. -->
  <xsl:template name='inline-text'>
    <xsl:param name="k9-format">inline-text</xsl:param>
    <xsl:param name="k9-elem"><xsl:value-of select="local-name()" /></xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag edit delete align connect insert autoconnect</xsl:param>
    <xsl:param name="k9-rounding">0pt</xsl:param>
    <xsl:param name="text"><xsl:value-of select="text()" /></xsl:param>
    
    <xsl:call-template name="textarea">
      <xsl:with-param name="k9-format"><xsl:value-of select="$k9-format" /></xsl:with-param>
      <xsl:with-param name="k9-elem"><xsl:value-of select="$k9-elem" /></xsl:with-param>
      <xsl:with-param name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:with-param>
      <xsl:with-param name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:with-param>
      <xsl:with-param name="k9-rounding"><xsl:value-of select="$k9-rounding" /></xsl:with-param>
      <xsl:with-param name="text"><xsl:value-of select="$text" /></xsl:with-param>
    
    </xsl:call-template>
  
    
  </xsl:template>


	<!-- A basic on-screen image, params: @href, @width @height -->
	<!-- batik hates it if you don't specify height & width -->

	<!-- <xsl:template name="image-editable" k9-ui="image" k9-format="image-editable"> 
		<decal style="kite9-type: svg; kite9-usage: decal;" k9-texture="solid"> <svg:image 
		x="0" y="0" width="#{$width}" height="#{$height}" xlink:href="pre:#{@href}" 
		/> </decal> <front k9-highlight="stroke pulse" style='kite9-type: svg; ' 
		k9-texture="none"> <svg:rect x='0' y='0' width='pre:#{@width}' height='pre:#{@height}' 
		rx='2' ry='2' /> </front> </xsl:template> -->
    
    
    
	

  <!-- Container template where the container contents is in a specific order -->
  <xsl:template name='fixed-container'>
    <xsl:param name="k9-format">fixed-container</xsl:param>
    <xsl:param name="k9-palette">connected</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag delete align connect autoconnect cascade</xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="layout"><xsl:value-of select="@layout"/></xsl:param>
    <g>
      <xsl:attribute name="k9-ui"><xsl:value-of select="$k9-ui" /></xsl:attribute>
      <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:attribute name="k9-format"><xsl:value-of select="$k9-format" /></xsl:attribute>
      <xsl:attribute name="k9-texture"><xsl:value-of select="$k9-texture" /></xsl:attribute>
      <xsl:attribute name="k9-palette"><xsl:value-of select="$k9-palette" /></xsl:attribute>
      <xsl:if test="$layout">
        <xsl:attribute name="layout"><xsl:value-of select="$layout"></xsl:value-of></xsl:attribute>
      </xsl:if>
		  <xsl:copy-of select="@*" />
		   
		  <xsl:call-template name="back-round-rect">
        <xsl:with-param name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:with-param>
        <xsl:with-param name="k9-rounding"><xsl:value-of select="$k9-rounding" /></xsl:with-param>
       </xsl:call-template>
       
		  <xsl:apply-templates />
     </g>
  </xsl:template>




  <!-- Editable text object with an icon, where the icon has the k9-shape of the tag of the element. You can edit the text. 
    Links connect to the icon. -->

  <xsl:template name="captioned-text">
   k9-ui="drag delete align edit cascade" k9-palette="connected"
    k9-format="captioned-text" k9-texture="none">
    <back/>
    <depiction k9-shape="pre:#{local-name()}" k9-ui="connect autoconnect align 
		delete"
      name="pre:#{@id}@dep" k9-texture="solid">
      <back k9-highlight="pulse"/>
    </depiction>
    <caption id="pre:#{@id}@caption" k9-shape="rect" k9-texture="none">
      <back/>
      <textarea k9-ui="text">
        <contents optional="true"/>
      </textarea>
    </caption>
    <shape-align>
      <from reference="pre:#{@id}@dep"/>
      <to reference="pre:#{@id}@caption"/>
    </shape-align>
  </xsl:template> 

	<!-- Icon with text below it. k9-shape of the icon defaults to the tag used. -->
	<!-- <xsl:template name='portrait-text' k9-ui="drag delete edit align cascade 
		connect autoconnect" k9-palette="connected" k9-format="portrait-text"> <back 
		/> <depiction k9-shape="pre:#{local-name()}" k9-texture="solid"> <back k9-highlight="pulse" 
		/> </depiction> <caption k9-shape="rect" k9-texture="none"> <back /> <textarea 
		k9-ui="text"> <contents optional="true" /> </textarea> </caption> </xsl:template> -->

</xsl:stylesheet>
