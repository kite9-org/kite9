<xsl:stylesheet xmlns="http://www.w3.org/2000/svg" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:adl="http://www.kite9.org/schema/adl" xmlns:pp="http://www.kite9.org/schema/post-processor" version="1.0">


 <xsl:variable name="links-markers">
  <defs id="links-markers">

   <!-- Link Ends -->

   <marker id="-start-marker">
   </marker>

   <marker id="-end-marker">
   </marker>

   <marker id="strike-start-marker" markerWidth="8" markerHeight="6" refX="1" refY="3" orient="auto">
    <path d="
        M7,0 L5,6 h-1 L6,0z" class="strike-marker"></path>
   </marker>

   <marker id="strike-end-marker" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
    <path d="
        M3,0 L1,6 h-1 L2,0z" class="strike-marker"></path>
   </marker>

   <marker id="circle-start-marker" markerWidth="6" markerHeight="6" refX="3" refY="3">
    <circle cx="3" cy="3" r="2" class="circle-marker"></circle>
   </marker>

   <marker id="circle-end-marker" markerWidth="6" markerHeight="6" refX="3" refY="3">
    <circle cx="3" cy="3" r="2" class="circle-marker"></circle>
   </marker>

   <marker id="diamond-start-marker" markerWidth="8" markerHeight="6" refX="2" refY="3" orient="auto">
    <polygon points="1,3 4,1 7,3 4,5" class="diamond-marker"></polygon>
   </marker>

   <marker id="diamond-end-marker" markerWidth="8" markerHeight="6" refX="6" refY="3" orient="auto">
    <polygon points="1,3 4,1 7,3 4,5" class="diamond-marker"></polygon>
   </marker>

   <marker id="open-diamond-start-marker" markerWidth="8" markerHeight="6" refX="5" refY="3"
    orient="auto">
    <path d="
        M0,3 L3,1 L6,3 L5,3 L3,1.66 L1,3
            L3,4.33 L5,3 L6,3 L3,5 L0,3"
     class="open-diamond-marker"></path>
   </marker>

   <marker id="open-diamond-end-marker" markerWidth="8" markerHeight="6" refX="1" refY="3" orient="auto">
    <path d="
        M6,3 L3,1 L0,3 L1,3 L3,1.66 L5,3
            L3,4.33 L1,3 L0,3 L3,5 L6,3"
     class="open-diamond-marker"></path>
   </marker>

   <marker id="barbed-arrow-end-marker" markerWidth="7" markerHeight="7" refX="4" refY="4" orient="auto">
    <path d="M2,2 L6,4 L2,6 L4,4 L2,2" class="barbed-arrow-marker"></path>
   </marker>

   <marker id="barbed-arrow-start-marker" markerWidth="7" markerHeight="7" refX="4" refY="4"
    orient="auto">
    <path d="M6,2 L2,4 L6,6 L4,4 L6,2" class="barbed-arrow-marker"></path>
   </marker>

   <marker id="open-arrow-end-marker" markerWidth="7" markerHeight="7" refX="1" refY="4" orient="auto">
    <path d="
        M6,4 L1,2 L1,3 L4,4 L1,5 L1,6 L6,4
        M2,3 h-1 v2 h1z" class="open-arrow-marker"></path>
   </marker>

   <marker id="open-arrow-start-marker" markerWidth="7" markerHeight="7" refX="6" refY="4" orient="auto">
    <path d="
        M1,4 L6,2 L6,3 L3,4 L6,5 L6,6 L1,4
        M5,3 h1 v2 h-1z" class="open-arrow-marker"></path>
   </marker>

   <marker id="arrow-start-marker" markerWidth="7" markerHeight="7" refX="4" refY="4" orient="auto">
    <polygon points="2,4 6,2 6,6" class="arrow-marker"></polygon>
   </marker>

   <marker id="arrow-end-marker" markerWidth="7" markerHeight="7" refX="4" refY="4" orient="auto">
    <polygon points="6,4 2,2 2,6" class="arrow-marker"></polygon>
   </marker>

   <marker id="crow-end-marker" markerWidth="7" markerHeight="7" refX="5" refY="4" orient="auto">
    <path d="
         M1,4 L4,1 l0.66 0.66 L2.3,4z
         M1,4 L4,7 l0.66 -0.66 L2.3,4z"
     class="crow-marker"/>
   </marker>

   <marker id="crow-start-marker" markerWidth="7" markerHeight="7" refX="1" refY="4" orient="auto">
    <path d="
         M5,4 L2,1 l-0.66 0.66 L3.66,4z
         M5,4 L2,7 l-0.66 -0.66 L3.66,4z"
     class="crow-marker"/>
   </marker>

   <marker id="square-end-marker" markerWidth="7" markerHeight="7" refX="2" refY="2" orient="auto">
    <rect x="0" y="0" width="4" height="4" class="square-marker"/>
   </marker>

   <marker id="square-start-marker" markerWidth="7" markerHeight="7" refX="2" refY="2" orient="auto">
    <rect x="0" y="0" width="4" height="4" class="square-marker"/>
   </marker>

  </defs>
 </xsl:variable>
</xsl:stylesheet>