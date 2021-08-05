<xsl:stylesheet
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns="http://www.kite9.org/schema/adl"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:k9="http://www.kite9.org/schema/macros"
        version="1.0">


  <xsl:template match="adl:label">
    <svg:g>
      <xsl:attribute name="k9-elem">label</xsl:attribute>
      <xsl:attribute name="k9-palette">label</xsl:attribute>
      <xsl:attribute name="k9-texture">none</xsl:attribute>

    </svg:g>
    k9:elem="label" k9-ui="delete orphan edit"
           k9-palette="label"
           k9-texture="none">


  </xsl:template>
    id='label' 

    <back />
    <front style="kite9-type: text; ">
      <contents optional="true" />
    </front>
  </xsl:template>
  
  
</svg:xsl>
