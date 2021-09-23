<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="uml-shapes-template.xsl" />
 
  <xsl:template match="/">
    <xsl:call-template name="diagram-root-svg">
      <xsl:with-param name="css">
        @import url('/public/templates/uml/uml.css');
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
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
 <xsl:template match="
    adl:property | 
    adl:method">
    <xsl:call-template name="formats-text-fixed" />
  </xsl:template>
   
  <xsl:template match="
    adl:use-case | 
    adl:note">
    <xsl:call-template name="formats-text-shape-inline" />
  </xsl:template>

  <xsl:template match="
    adl:interface | 
    adl:actor">
    <xsl:call-template name="formats-text-shape-captioned" />
  </xsl:template>

  <xsl:template match="adl:uml-container | 
    adl:component |
    adl:package |
    adl:properties |
    adl:methods">
    <xsl:call-template name="formats-container" />
  </xsl:template>
  
  <xsl:template match="adl:class-name | adl:stereotype">
    <xsl:call-template name="formats-text-fixed" />
  </xsl:template>

  <xsl:template match="adl:class">
    <xsl:call-template name="formats-container">
      <xsl:with-param name="content">
        <xsl:apply-templates select="adl:stereotype" />
        <xsl:apply-templates select="adl:class-name" />
        <xsl:call-template name="formats-container">
          <xsl:with-param name="k9-elem">properties</xsl:with-param>
          <xsl:with-param name="content">
            <xsl:apply-templates select="adl:property" />
          </xsl:with-param>
          <xsl:with-param name="k9-shape">
            <xsl:call-template name="uml-divider" />
          </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="formats-container">
          <xsl:with-param name="k9-elem">methods</xsl:with-param>
          <xsl:with-param name="content">
            <xsl:apply-templates select="adl:method" />
          </xsl:with-param>
          <xsl:with-param name="k9-shape">
            <xsl:call-template name="uml-divider" />
          </xsl:with-param>
        </xsl:call-template> 
      </xsl:with-param>
    </xsl:call-template>
  
  </xsl:template>
 
</xsl:stylesheet>
