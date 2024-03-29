<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
 
  <xsl:template name='links-link-align' match="*[@k9-format='link-align']">
    <xsl:param name="class" select="@class"/>
    <xsl:param name="style" select="@style"/>
    <xsl:param name="attributes" select="@*[name() != 'class' and name() != 'style' and name() != 'id' ]" />
    <xsl:param name="id" select="@id" />

    <xsl:param name="k9-elem" select="local-name()" />
    <xsl:param name="k9-format">link-align</xsl:param>
    <xsl:param name="k9-highlight">bar stroke</xsl:param>
    <xsl:param name="k9-ui">delete link cascade direction</xsl:param>
    
    <xsl:param name="shape">
   		<g k9-elem="link-grab">
        <xsl:attribute name="k9-highlight"><xsl:value-of select="$k9-highlight" /></xsl:attribute>
        <path pp:d="$path" d="" k9-animate="link"/>
      </g>
    </xsl:param>
    
    <xsl:param name="body">
      <g k9-elem="align-body">
        <path k9-animate="link" d="" pp:d="$path" />
      </g>
    </xsl:param>
      
    <xsl:param name="content">
   	  <g k9-elem="from" reference="{./adl:from/@reference}" />
      <g k9-elem="to" reference="{adl:to/@reference}" /> 
      <xsl:copy-of select="$body" />	
   	</xsl:param> 
   	
    <xsl:param name="decoration"><xsl:apply-templates mode="link-decoration" select="." /></xsl:param>
    
    <xsl:call-template name="texture-basic">
      <xsl:with-param name="k9-elem" select="$k9-elem"  />
      <xsl:with-param name="k9-format" select="$k9-format"  />
      <xsl:with-param name="k9-highlight" select="$k9-highlight"  />
      <xsl:with-param name="k9-texture" />
      <xsl:with-param name="k9-ui" select="$k9-ui" />
      <xsl:with-param name="id" select="$id"  />
      <xsl:with-param name="style" select="$style" />
      <xsl:with-param name="class" select="$class" />
      <xsl:with-param name="attributes" select="$attributes"  />
      <xsl:with-param name="shape" select="$shape" />
      <xsl:with-param name="content" select="$content" />
      <xsl:with-param name="decoration" select="$decoration" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:align">
    <xsl:call-template name="links-link-align" />
  </xsl:template>
  
</xsl:stylesheet>