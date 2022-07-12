<xsl:stylesheet 
  xmlns="http://www.w3.org/2000/svg"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:adl="http://www.kite9.org/schema/adl"
	version="1.0">
	
  <xsl:import href="../adl/adl-all-components.xsl" />
  
  <xsl:template match="/" mode="diagram-defs">
   	<xsl:call-template name="adl-diagram-defs" />
    <clipPath id="iconCircle-100pt">
      <circle r="50pt" cx="50pt" cy="50pt" />
    </clipPath>
   </xsl:template>
   
   <xsl:template match="/" mode="diagram-script">
  	 import '/github/kite9-org/kite9/templates/admin/admin.js?v=v0.9'
   </xsl:template>
   
   <xsl:template match="/" mode="diagram-element-css">
     <xsl:call-template name="adl-diagram-element-css" />
     <adl:css>@import url('/github/kite9-org/kite9/templates/admin/admin-elements.css?v=v0.9');</adl:css>     
   </xsl:template>
  
   <xsl:template match="/" mode="diagram-palettes">
   </xsl:template>
   
   <xsl:template match="/" mode="diagram-texture-css">
     <adl:css>@import url('/github/kite9-org/kite9/templates/admin/admin-textures.css?v=v0.9');</adl:css>
   </xsl:template>
   
   
   <xsl:template match="adl:diagram">
   	<xsl:call-template name="formats-container">
   		<xsl:with-param name="k9-elem">admin</xsl:with-param>
   		<xsl:with-param name="k9-texture">none</xsl:with-param>
   		<xsl:with-param name="content">
		   	<!--  trail -->
		    <xsl:call-template name="formats-container">
		    	<xsl:with-param name="class">trail</xsl:with-param>
		   		<xsl:with-param name="k9-elem">container</xsl:with-param>
		   		<xsl:with-param name="k9-texture">none</xsl:with-param>
		    	<xsl:with-param name="content">
		      		<xsl:apply-templates select="./adl:parents[adl:links]" mode="pill" />
		      		<xsl:apply-templates select="." mode="pill">
		      			<xsl:with-param name="chevron"></xsl:with-param>
		      		</xsl:apply-templates>
		      	</xsl:with-param>
		     </xsl:call-template>
		        
		     <!-- main -->  
		     <xsl:call-template name="formats-container">
		     	<xsl:with-param name="class">main</xsl:with-param>
		   		<xsl:with-param name="k9-elem">container</xsl:with-param>
		   		<xsl:with-param name="k9-texture">outline</xsl:with-param>
		        <xsl:with-param name="content">
		        
		        	<xsl:if test="adl:type='user' or adl:type='organisation'">
					  <xsl:call-template name="formats-container">
					  	<xsl:with-param name="class">grid</xsl:with-param>
		   				<xsl:with-param name="k9-elem">container</xsl:with-param>
				   		<xsl:with-param name="k9-texture">outline</xsl:with-param>
		   				<xsl:with-param name="id">repositories</xsl:with-param>
		   				<xsl:with-param name="k9-ui">NewDocument</xsl:with-param>
		   				<xsl:with-param name="content">
		   					<xsl:apply-templates select="adl:repositories" mode="entity" />
		   					<xsl:call-template name="labels-basic">
		   						<xsl:with-param name="text"><text>Repositories</text></xsl:with-param>
		   					</xsl:call-template>
		   				</xsl:with-param>
					  </xsl:call-template>
			        </xsl:if> 
		    
	    		    <xsl:if test="adl:type='user'">
	    		    	<xsl:call-template name="formats-container">
						  	<xsl:with-param name="class">grid</xsl:with-param>
			   				<xsl:with-param name="k9-elem">container</xsl:with-param>
			 		   		<xsl:with-param name="k9-texture">outline</xsl:with-param>
			   				<xsl:with-param name="id">organisations</xsl:with-param>
			   				<xsl:with-param name="k9-ui"></xsl:with-param>
			   				<xsl:with-param name="content">
			   					<xsl:apply-templates select="adl:organisations" mode="entity" />
			   					<xsl:call-template name="labels-basic">
			   						<xsl:with-param name="text"><text>Organisations</text></xsl:with-param>
			   					</xsl:call-template>
			   				</xsl:with-param>
						</xsl:call-template>
					</xsl:if>


					<xsl:if test="adl:type='repository' or adl:type='directory'">
					  <xsl:call-template name="formats-container">
					  	<xsl:with-param name="class">grid</xsl:with-param>
		   				<xsl:with-param name="k9-elem">container</xsl:with-param>
		   		   		<xsl:with-param name="k9-texture">outline</xsl:with-param>
		   				<xsl:with-param name="id">documents</xsl:with-param>
		   				<xsl:with-param name="k9-ui">NewDocument</xsl:with-param>
		   				<xsl:with-param name="content">
		   					<xsl:apply-templates select="adl:documents" mode="entity" />
		   					<xsl:call-template name="labels-basic">
		   						<xsl:with-param name="text"><text>Documents</text></xsl:with-param>
		   					</xsl:call-template>
		   				</xsl:with-param>
					  </xsl:call-template>
					  
					  <xsl:call-template name="formats-container">
					  	<xsl:with-param name="class">grid</xsl:with-param>
		   		   		<xsl:with-param name="k9-texture">outline</xsl:with-param>
		   				<xsl:with-param name="k9-elem">container</xsl:with-param>
		   				<xsl:with-param name="id">sub-directories</xsl:with-param>
		   				<xsl:with-param name="k9-ui"></xsl:with-param>
		   				<xsl:with-param name="content">
		   					<xsl:apply-templates select="adl:directory | adl:subDirectories" mode="entity" />
		   					<xsl:call-template name="labels-basic">
		   						<xsl:with-param name="text"><text>Sub-Directories</text></xsl:with-param>
		   					</xsl:call-template>
		   				</xsl:with-param>
					  </xsl:call-template>
			        </xsl:if> 
				</xsl:with-param> 	 	
		     </xsl:call-template>
   		</xsl:with-param>
   	</xsl:call-template>
   	
   </xsl:template>
   
   <xsl:template match="*" mode="entity">
   	<xsl:call-template name="formats-text-image-portrait">
   	 	<xsl:with-param name="k9-elem">entity</xsl:with-param>
		<xsl:with-param name="id" select="adl:links[adl:rel='self']/adl:href" />
		<xsl:with-param name="k9-ui" select="adl:commands" />
		<xsl:with-param name="depiction-id"></xsl:with-param>
		<xsl:with-param name="href" select="adl:icon/text()" />
		<xsl:with-param name="width">100pt</xsl:with-param>
		<xsl:with-param name="height">100pt</xsl:with-param>
 		<xsl:with-param name="caption">
 		 
	      <xsl:call-template name="texture-basic">
	      	<xsl:with-param name="class">description</xsl:with-param>
	        <xsl:with-param name="k9-elem">caption</xsl:with-param>
	        <xsl:with-param name="k9-texture">foreground</xsl:with-param>
	        <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
	        <xsl:with-param name="k9-format">text-fixed</xsl:with-param>
	        <xsl:with-param name="shape"></xsl:with-param>
	        <xsl:with-param name="content"><text><xsl:value-of select="adl:description/text()" /></text></xsl:with-param>
	      </xsl:call-template>
	      
	      <xsl:call-template name="texture-basic">
	      	<xsl:with-param name="class">title</xsl:with-param>
	        <xsl:with-param name="k9-elem">caption</xsl:with-param>
	        <xsl:with-param name="k9-texture">foreground</xsl:with-param>
	        <xsl:with-param name="k9-highlight">pulse</xsl:with-param>
	        <xsl:with-param name="k9-format">text-fixed</xsl:with-param>
	        <xsl:with-param name="shape"></xsl:with-param>
	        <xsl:with-param name="content"><text><xsl:value-of select="adl:title/text()" /></text></xsl:with-param>
	      </xsl:call-template>
	    </xsl:with-param>
 		
   	</xsl:call-template>
   </xsl:template>
  
  	<xsl:template match="*" mode="pill">
  		<xsl:param name="chevron">true</xsl:param>
  		<xsl:call-template name="formats-text-image-portrait">
  			<xsl:with-param name="k9-elem">pill</xsl:with-param>
  			<xsl:with-param name="id" select="adl:links[adl:rel='self']/adl:href" />
  			<xsl:with-param name="depiction-id"></xsl:with-param>
  			<xsl:with-param name="k9-ui" select="adl:commands" />
  			<xsl:with-param name="href" select="adl:icon/text()" />
  			<xsl:with-param name="text"><text><xsl:value-of select="adl:title/text()" /></text></xsl:with-param>
  		</xsl:call-template>
  		
  		
  		<xsl:if test="$chevron">
  			<xsl:call-template name="formats-image-fixed">
  				<xsl:with-param name="k9-elem">chevron</xsl:with-param>
   				<xsl:with-param name="href">/github/kite9-org/kite9/templates/admin/icons/chevron.svg</xsl:with-param>
   			</xsl:call-template>
  		</xsl:if>
  		
	</xsl:template>
  
  
</xsl:stylesheet>