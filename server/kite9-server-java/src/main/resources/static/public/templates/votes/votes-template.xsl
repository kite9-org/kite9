<xsl:stylesheet 
 xmlns="http://www.w3.org/2000/svg" 
 xmlns:svg="http://www.w3.org/2000/svg"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:adl="http://www.kite9.org/schema/adl"
 xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">
 
 <xsl:template name="votes">
    <svg:rect x="-5pt" y="-5pt" width="52pt" height="26pt" rx="8pt" ry="8pt" class="back" />
    <svg:text x="8pt" y="9pt" class="font-awesome">&#xf005;</svg:text> 
    <svg:text x="30pt" y="9pt" class="count">#{@count}</svg:text>
 </xsl:template>

</xsl:stylesheet>