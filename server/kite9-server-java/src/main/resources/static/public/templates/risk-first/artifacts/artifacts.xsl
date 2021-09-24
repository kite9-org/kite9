<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../../formats/formats-template.xsl" />
  
  <xsl:template name="action" match="adl:action">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-ui">drag delete align connect autoconnect edit vote</xsl:with-param>
      <xsl:with-param name="k9-shape">
        <g k9-texture="background" k9-highlight="pulse" >
          <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px)</xsl:attribute>
          <polygon points="" pp:points="
            0 0, 
            [[$width - (15 * $pt)]] 0,
            [[$width]] [[$height div 2]],
            [[$width - (15 * $pt)]] [[$height]],
            0 [[$height]]"  />
        </g>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="risk-action-left" match="adl:action[contains(@class, 'left')]" priority="5">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-ui">drag delete align connect autoconnect edit vote</xsl:with-param>
      <xsl:with-param name="k9-shape">
        <g k9-texture="background" k9-highlight="pulse" >
          <xsl:attribute name="pp:style">transform: translate([[$x]]px,[[$y]]px)</xsl:attribute>
          <polygon points="" pp:points="
            0 [[$height div 2]], 
            [[15 * $pt]] 0, 
            [[$width]] 0, 
            [[$width]] [[$height]], 
            [[15 * $pt]] [[$height]],
            0 [[$height div 2]]" />
        </g>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="arrow" match="adl:arrow">
    <xsl:call-template name="formats-text-shape-inline">
      <xsl:with-param name="k9-ui">drag delete align connect autoconnect edit</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="artifact" match="adl:artifact |  
    adl:document |
    adl:users |
    adl:computer | 
    adl:interface |
    adl:component |
    adl:channel-artifact |
    adl:internal-model-artifact |
    adl:protocol-artifact |
    adl:agent-artifact |
    adl:image-artifact">
    <xsl:call-template name="formats-text-image-portrait">
      <xsl:with-param name="k9-ui">drag delete align connect autoconnect edit</xsl:with-param>
      <xsl:with-param name="k9-texture-text">artifact</xsl:with-param>
      <xsl:with-param name="k9-texture-back">none</xsl:with-param>
      <xsl:with-param name="k9-ui-depiction"></xsl:with-param>
      <xsl:with-param name="href">
        <xsl:choose>
          <xsl:when test="name() = 'agent-artifact'">
            /public/templates/risk-first/redesign/risks/agency_risk_v2.svg
          </xsl:when>
          
          <xsl:when test="name() = 'document'">
            /public/templates/risk-first/redesign/artifacts/document_v2.svg
          </xsl:when>
          
          <xsl:when test="name() = 'users'">
            /public/templates/risk-first/redesign/artifacts/users_v2.svg
          </xsl:when>
          
          <xsl:when test="name() = 'interface'">
            /public/templates/risk-first/redesign/artifacts/interface_v2.svg
          </xsl:when>
          
          <xsl:when test="name() = 'protocol-artifact'">
            /public/templates/risk-first/redesign/risks/protocol_risk_v2.svg
          </xsl:when>
          
          <xsl:when test="name() = 'internal-model-artifact'">
            /public/templates/risk-first/redesign/risks/internal_model_risk.svg
          </xsl:when>
          
          <xsl:when test="name() = 'channel-artifact'">
            /public/templates/risk-first/redesign/risks/channel_risk_v2.svg
          </xsl:when>
          
          <xsl:when test="name() = 'component'">
            /public/templates/risk-first/redesign/artifacts/component_v2.svg
          </xsl:when>
          <xsl:otherwise>
            /public/templates/risk-first/redesign/artifacts/document_v2.svg
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:call-template>  
  </xsl:template>
    
  <xsl:template name="generic-artifact" match="adl:generic-artifact">
    <xsl:call-template name="formats-text-image-portrait">
      <xsl:with-param name="k9-ui">drag delete align connect autoconnect edit</xsl:with-param>
      <xsl:with-param name="texture-text">artifact</xsl:with-param>
      <xsl:with-param name="texture-back">none</xsl:with-param>
    </xsl:call-template>  
  </xsl:template>  
  
</xsl:stylesheet>