<xsl:stylesheet 
 xmlns="http://www.w3.org/2000/svg" 
 xmlns:svg="http://www.w3.org/2000/svg"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:adl="http://www.kite9.org/schema/adl"
 xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


 <xsl:attribute-set name="terminators">
  <xsl:attribute name="k9-highlight">fill grab</xsl:attribute>
  <xsl:attribute name="k9-palette">end</xsl:attribute>
  <xsl:attribute name="k9-containers">link</xsl:attribute>
  <xsl:attribute name="k9-ui">drag label</xsl:attribute>
  <xsl:attribute name="k9-elem"><xsl:value-of select="local-name()" /></xsl:attribute>
  
 </xsl:attribute-set>


 <xsl:template name="round-terminator">
  <g xsl:use-attribute-sets="terminators">
   <xsl:copy-of select="@*" />
   <ellipse cx='0' cy='0' rx="8" ry="8"/>
   <xsl:apply-templates/>
  </g>
 </xsl:template>

 <xsl:template name="up-terminator">
  <g xsl:use-attribute-sets="terminators">
   <xsl:copy-of select="@*" />
   <polygon points="-10 12, 0 -8, 10 12"/>
   <xsl:apply-templates/>
  </g>
 </xsl:template>


 <xsl:template name="down-terminator">
  <g xsl:use-attribute-sets="terminators">
   <xsl:copy-of select="@*" />
   <polygon points="10 -12, 0 8, -10 -12"/>
   <xsl:apply-templates/>
  </g>
 </xsl:template>


 <xsl:template name="left-terminator">
  <g xsl:use-attribute-sets="terminators">
   <xsl:copy-of select="@*" />
   <polygon points="12 -10, -8 0, 12 10"/>
   <xsl:apply-templates/>
  </g>
 </xsl:template>


 <xsl:template name="right-terminator">
  <g xsl:use-attribute-sets="terminators">
   <xsl:copy-of select="@*" />
   <polygon points="-12 -10, 8 0, -12 10"/>
   <xsl:apply-templates/>
  </g>
 </xsl:template>

 <xsl:template name="null-terminator">
  <g xsl:use-attribute-sets="terminators">
   <xsl:copy-of select="@*" />
   <xsl:apply-templates/>
  </g>
 </xsl:template>


</xsl:stylesheet>