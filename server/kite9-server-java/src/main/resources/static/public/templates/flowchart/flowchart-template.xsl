<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:adl="http://www.kite9.org/schema/adl"
  xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

  <xsl:import href="../adl/adl.xsl" />
  <xsl:import href="shapes-template.xsl" />

  
  <xsl:template match="/">
    <xsl:call-template name="diagram-root-svg">
      <xsl:with-param name="css">
        @import url('/public/templates/flowchart/flowchart.css');
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
        import '/public/templates/flowchart/flowchart.js';
      </xsl:with-param>
      <xsl:with-param name="defs">
        <xsl:copy-of select="$links-markers" />
        <xsl:copy-of select="$container-indicators" />
        <xsl:copy-of select=".//svg:defs" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:document | adl:decision">
    <xsl:call-template name="containers-basic">
       <xsl:with-param name="k9-shape">
        <xsl:apply-templates select="." mode="flowchart-shape" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="adl:textarea">
    <xsl:call-template name="formats-textarea" />
  </xsl:template>
  
  
  
<!-- 

[k9-elem=stored] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#stored');
}

[k9-elem=process] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#process');
}

[k9-elem=document] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#document');
}

[k9-elem=delay] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#delay');
}

[k9-elem=start] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#start');
}

[k9-elem=manual] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#manual');
}

[k9-elem=terminator] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#terminator');
}

[k9-elem=input] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#input');
}

[k9-elem=database] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#database');
}

[k9-elem=preparation] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#preparation');
}

[k9-elem=internal] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#internal');
}

[k9-elem=off-page] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#off-page');
}

[k9-elem=direct] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#direct');
}

[k9-elem=predefined] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#predefined');
}

[k9-elem=display] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#display');
}

[k9-elem=start] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#start');
}

[k9-elem=loop-limit] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#loop-limit');
}

[k9-elem=stored] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#stored');
}

[k9-elem=sequential] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#sequential');
}

[k9-elem=reference] > [k9-elem=back] {
  kite9-template: url('flowchart-shapes-template.xml#reference');
}
  -->
  
</xsl:stylesheet>  