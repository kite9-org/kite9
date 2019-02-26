
Kite9 uses least-cost optimisation algorithms to place diagram elements to minimize link distances.

It supports labelling of links and decoration of the ends of links, as shown in the following examples:

![Link Example 1](images/l1.png)
![Link Example 2](images/l2.png)
![Link Example 3](images/l3.png)
![Link Example 4](images/l4.png)
![Link Example 5](images/l5.png)

## Adding a Link / Link End

(Subject to change)

A link needs to have `kite9-type: link` css property set.  e.g.

```xml
<svg:svg>
  ...
  <diagram xmlns="http://www.kite9.org/schema/adl" id="The Diagram">
  <glyph id="one">
    <label id="one-label">One</label>
  </glyph>
  <glyph id="two">
    <label id="one-label">Two</label>
  </glyph>
  <link id="tl1" style="kite-type: link">
    <from reference="meets" style="kite9-type: link-end" />
    <to reference="one" style="kite9-type: link-end"/>
  </link>
</diagram>
</svg:svg>
 
Currently, links must be nested as child of the diagram element. 

## Labels

tbc.  

