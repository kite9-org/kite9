<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:svg="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="/public/templates/common/common.xsl" />
	<xsl:import	href="/public/templates/containers/containers-template.xsl" />
	<xsl:import href="/public/templates/common/diagram/diagram-template.xsl" />
	<xsl:import	href="/public/templates/common/formats/formats-template.xsl" />
  <xsl:import href="/public/templates/links/common-links-template.xsl" />

	<xsl:template match="adl:box">
		<xsl:call-template name="fixed-container">
	     <xsl:with-param name="k9-rounding">5pt</xsl:with-param>	
		</xsl:call-template>
	</xsl:template>

  <xsl:template match="adl:container">
    <xsl:call-template name="editable-container">
      <xsl:with-param name="k9-rounding">10pt</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
	<xsl:template match="adl:textarea">
		<xsl:call-template name="textarea" />
	</xsl:template>

	<xsl:template match="adl:diagram">
		<xsl:call-template name="diagram" />
	</xsl:template>
 
	<xsl:template match="/">
		<xsl:call-template name="root-svg">
			<xsl:with-param name="css">
				@import url('/public/templates/basic/basic.css');
			</xsl:with-param>
			<xsl:with-param name="constants">
				document.params = {
				'align-template-uri' : '/public/templates/links/common-links.adl#align',
				'link-template-uri' : '/public/templates/links/common-links.adl#l1',
				'cell-template-uri' : '/public/templates/grid/common-grid.adl#t1',
				'palettes' : [
				/* '/public/templates/links/common-links.adl', 'link',
				'/public/templates/links/common-ends.adl', 'end',
				'/public/templates/designer/palette.adl', 'connected symbol',
				'/public/templates/flowchart/palette-inline.adl', 'connected',
				'/public/templates/flowchart/palette-captioned.adl', 'connected',
				'/public/templates/uml/palette.adl', 'connected',
				'/public/templates/containers/common-containers.adl', 'connected',
				'/public/templates/grid/common-grid.adl', 'cell table'*/

				] ,
				'label-template-uri' : '/public/templates/labels/common-labels.adl#label'
				};
			</xsl:with-param>
			<xsl:with-param name="script">
				import '/public/templates/basic/basic.js';
			</xsl:with-param>
      <xsl:with-param name="defs">
        <xsl:copy-of select="$links-markers" />
      </xsl:with-param>
		</xsl:call-template>
	</xsl:template>


</xsl:stylesheet>
        
        
        

