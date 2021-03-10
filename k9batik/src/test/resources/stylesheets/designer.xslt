<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:k9="http://www.kite9.org/schema/adl"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        version="1.0" xmlns:xsL="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="base-uri"/>
    <xsl:param name="template-uri"/>
    <xsl:param name="template-path"/>

    <xsl:template name="show-box">
        <rect x="0" y="0"
              k9:width="$width" k9:height="$height"
              width="0"
              height="0"
              rx="8" ry="8" style="fill: url(#glyph-background); "
              class="glyph-back"/>
    </xsl:template>

    <xsl:template match="diagram">
        <svg contentScriptType="text/ecmascript"
             zoomAndPan="magnify"
             contentStyleType="text/css"
             preserveAspectRatio="xMidYMid"
             version="1.0"
             k9-elem="diagram">
            <xsl:attribute name="k9:width">$width</xsl:attribute>
            <xsl:attribute name="k9:height">$height</xsl:attribute>

            <defs>
                <style type="text/css">
                    @import url("<xsl:value-of select="$template-path"/>designer.css");
                </style>
            </defs>

            <g filter="url(#dropshadow)">
                <xsl:apply-templates/>
            </g>

            <defs>
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

                <marker id="circle-end-marker" markerWidth="6" markerHeight="6" refX="3" refY="3">
                    <circle cx="3" cy="3" r="2" class="circle-marker"></circle>
                </marker>

                <marker id="diamond-start-marker" markerWidth="8" markerHeight="6" refX="1" refY="3" orient="auto">
                    <polygon points="1,3 4,1 7,3 4,5" class="diamond-marker"></polygon>
                </marker>

                <marker id="diamond-end-marker" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
                    <polygon points="1,3 4,1 7,3 4,5" class="diamond-marker"></polygon>
                </marker>

                <marker id="diamond-open-start-marker" markerWidth="8" markerHeight="6" refX="1" refY="3" orient="auto">
                    <polygon points="1,3 4,1 7,3 4,5" class="open-diamond-marker"></polygon>
                </marker>

                <marker id="diamond-open-end-marker" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
                    <polygon points="1,3 4,1 7,3 4,5" class="open-diamond-marker"></polygon>
                </marker>

                <marker id="barbed-arrow-end-marker" markerWidth="7" markerHeight="7" refX="6" refY="4" orient="auto">
                    <path d="M2,2 L6,4 L2,6" class="barbed-arrow-marker"></path>
                </marker>

                <marker id="barbed-arrow-start-marker" markerWidth="7" markerHeight="7" refX="2" refY="4" orient="auto">
                    <path d="M6,2 L2,4 L6,6" class="barbed-arrow-marker"></path>
                </marker>

                <marker id="arrow-open-end-marker" markerWidth="7" markerHeight="7" refX="6" refY="4" orient="auto">
                    <polygon points="6,4 2,2 2,6" class="open-arrow-marker"></polygon>
                </marker>

                <marker id="arrow-open-start-marker" markerWidth="7" markerHeight="7" refX="2" refY="4" orient="auto">
                    <polygon points="2,4 6,2 6,6" class="open-arrow-marker"></polygon>
                </marker>

                <marker id="arrow-start-marker" markerWidth="7" markerHeight="7" refX="2" refY="4" orient="auto">
                    <polygon points="2,4 6,2 6,6" class="arrow-marker"></polygon>
                </marker>

                <marker id="arrow-end-marker" markerWidth="7" markerHeight="7" refX="6" refY="4" orient="auto">
                    <polygon points="6,4 2,2 2,6" class="arrow-marker"></polygon>
                </marker>

                <marker id="-start-marker" />
                <marker id="-end-marker" />
                <marker id="gap-start-marker" />
                <marker id="gap-end-marker" />
                <marker id="none-start-marker" />
                <marker id="none-end-marker" />

            </defs>

        </svg>
    </xsl:template>


    <xsl:template match="glyph">
        <g k9-elem="glyph">
            <xsl:copy-of select="@*" />
            <xsl:call-template name="show-box" />
            <xsl:apply-templates/>
        </g>
    </xsl:template>

    <xsl:template match="context">
        <g k9-elem="context">
            <xsl:copy-of select="@*" />
            <rect x='0' y='0' width="0" height="0" k9:width='$width' k9:height='$height' rx='12' ry='12' class="context-back" />
            <xsl:apply-templates/>
        </g>
    </xsl:template>

    <xsl:template match='arrow'>
        <g k9-elem="arrow">
            <xsl:copy-of select="@*" />
            <rect x='0' y='0' width="0" height="0" k9:width='$width' k9:height='$height' rx='4' ry='4' style='fill: black; ' />
            <xsl:apply-templates/>
        </g>
    </xsl:template>

    <xsl:template match="link">
        <g k9-elem="link">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates/>
            <g k9-elem="link-body">
                <path class="link">
                    <xsl:attribute name="k9:d">$path</xsl:attribute>
                    <xsl:attribute name="marker-start">url(#<xsl:value-of select="concat(from/@class,'-start-marker')" />)</xsl:attribute>
                    <xsl:attribute name="marker-end">url(#<xsl:value-of select="concat(to/@class,'-end-marker')" />)</xsl:attribute>
                </path>
            </g>
        </g>
    </xsl:template>

    <xsl:template match="*">
        <g>
            <xsl:attribute name="k9-elem"><xsl:value-of select="name(.)" /></xsl:attribute>
            <xsl:copy-of select="@*" />
            <xsl:apply-templates />
        </g>
    </xsl:template>

    <xsl:template match="glyph/label">
        <g k9-elem="text-label">
            <text class="glyph-label-text"><xsl:apply-templates select="text()" /></text>
        </g>
    </xsl:template>


</xsl:stylesheet>