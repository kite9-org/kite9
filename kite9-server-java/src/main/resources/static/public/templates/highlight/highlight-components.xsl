<xsl:stylesheet
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:adl="http://www.kite9.org/schema/adl"
        xmlns:pp="http://www.kite9.org/schema/post-processor"
        version="1.0">
        
  
  <xsl:template name="highlight-diagram-element-css">
    <adl:css>@import url('/public/templates/highlight/highlight-elements.css');</adl:css>
  </xsl:template>      
    
  <xsl:template name="highlight-diagram-defs">
      <defs id="highlight-indicators">
		<filter id="highlight-visible">
		    <feColorMatrix
		      type="matrix"
		      values="0 0 0 0 .214
		              0 0 0 0 .25
		              0 0 0 0 1
		              0 0 0 1 0 "/>
		  </filter>
		  <filter id="highlight-invisible">
		    <feColorMatrix
		      type="matrix"
		      values="0 0 0 0 0
		              0 0 0 0 0
		              0 0 0 0 0
		              0 0 0 0 0 "/>
		  </filter>
      </defs>
  </xsl:template>
  
    
</xsl:stylesheet>
