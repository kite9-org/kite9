<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:svg="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl-template.xsl" />

	<xsl:template match="adl:box">
		<xsl:call-template name="formats-container">
	     <xsl:with-param name="k9-rounding">5pt</xsl:with-param>	
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="adl:textarea">
		<xsl:call-template name="formats-text-inline">
      <xsl:with-param name="k9-texture">none</xsl:with-param>
    </xsl:call-template>
	</xsl:template>
 
	<xsl:template match="/">
		<xsl:call-template name="diagram-root-svg">
			<xsl:with-param name="css">
				@import url('/public/templates/basic/basic.css');
			</xsl:with-param>
			<xsl:with-param name="constants">
				document.params = {
				'align-template-uri' : '/public/templates/links/links-palette.adl#align',
				'link-template-uri' : '/public/templates/links/links-palette.adl#l1',
				'cell-template-uri' : '/public/templates/grid/palette.adl#t1',
        'label-template-uri' : '/public/templates/labels/palette.adl#label',
				'palettes' : [
				/* '/public/templates/links/common-links.adl', 'link',
				'/public/templates/links/common-ends.adl', 'end',
				'/public/templates/designer/palette.adl', 'connected symbol',
				'/public/templates/flowchart/palette-inline.adl', 'connected',
				'/public/templates/flowchart/palette-captioned.adl', 'connected',
				'/public/templates/uml/palette.adl', 'connected',
				'/public/templates/containers/common-containers.adl', 'connected',
				'/public/templates/grid/common-grid.adl', 'cell table'*/

				]
				};
			</xsl:with-param>
			<xsl:with-param name="script">
				import '/public/templates/basic/basic.js';
			</xsl:with-param>
      <xsl:with-param name="defs">
        <xsl:copy-of select="$links-markers" />
        <xsl:copy-of select="$container-indicators" />
        <xsl:copy-of select=".//svg:defs" />
      </xsl:with-param>
		</xsl:call-template>
	</xsl:template>


</xsl:stylesheet>
        
        
        

