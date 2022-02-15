<xsl:stylesheet xmlns="http://www.w3.org/2000/svg"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:adl="http://www.kite9.org/schema/adl"
    xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">

	<xsl:template name="twitter-defs">
	  	<clipPath id="avatarClip">
      		<circle cx="80pt" cy="80pt" r="80pt" />
    	</clipPath>
	</xsl:template>

	<xsl:template match="adl:tweet">
		<xsl:call-template name="formats-container">
	 	 		<xsl:with-param name="k9-texture">none</xsl:with-param>
 				<xsl:with-param name="content">
				<xsl:call-template name="avatar" />
				<xsl:call-template name="formats-container">
					<xsl:with-param name="k9-elem">mini-body</xsl:with-param>
					<xsl:with-param name="k9-texture">none</xsl:with-param>
					<xsl:with-param name="content">
						<xsl:call-template name="heading" />
						<xsl:apply-templates />
						<xsl:call-template name="footer" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:with-param>
		
			<xsl:with-param name="k9-rounding">5pt</xsl:with-param>	
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="adl:tweet[@class='big']">
		<xsl:call-template name="formats-container">
 	 		<xsl:with-param name="k9-texture">none</xsl:with-param>
 			<xsl:with-param name="content">
				<xsl:call-template name="formats-container">
					<xsl:with-param name="k9-elem">big-top</xsl:with-param>
	 					<xsl:with-param name="k9-texture">none</xsl:with-param>
						<xsl:with-param name="content">
						<xsl:call-template name="avatar">
							<xsl:with-param name="reply"><xsl:value-of select="@reply" /></xsl:with-param>
						</xsl:call-template>
						<xsl:call-template name="heading">
							<xsl:with-param name="include-date">false</xsl:with-param>
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="formats-container">
					<xsl:with-param name="k9-elem">big-body</xsl:with-param>
					<xsl:with-param name="k9-texture">none</xsl:with-param>
			 		<xsl:with-param name="content">
						<xsl:apply-templates />
						<xsl:call-template name="sent" />
						<xsl:call-template name="social-text" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:with-param>
		
			<xsl:with-param name="k9-rounding">5pt</xsl:with-param>	
		</xsl:call-template>
	</xsl:template>
	
	
	<xsl:template match="adl:avatar" name="avatar">
		<xsl:param name="reply">false</xsl:param>
		<xsl:call-template name="formats-image-fixed">
			<xsl:with-param name="k9-elem">avatar</xsl:with-param>
			<xsl:with-param name="width">160pt</xsl:with-param>
			<xsl:with-param name="height">160pt</xsl:with-param>
			<xsl:with-param name="id"><xsl:value-of select="@id" />@avatar</xsl:with-param>	
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="heading">
		<xsl:param name="include-date">true</xsl:param>
		<xsl:call-template name="formats-container">
			<xsl:with-param name="k9-texture">none</xsl:with-param>
			<xsl:with-param name="k9-elem">heading</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="formats-text-fixed">
					<xsl:with-param name="content"><text><xsl:value-of select="@displayName" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">displayName</xsl:with-param>
				</xsl:call-template>			
				<xsl:call-template name="formats-text-fixed">
					<xsl:with-param name="content"><text><xsl:value-of select="@screenName" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">screenName</xsl:with-param>
				</xsl:call-template>	
				<xsl:if test="contains($include-date,'true')">		
					<xsl:call-template name="formats-text-fixed">
						<xsl:with-param name="content"><text> &#183; <xsl:value-of select="@date" /></text></xsl:with-param>
						<xsl:with-param name="k9-elem">date</xsl:with-param>
					</xsl:call-template>
				</xsl:if>				
			</xsl:with-param>		
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="sent">
		<xsl:call-template name="formats-container">
			<xsl:with-param name="shape">
				<path pp:d="M[[$x]] [[$y]] L [[$width]] [[$y]]" d="" />
				<path pp:d="M[[$x]] [[$y + $height]] L [[$width]] [[$y + $height]]" d="" />
			</xsl:with-param>
			<xsl:with-param name="k9-elem">sent</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="formats-text-fixed">
					<xsl:with-param name="content"><text><xsl:value-of select="@longDate" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">date</xsl:with-param>
				</xsl:call-template>			
				<xsl:call-template name="formats-text-fixed">
					<xsl:with-param name="content"><text> &#183; <xsl:value-of select="@source" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">source</xsl:with-param>
				</xsl:call-template>
			</xsl:with-param>		
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="social-text">
		<xsl:call-template name="formats-container">
			<xsl:with-param name="k9-elem">social-text</xsl:with-param>
			<xsl:with-param name="k9-texture">none</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:if test="@retweets">
					<xsl:call-template name="formats-text-fixed">
						<xsl:with-param name="content"><text><xsl:value-of select="@retweets" /></text></xsl:with-param>
						<xsl:with-param name="class">bold</xsl:with-param>
						<xsl:with-param name="k9-elem">retweets-count</xsl:with-param>
					</xsl:call-template>			
					<xsl:call-template name="formats-text-fixed">
						<xsl:with-param name="content"><text>Retweet<xsl:if test="@retweets != '1'">s</xsl:if></text></xsl:with-param>
						<xsl:with-param name="class">label</xsl:with-param>
						<xsl:with-param name="k9-elem">retweets-label</xsl:with-param>
					</xsl:call-template>
				</xsl:if>			
				<xsl:if test="@quoteTweets">
					<xsl:call-template name="formats-text-fixed">
						<xsl:with-param name="content"><text><xsl:value-of select="@quoteTweets" /></text></xsl:with-param>
						<xsl:with-param name="class">bold</xsl:with-param>
						<xsl:with-param name="k9-elem">quoteTweets-count</xsl:with-param>
					</xsl:call-template>			
					<xsl:call-template name="formats-text-fixed">
						<xsl:with-param name="content"><text>Quote Tweet<xsl:if test="@quoteTweets != '1'">s</xsl:if></text></xsl:with-param>
						<xsl:with-param name="class">label</xsl:with-param>
						<xsl:with-param name="k9-elem">quoteTweets-label</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="@likes">		
					<xsl:call-template name="formats-text-fixed">
						<xsl:with-param name="content"><text><xsl:value-of select="@likes" /></text></xsl:with-param>
						<xsl:with-param name="class">bold</xsl:with-param>
						<xsl:with-param name="k9-elem">likes-count</xsl:with-param>
					</xsl:call-template>			
					<xsl:call-template name="formats-text-fixed">
						<xsl:with-param name="content"><text>Like<xsl:if test="@likes != '1'">s</xsl:if></text></xsl:with-param>
						<xsl:with-param name="class">label</xsl:with-param>
						<xsl:with-param name="k9-elem">likes-label</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
				
			</xsl:with-param>		
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="adl:media">
		<xsl:param name="width">
			<xsl:choose>
				<xsl:when test="contains(ancestor::adl:tweet/@class, 'big')">1700</xsl:when>
				<xsl:otherwise>1540</xsl:otherwise>
			</xsl:choose>
		</xsl:param>
		<xsl:param name="rounding">40</xsl:param>
		<xsl:param name="ratio" select="@height div @width" />
		<xsl:param name="height" select="$width * $ratio" />
		<xsl:call-template name="formats-container">
			<xsl:with-param name="k9-elem">media</xsl:with-param>
			<xsl:with-param name="k9-rounding"><xsl:value-of select="$rounding" />pt</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="formats-image-fixed">
					<xsl:with-param name="k9-elem">media-image</xsl:with-param>
					<xsl:with-param name="image">
				      <image x="0" y="0">
				        <xsl:attribute name="xlink:href"><xsl:value-of select="@href" /></xsl:attribute>
				        <xsl:attribute name="width"><xsl:value-of select="$width" />pt</xsl:attribute>
				        <xsl:attribute name="height"><xsl:value-of select="$height" />pt</xsl:attribute>
						<xsl:attribute name="clip-path">url(#<xsl:value-of select="@id" />-cp)</xsl:attribute>
				      </image>
				    </xsl:with-param>
					<xsl:with-param name="width"><xsl:value-of select="$width" />pt</xsl:with-param>
					<xsl:with-param name="height"><xsl:value-of select="$height" />pt</xsl:with-param>
					<xsl:with-param name="decoration">
						<defs>
						 	<clipPath>
						 		<xsl:attribute name="id"><xsl:value-of select="@id" />-cp</xsl:attribute>
						 		<xsl:choose>
						 			<xsl:when test="@site">
						 				<!-- rounded at the top only -->
						 				<path d="" pp:d="M [[$width div 2 - {$rounding} * $pt]] 0 
						 				h [[$width div 2]]
						 				q [[{$rounding} * $pt]] 0 [[{$rounding} * $pt]] [[{$rounding} * $pt]]
						 				v [[$height]] h [[-$width]] 
						 				v [[-$height]] 
						 				q 0 -[[{$rounding} * $pt]] [[{$rounding} * $pt]] -[[{$rounding} * $pt]]
						 				z" />
						 			</xsl:when>
						 			<xsl:otherwise>
							    		<rect id="rect" x="0" y="0">
							    			<xsl:attribute name="width"><xsl:value-of select="$width" />pt</xsl:attribute>
							    			<xsl:attribute name="height"><xsl:value-of select="$height" />pt</xsl:attribute>
							    			<xsl:attribute name="rx"><xsl:value-of select="$rounding" />pt</xsl:attribute>
							    		</rect>				 			
						 			</xsl:otherwise>
						 		</xsl:choose> 
						 	</clipPath>
						</defs>
					</xsl:with-param>

				</xsl:call-template>
				<xsl:if test="@site">
					<xsl:call-template name="media-info" />					
				</xsl:if>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="media-info">
		<xsl:call-template name="formats-container">
			<xsl:with-param name="k9-elem">media-info</xsl:with-param>
			<xsl:with-param name="k9-texture">none</xsl:with-param>
			
			<xsl:with-param name="content">
				<xsl:call-template name="formats-text-fixed">
					<xsl:with-param name="content"><text><xsl:value-of select="@site" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">site-name</xsl:with-param>
				</xsl:call-template>			
				<xsl:call-template name="formats-text-fixed">
					<xsl:with-param name="content"><text><xsl:value-of select="@title" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">site-title</xsl:with-param>
				</xsl:call-template>			
				<xsl:call-template name="formats-text-fixed">
					<xsl:with-param name="content"><text><xsl:value-of select="@description" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">site-description</xsl:with-param>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="footer">
		<xsl:call-template name="formats-container">
			<xsl:with-param name="k9-texture">none</xsl:with-param>
			<xsl:with-param name="k9-elem">footer</xsl:with-param>
			<xsl:with-param name="content">
				<xsl:call-template name="formats-text-shape-portrait">
					<xsl:with-param name="shape">
						<rect x="2" y="-2" width="20" height="28" fill="white"/>
						<path d="M14.046 2.242l-4.148-.01h-.002c-4.374 0-7.8 3.427-7.8 7.802 0 4.098 3.186 7.206 7.465 7.37v3.828c0 .108.044.286.12.403.142.225.384.347.632.347.138 0 .277-.038.402-.118.264-.168 6.473-4.14 8.088-5.506 1.902-1.61 3.04-3.97 3.043-6.312v-.017c-.006-4.367-3.43-7.787-7.8-7.788zm3.787 12.972c-1.134.96-4.862 3.405-6.772 4.643V16.67c0-.414-.335-.75-.75-.75h-.396c-3.66 0-6.318-2.476-6.318-5.886 0-3.534 2.768-6.302 6.3-6.302l4.147.01h.002c3.532 0 6.3 2.766 6.302 6.296-.003 1.91-.942 3.844-2.514 5.176z"></path>					
					</xsl:with-param>
					<xsl:with-param name="text"><text><xsl:value-of select="@comments" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">comments</xsl:with-param>
				</xsl:call-template>			
				<xsl:call-template name="formats-text-shape-portrait">
					<xsl:with-param name="shape">
						<rect x="2" y="-2" width="20" height="28" fill="white"/>
						<path d="M23.77 15.67c-.292-.293-.767-.293-1.06 0l-2.22 2.22V7.65c0-2.068-1.683-3.75-3.75-3.75h-5.85c-.414 0-.75.336-.75.75s.336.75.75.75h5.85c1.24 0 2.25 1.01 2.25 2.25v10.24l-2.22-2.22c-.293-.293-.768-.293-1.06 0s-.294.768 0 1.06l3.5 3.5c.145.147.337.22.53.22s.383-.072.53-.22l3.5-3.5c.294-.292.294-.767 0-1.06zm-10.66 3.28H7.26c-1.24 0-2.25-1.01-2.25-2.25V6.46l2.22 2.22c.148.147.34.22.532.22s.384-.073.53-.22c.293-.293.293-.768 0-1.06l-3.5-3.5c-.293-.294-.768-.294-1.06 0l-3.5 3.5c-.294.292-.294.767 0 1.06s.767.293 1.06 0l2.22-2.22V16.7c0 2.068 1.683 3.75 3.75 3.75h5.85c.414 0 .75-.336.75-.75s-.337-.75-.75-.75z"></path>
					</xsl:with-param>
					<xsl:with-param name="text"><text><xsl:value-of select="@retweets" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">retweets</xsl:with-param>
				</xsl:call-template>		
				<xsl:call-template name="formats-text-shape-portrait">
					<xsl:with-param name="shape">
						<rect x="2" y="-2" width="20" height="28" fill="white"/>
						<path d="M12 21.638h-.014C9.403 21.59 1.95 14.856 1.95 8.478c0-3.064 2.525-5.754 5.403-5.754 2.29 0 3.83 1.58 4.646 2.73.814-1.148 2.354-2.73 4.645-2.73 2.88 0 5.404 2.69 5.404 5.755 0 6.376-7.454 13.11-10.037 13.157H12zM7.354 4.225c-2.08 0-3.903 1.988-3.903 4.255 0 5.74 7.034 11.596 8.55 11.658 1.518-.062 8.55-5.917 8.55-11.658 0-2.267-1.823-4.255-3.903-4.255-2.528 0-3.94 2.936-3.952 2.965-.23.562-1.156.562-1.387 0-.014-.03-1.425-2.965-3.954-2.965z"></path>	
					</xsl:with-param>
					<xsl:with-param name="text"><text><xsl:value-of select="@likes" /></text></xsl:with-param>
					<xsl:with-param name="k9-elem">likes</xsl:with-param>
				</xsl:call-template>		
			</xsl:with-param>		
		</xsl:call-template>
	</xsl:template>
 
</xsl:stylesheet>
        
        
        

