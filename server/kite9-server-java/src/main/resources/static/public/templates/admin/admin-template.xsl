<xsl:stylesheet 
  xmlns="http://www.w3.org/2000/svg"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:adl="http://www.kite9.org/schema/adl"
	version="1.0">
	
  <xsl:import href="/public/templates/common/common.xsl" />
  <xsl:import href="/public/templates/containers/containers-template.xsl" />
  <xsl:import href="/public/templates/common/diagram/diagram-template.xsl" />
  <xsl:import href="/public/templates/common/formats/formats-template.xsl" />
  
  
  <xsl:template match="/">
    <xsl:call-template name="diagram-root-svg">
      <xsl:with-param name="css">
        @import url('/public/templates/admin/admin.css');
      </xsl:with-param>
      <xsl:with-param name="script">
        import '/public/templates/admin/admin.js';
      </xsl:with-param>
      <xsl:with-param name="defs">
        <xsl:copy-of select="$container-indicators" />
        <clipPath id="iconCircle-100pt">
          <circle r="50pt" cx="50pt" cy="50pt" />
        </clipPath>
      </xsl:with-param>
      <xsl:with-param name="constants">
        document.params = {
        };
      </xsl:with-param>
    </xsl:call-template>    
  </xsl:template>
  
  <xsl:template match="adl:diagram">
    <xsl:call-template name="diagram-basic">
      <xsl:with-param name="content">
         <xsl:apply-templates />
      </xsl:with-param>
    
      
      
        <!-- pill trail first -->
        <!-- <g k9-elem="container" class="trail">
          <xsl:choose>
            <xsl:when test="count(adl:parents/*) > 0">
                <xsl:for-each select="adl:parents">
                  <xsl:call-template name="pill-box" />
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
    
            </xsl:otherwise>
          </xsl:choose>
  
          <xsl:call-template name="pill-box" />
        </g>
      </xsl:with-param> -->
    </xsl:call-template>
  
  
  
   <!-- <g>
      <xsl:attribute name="k9-elem">diagram</xsl:attribute>
      <xsl:attribute name="k9-ui">open</xsl:attribute>
      <xsl:attribute name="k9-texture">outline</xsl:attribute>
        
      
      
         <xsl:call-template name="container">
          <xsl:with-param name="contents">
            <hi />
          </xsl:with-param>
        
        </xsl:call-template> -->
      
     <!--    <xsl:attribute name="k9-elem">container</xsl:attribute>
        <xsl:attribute name="k9-ui">open</xsl:attribute>
        <xsl:attribute name="k9-texture">outline</xsl:attribute>
        
        <xsl:call-template name="back-round-rect">
          <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
          <xsl:with-param name="k9-rounding">15pt</xsl:with-param>
        </xsl:call-template>
         -->
       <!--  <xsl:if test="adl:type='user' or adl:type='organisation'">
          <g class="grid" id="repositories">
            <xsl:for-each select="adl:repositories">
              <xsl:call-template name="entity-box" />
            </xsl:for-each>
            <label>Repositories</label>
          </g>    
        </xsl:if>

        <xsl:if test="adl:type='user'">
          <g k9-elem="container" class="grid" id="organisations">
            <xsl:for-each select="adl:organisations">
              <xsl:call-template name="entity-box" />
            </xsl:for-each>
            <label>Organisations</label>
          </g>
        </xsl:if>
      
        <xsl:if test="adl:type='repository' or adl:type='directory'">
          <g k9-elem="container" class="grid" id="documents" k9-ui="NewDocument">
            <xsl:attribute name="subject-uri">
              <xsl:value-of select="adl:links[adl:rel='self']/adl:href" />
            </xsl:attribute>
            <xsl:for-each select="adl:documents">
              <xsl:call-template name="entity-box" />
            </xsl:for-each>
            <label>Diagrams</label>
          </g>  
  
          <g k9-elem="container"  class="grid" id="subDirectories">
            <xsl:for-each select="adl:subDirectories">
              <xsl:call-template name="entity-box" />
            </xsl:for-each>
            <label>Sub Directories</label>
          </g>
        </xsl:if> 
      </g>  
    </g> -->
 
  </xsl:template>


	<xsl:template name="entity-box">
    <xsl:call-template name="containers-basic">
    
    
    
    </xsl:call-template>
		<!-- <entity>
			<xsl:attribute name="id">
				<xsl:value-of select="adl:links[adl:rel='self']/adl:href" />
			</xsl:attribute>
			<xsl:attribute name="subject-uri">
				<xsl:value-of select="adl:links[adl:rel='self']/adl:href" />
			</xsl:attribute>
			<xsl:attribute name="k9-ui">
				<xsl:value-of select="adl:commands" />
			</xsl:attribute>
			<xsl:copy-of select="adl:icon" />
			<xsl:copy-of select="adl:title" />
			<xsl:copy-of select="adl:description" />
		</entity> -->
	</xsl:template>
  
  <xsl:template match="adl:documents | adl:repositories" >
    <xsl:call-template name="entity-box" />
  </xsl:template>
	
  
	<xsl:template name="pill-box">
		<g k9-elem="pill">
			<xsl:attribute name="id">
				<xsl:value-of select="adl:links[adl:rel='self']/adl:href" />
			</xsl:attribute>
			<xsl:attribute name="subject-uri">
				<xsl:value-of select="adl:links[adl:rel='self']/adl:href" />
			</xsl:attribute>
			<xsl:attribute name="k9-ui">
				<xsl:value-of select="adl:commands" />
			</xsl:attribute>
      <xsl:call-template name="admin-icon">
        <xsl:with-param name="url"><xsl:value-of select="adl:icon" /></xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="formats-inline-text">
        <xsl:with-param name="text"><xsl:value-of select="adl:title" /></xsl:with-param>
        <xsl:with-param name="k9-elem">title</xsl:with-param>
      </xsl:call-template>
		</g>
	</xsl:template>
	

  <!-- Basically a copy of common-image-template except for clip-path -->
  <xsl:template name="admin-icon">
    <xsl:param name="size">100pt</xsl:param>
    <xsl:param name="url">/public/templates/admin/icons/adl.svg</xsl:param>
    <g>
        <xsl:attribute name="k9-elem">icon</xsl:attribute>
        <xsl:attribute name="k9-texture">outline</xsl:attribute>
        <rect x="0" y="0" width="{$size}" height="{$size}" fill="none" stroke="white" stroke-opacity=".01" />
        <g style="kite9-type: svg; kite9-usage: decal;">
          <image x="0" y="0" width="{$size}" height="{$size}" xlink:href="{$url}" />
        </g>
    </g>
  </xsl:template>

  <xsl:template name="adl:title">
    <xsl:call-template name="inline-text">
      <xsl:with-param name="text"><xsl:value-of select="text()" /></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:icon">
    <xsl:call-template name="admin-icon">
      <xsl:with-param name="url"><xsl:value-of select="text()" /></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

<!-- 	<xsl:template match="adl:diagram">
		<xsl:call-template name="diagram">
      <xsl:with-param name="contents">
        <adl:main-page>
          <adl:
        </adl:main-screen>
      </xsl:with-param>
    </xsl:call-template>
	</xsl:template> -->

</xsl:stylesheet>