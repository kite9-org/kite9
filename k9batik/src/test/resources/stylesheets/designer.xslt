<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:k9="http://www.kite9.org/schema/adl"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        version="1.0">

  <xsl:param name="base-uri" />
  <xsl:param name="template-uri" />
  <xsl:param name="template-path" />


  <xsl:template match="diagram">
    <svg contentScriptType="text/ecmascript"
         zoomAndPan="magnify"
         contentStyleType="text/css"
         preserveAspectRatio="xMidYMid"
         version="1.0">
      <xsl:attribute name="k9:width">$width</xsl:attribute>
      <xsl:attribute name="k9:height">$height</xsl:attribute>

      <style type="text/css">
        @import url("<xsl:value-of select="$template-path" />designer.css");
      </style>

      <xsl:apply-templates />

    </svg>
  </xsl:template>
</xsl:stylesheet>