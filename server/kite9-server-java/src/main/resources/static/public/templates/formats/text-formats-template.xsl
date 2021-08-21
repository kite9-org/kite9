<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:import href="../back/back-template.xsl" />
  <xsl:import href="generic-formats-template.xsl" />
  
  <!-- 
    This defines several basic ways of outputting content, chosen by the k9-format parameter.
    Objects defining k9-format will therefore use this, but you can call the template directly.
    
    You can use these as-is, or override their parameters to give new behaviours.
    
    text-inline         - shape surrounds text
    text-inline-fixed   - non-editable version
    
    text-caption        - shape above/below text
    text-caption-fixed  - non-editable version
   --> 
   
  <xsl:template name="formats-text-inline-fixed" match="*[@k9-format='text-inline-fixed']">
    <xsl:param name="content"><text><xsl:value-of select="text()" /></text></xsl:param>

    <xsl:param name="k9-contains"></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-inline-fixed</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-palette"></xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">none</xsl:param>
    <xsl:param name="k9-ui">orphan edit</xsl:param>
    <xsl:param name="k9-shape">
      <xsl:call-template name="back-basic">
        <xsl:with-param name="highlight" select="$k9-highlight" /> 
        <xsl:with-param name="rounding" select="$k9-rounding" />
      </xsl:call-template>  
    </xsl:param>
    
    <xsl:param name="k9-decoration"><xsl:apply-templates select="." mode="decoration" /></xsl:param>

    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    
    <xsl:call-template name="formats-generic">
      <xsl:with-param name="pre" select="$k9-shape" />
      <xsl:with-param name="content" select="$content" />
      <xsl:with-param name="post" select="$k9-decoration" />
      
      <!-- standard params -->
      <xsl:with-param name="k9-contains" select="$k9-contains" />
      <xsl:with-param name="k9-elem" select="$k9-elem" />
      <xsl:with-param name="k9-format" select="$k9-format" />
      <xsl:with-param name="k9-palette" select="$k9-palette" />
      <xsl:with-param name="k9-texture" select="$k9-texture" />
      <xsl:with-param name="k9-ui" select="$k9-ui" />
      <xsl:with-param name="attributes" select="$attributes" />
      <xsl:with-param name="class" select="$class" />
    
    </xsl:call-template>
    
  </xsl:template>


  <!-- Inline text, where the k9-shape attribute defaults to the tag name, 
    and the user can edit the text, drag it around etc. -->
  <xsl:template name='formats-text-inline' match="*[@k9-format='text-inline']">
    <xsl:param name="content"><text><xsl:value-of select="./text()" /></text></xsl:param>

    <xsl:param name="k9-contains"></xsl:param>
    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">text-inline</xsl:param>
    <xsl:param name="k9-highlight">pulse</xsl:param>
    <xsl:param name="k9-palette"></xsl:param>
    <xsl:param name="k9-rounding">5pt</xsl:param>
    <xsl:param name="k9-texture">solid</xsl:param>
    <xsl:param name="k9-ui">drag edit delete align connect insert autoconnect</xsl:param>
    <xsl:param name="k9-shape">
      <xsl:call-template name="back-basic">
        <xsl:with-param name="highlight" select="$k9-highlight" /> 
        <xsl:with-param name="rounding" select="$k9-rounding" />
      </xsl:call-template>  
    </xsl:param>
    <xsl:param name="k9-decoration"><xsl:apply-templates mode="decoration" /></xsl:param>

    <xsl:param name="class" select="@class"/>
    <xsl:param name="attributes" select="@*" />
    
    <xsl:call-template name="formats-generic">
      <xsl:with-param name="pre" select="$k9-shape" />
      <xsl:with-param name="content" select="$content" />
      <xsl:with-param name="post" select="$k9-decoration" />
      
      <!-- standard params -->
      <xsl:with-param name="k9-contains" select="$k9-contains" />
      <xsl:with-param name="k9-elem" select="$k9-elem" />
      <xsl:with-param name="k9-format" select="$k9-format" />
      <xsl:with-param name="k9-palette" select="$k9-palette" />
      <xsl:with-param name="k9-texture" select="$k9-texture" />
      <xsl:with-param name="k9-ui" select="$k9-ui" />
      <xsl:with-param name="attributes" select="$attributes" />
      <xsl:with-param name="class" select="$class" />
    
    </xsl:call-template>
    
  </xsl:template>





  <!-- Editable text object with an icon, where the icon has the k9-shape of the tag of the element. You can edit the text. 
    Links connect to the icon. -->

 <!--  <xsl:template name="formats-captioned-text">
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
  </xsl:template>  -->

	<!-- Icon with text below it. k9-shape of the icon defaults to the tag used. -->
  
<!--   <xsl:template name='portrait-text' k9-ui="drag delete edit align cascade 
		connect autoconnect"
    k9-palette="connected" k9-format="portrait-text">
    <back/>
    <depiction k9-shape="pre:#{local-name()}" k9-texture="solid">
      <back k9-highlight="pulse"/>
    </depiction>
    <caption k9-shape="rect" k9-texture="none">
      <back/>
      <textarea k9-ui="text">
        <contents optional="true"/>
      </textarea>
    </caption>
  </xsl:template> -->


  <!-- default no-match rules for back/decoration modes -->
    
    
      

  <!-- Editable text object with an icon, where the icon has the k9-shape of the tag of the element. You can edit the text. 
    Links connect to the icon. -->

 <!--  <xsl:template name="formats-captioned-text">
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
  </xsl:template>  -->

  <!-- Icon with text below it. k9-shape of the icon defaults to the tag used. -->
  
<!--   <xsl:template name='portrait-text' k9-ui="drag delete edit align cascade 
    connect autoconnect"
    k9-palette="connected" k9-format="portrait-text">
    <back/>
    <depiction k9-shape="pre:#{local-name()}" k9-texture="solid">
      <back k9-highlight="pulse"/>
    </depiction>
    <caption k9-shape="rect" k9-texture="none">
      <back/>
      <textarea k9-ui="text">
        <contents optional="true"/>
      </textarea>
    </caption>
  </xsl:template> -->

</xsl:stylesheet>
