




1.  We have lots of individual XML files, css files, templates etc.

2.  Kite9SVGTranscoder is responsible for turning these into SVG.



3.  GVTBuilder:  this turns an XML tree into a GraphicsNode tree.   To do this, it uses the Kite9BridgeContext, which knows how to convert every XML element into a corresponding GraphicsNode.

4.  To do this, we use Batik Bridges.  A bridge converts a single type of xml element into a single type of graphics node.  

5.  We have a Kite9DiagramBridge, which knows how to call the ArrangementPipeline and position all the diagram elements.

6.  All other kite9 elements are converted with the Kite9Bridge, which simply does Group GraphicsNodes.

7.  GraphicsNodes are used for sizing content in leaf elements, mainly.
