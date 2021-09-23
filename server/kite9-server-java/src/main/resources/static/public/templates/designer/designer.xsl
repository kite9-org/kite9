<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

  <xsl:import href="../adl/adl-template.xsl" />
  <xsl:import href="designer-components.xsl" />


  <xsl:template match="/" mode="diagram-css">
    @import url('/public/templates/designer/designer.css');
  </xsl:template>

    <xsl:template match="/">
      <xsl:call-template name="diagram-root-svg">
          <xsl:with-param name="css">
          </xsl:with-param>
          <xsl:with-param name="constants">
              document.params = {
                  'align-template-uri' : '/public/templates/links/common-links.adl#align',
                  'link-template-uri' : '/public/templates/links/common-links.adl#l1',
                  'cell-template-uri' : '/public/templates/grid/palette.adl#t1',
                  'palettes' : [
              /*          '/public/templates/links/common-links.adl', 'link',
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
              import '/public/templates/designer/designer.js';
          </xsl:with-param>
          <xsl:with-param name="defs">
              <xsl:copy-of select="$links-markers" />
              <xsl:copy-of select="$container-indicators" />
              <xsl:copy-of select=".//svg:defs" />
              <linearGradient id='glyph-background' x1='0%' x2='0%' y1='0%' y2='100%'>
                  <stop offset='0%' stop-color='#FFF'/>
                  <stop offset='100%' stop-color='#DDD'/>
              </linearGradient>

              <filter id="dropshadow" height="130%">
                  <feGaussianBlur in="SourceAlpha" stdDeviation="1"/>
                  <feOffset dx="2" dy="2"/>
                  <feColorMatrix
                          type="matrix"
                          values="0 0 0 0 .5
                0 0 0 0 .5
                0 0 0 0 .5
                0 0 0 1 0"/>
                  <feMerge>
                      <feMergeNode/>
                      <feMergeNode in="SourceGraphic"/>
                  </feMerge>
              </filter>
          </xsl:with-param>

      </xsl:call-template>
    </xsl:template>

    <xsl:attribute-set name="filters">
        <xsl:attribute name="filter">url(#dropshadow)</xsl:attribute>
    </xsl:attribute-set>

   

</xsl:stylesheet>