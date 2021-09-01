<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:svg="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">

    <xsl:import href="/public/templates/containers/containers-template.xsl" />
    <xsl:import href="/public/templates/common/diagram/diagram-template.xsl" />
    <xsl:import href="/public/templates/common/formats/formats-template.xsl" />
    <xsl:import href="/public/templates/common/common.xsl" />

    <xsl:template match="/">
      <xsl:call-template name="root-svg">
          <xsl:with-param name="css">
              @import url('/public/templates/designer/designer.css');
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
              <xsl:copy-of select="container-defs" />
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

    <xsl:template name="designer-show-box">
        <g k9-elem="back">
            <rect x="0" y="0"
                  pp:width="$width" pp:height="$height"
                  width="0"
                  height="0"
                  rx="8" ry="8" />
        </g>
    </xsl:template>


    <xsl:template match="adl:diagram">
        <xsl:call-template name="diagram" />
    </xsl:template>

    <xsl:template match="adl:glyph">
        <g k9-elem="glyph" k9-texture="background">
            <xsl:copy-of select="@*" />
            <xsl:call-template name="show-box" />
            <xsl:apply-templates/>
        </g>
    </xsl:template>

    <xsl:template match="adl:context">
        <xsl:call-template name="containers-basic" />
    </xsl:template>

    <xsl:template match='adl:context/adl:label | adl:cell/adl:label'>
        <g k9-elem="label" class="container-label">
            <xsl:copy-of select="@*" />
            <rect x='0' y='0' width="0" height="0" pp:width='$width' pp:height='$height' rx='4' ry='4' class="container-label-back" />
            <g k9-elem="text-label" class="container-label-front">
                <text><xsl:value-of select="string(.)" /></text>
            </g>
        </g>
    </xsl:template>

    <xsl:template match='adl:link-body | adl:arrow'>
        <g k9-elem="link-body">
            <xsl:copy-of select="@*" />
            <rect x='0' y='0' width="0" height="0" pp:width='$width' pp:height='$height' rx='4' ry='4' class="link-body-back" />
            <g k9-elem="text-label" class="link-body-label-text">
                <text><xsl:value-of select="@label" /><xsl:value-of select="string(.)" /></text>
            </g>
        </g>
    </xsl:template>

    <xsl:template match="adl:link">
        <g k9-elem="link">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates/>
            <g k9-elem="link-body">
                <path class="link">
                    <xsl:attribute name="pp:d">$path</xsl:attribute>
                    <xsl:attribute name="marker-start">url(#<xsl:value-of select="concat(from/@class,'-start-marker')" />)</xsl:attribute>
                    <xsl:attribute name="marker-end">url(#<xsl:value-of select="concat(to/@class,'-end-marker')" />)</xsl:attribute>
                </path>
            </g>
        </g>
    </xsl:template>

    <xsl:template match='adl:fromLabel | adl:toLabel'>
        <g>
            <xsl:copy-of select="@*" />
            <xsl:attribute name="k9-elem"><xsl:value-of select="name(.)" /></xsl:attribute>
            <rect x='0' y='0' width="0" height="0" pp:width='$width' pp:height='$height' rx='4' ry='4' class="connection-label-back" />
            <g k9-elem="text-label" class="connection-label-front">
                <text><xsl:value-of select="string(.)" /></text>
            </g>
        </g>
    </xsl:template>

    <xsl:template match="adl:glyph/adl:label">
        <xsl:call-template name="textarea" />
    </xsl:template>

    <xsl:template match="adl:glyph/adl:stereotype">
        <g k9-elem="text-label" class="glyph-stereotype-text">
            <text><xsl:apply-templates select="text()" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:glyph/adl:text-lines | adl:key/adl:text-lines">
        <g k9-elem="text-lines">
            <xsl:for-each select="text-line">
                <g k9-elem="text-label" class="generic-text">
                    <xsl:copy-of select="@*" />
                    <text><xsl:apply-templates select="text()" /></text>
                </g>
            </xsl:for-each>
        </g>
    </xsl:template>

    <!-- symbol -->
    <xsl:template match="adl:symbol[@shape='CIRCLE']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <circle style="fill: #00A070;  stroke: none" cx="6" cy="6" r="6" />
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:symbol[@shape='HEXAGON']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <polygon points="0,3 6,0 12,3 12,9 6,12 0,9" style="fill: #AA2030;  stroke: none"/>
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:symbol[@shape='DIAMOND']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <polygon points="6,0 12,6 6,12 0,6"  style="fill: #BB8040;  stroke: none" />
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)"  /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:symbol[@shape='SQUARE']">
        <g k9-elem="symbol">
            <xsl:copy-of select="@*" />
            <rect width="12" height="12" x="0" y="0"  style="fill: #CC8050; stroke: none" />
            <text x="6" y="9" class="symbol-text"><xsl:value-of select="substring(@theChar,1, 1)" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:key">
        <g k9-elem="key">
            <xsl:copy-of select="@*" />
            <g k9-elem="body">
                <rect x="0" y="0" pp:width="$width" pp:height="$height" width="0" height="0" style="fill: url(#glyph-background); " class="key-back"/>
                <xsl:apply-templates/>
            </g>
        </g>
    </xsl:template>

    <xsl:template match="adl:key/adl:boldText">
        <g k9-elem="boldText" class="bold-text">
            <xsl:copy-of select="@*" />
            <text><xsl:apply-templates select="text()" /></text>
        </g>
    </xsl:template>

    <xsl:template match="adl:key/adl:bodyText">
        <g k9-elem="bodyText" class="body-text">
            <xsl:copy-of select="@*" />
            <text><xsl:apply-templates select="text()" /></text>
        </g>
    </xsl:template>


    <xsl:template match='adl:grid'>
        <g k9-elem="grid">
            <xsl:copy-of select="@*" />
            <rect x="0" y="0" pp:width="$width" pp:height="$height" width="0" height="0" class="grid-back"/>
            <xsl:apply-templates/>
        </g>
    </xsl:template>

    <xsl:template match='adl:cell'>
        <g k9-elem="cell">
            <xsl:copy-of select="@*" />
            <rect x="0" y="0" pp:width="$width" pp:height="$height" width="0" height="0" class="cell-edge"/>
            <xsl:apply-templates/>
        </g>
    </xsl:template>

    <xsl:template match="adl:socket">
        <g k9-elem="socket" >
            <xsl:copy-of select="@*" />
            <ellipse cx="0" cy="0" rx="3" ry="3" class="socket-circle" />
        </g>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*">
        <g>
            <xsl:attribute name="k9-elem"><xsl:value-of select="name(.)" /></xsl:attribute>
            <xsl:copy-of select="@*" />
            <xsl:apply-templates />
        </g>
    </xsl:template>



</xsl:stylesheet>