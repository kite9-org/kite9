<xsl:stylesheet 
  xmlns="http://www.w3.org/2000/svg"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
	
	<xsl:template match="/">
		<svg contentScriptType="text/ecmascript"
			 width="1267.0px"
			 zoomAndPan="magnify"
			 contentStyleType="text/css"
			 height="1045.0px"
			 preserveAspectRatio="xMidYMid"
			 version="1.0">

			<defs>
				<style type="text/css">
					@import url("<xsl:value-of select="$css-url" />");
				</style>
			</defs>


		<xsl:variable name="css-url">/public/templates/admin/admin.css</xsl:variable>
		<svg:svg zoomAndPan="magnify" preserveAspectRatio="xMidYMid">
			<svg:defs>

			</svg:defs>
			<diagram>
				<xsl:apply-templates />
			</diagram>
		</svg:svg>
	</xsl:template>

	<xsl:template name="entity-box">
		<entity>
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
		</entity>
	</xsl:template>
	
	<xsl:template name="pill-box">
		<pill>
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
		</pill>
	</xsl:template>
	
	<xsl:template name="main-page">
		<container class="trail invis">
			<xsl:choose>
				<xsl:when test="count(adl:parents/*) > 0">
					<xsl:for-each select="adl:parents">
						<xsl:call-template name="pill-box" />
						<icon>/public/templates/admin/icons/chevron.svg</icon>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>

				</xsl:otherwise>
			</xsl:choose>
		
			<xsl:call-template name="pill-box" />
		</container>
		<container class="main">
			<xsl:if test="adl:type='user' or adl:type='organisation'">
				<container class="grid" id="repositories">
				<xsl:for-each select="adl:repositories">
					<xsl:call-template name="entity-box" />
				</xsl:for-each>
				<label>Repositories</label>
				</container>		
			</xsl:if>

			<xsl:if test="adl:type='user'">
				<container class="grid" id="organisations">
					<xsl:for-each select="adl:organisations">
						<xsl:call-template name="entity-box" />
					</xsl:for-each>
					<label>Organisations</label>
				</container>
			</xsl:if>
			
			<xsl:if test="adl:type='repository' or adl:type='directory'">
				<container class="grid" id="documents" k9-ui="NewDocument">
					<xsl:attribute name="subject-uri">
						<xsl:value-of select="adl:links[adl:rel='self']/adl:href" />
					</xsl:attribute>
					<xsl:for-each select="adl:documents">
						<xsl:call-template name="entity-box" />
					</xsl:for-each>
					<label>Diagrams</label>
				</container>	

				<container class="grid" id="subDirectories">
					<xsl:for-each select="adl:subDirectories">
						<xsl:call-template name="entity-box" />
					</xsl:for-each>
					<label>Sub Directories</label>
				</container>
			</xsl:if>		
		</container>
	</xsl:template>

	<xsl:template match="/adl:entity">
		<xsl:call-template name="main-page" />
	</xsl:template>

	<xsl:template match="*">
	</xsl:template>
</xsl:stylesheet>