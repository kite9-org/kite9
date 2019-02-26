

## `kite9-type`

Must be applied to all elements, otherwise an exception is thrown.  Legal values are:

 - `diagram`: indicates that the element is a whole diagram, all the content is processed in one go.
 - `text`: indicates that the text content of the element should be rendered as SVG `text`.
 - `link`: indicates that the element should be rendered as a link between two other elements.
 - `link-end`: part of a link, that tells it where to join to.  
 - `none`: ignore this element from the transform
 - `svg`: element contains SVG that should be output onto the diagram.  The size of the rendered element will be large enough to contain the SVG within it.
 - `container`: element is a container for other elements.  The size of the element will depend on it's contents (possibly other `text`, `svg` or `container` elements).

Generally, you will have an SVG document containing a single `diagram` element. This will contain a nested structure of `container`s, with `svg` and `text` elements inside them.  

### Output

 - When rendered, all elements are turned into SVG `<g>` tags apart from `none` elements, which are not rendered at all.  
 - The original structure of the input XML document is partially preserved in the structure of the `<g>` elements.
 - The original element `id` is kept.
 - The original element tag is added as a class, prefixed with `kite9-`.
 
Example:

```xml
<a style="kite9-type: diagram" id="one">
  <b style="kite9-type: container" id="two">
    <c style="kite9-type: svg" id="three">
      <svg:circle ... />
    </c>
  </b>
</a>
```

Will be rendered as:

```xml
<svg:g class="kite9-a" id="one">
  <svg:g class="kite9-b" id="two">
    <svg:g class="kite9-c" id="three">
      <svg:circle ... />
    </svg:g>
  </svg:g>
</svg:g>
```


## `kite9-sizing`

Currently, this can be either `minimize` or `maximize`.  If minimize, the element will occupy the smallest amount of space in the output.  However, if set to maximize, the element will consume free space around itself as far as it can.

Default is `minimize`.

## `kite9-usage`

When laying out a `container`, usually, the contents are all treated the same way, as regular elements of content which must be contained (see [kite9-layout](#kite9-layout), below).   

However, sometimes, a container might have other element types within it:

- `label`:  the element is a label for the container.  Currently, this is placed along the bottom edge of the container.
- 'decal': a decal is an element placed _over_ the container, and is rendered after the container size is known.  This means the `decal` can "decorate" the container.   Decals don't therefore affect the size of the container.

## `kite9-layout`

For elements with `kite9-type=container`, you can specify how the contents will be laid out.  Currently, the following are supported:
	
 - `horizontal`:  contents are placed in the container in any horizontal order.
 - `vertical`: contents are placed in the container in any horizontal order.
 - `left`: contents are laid out right-to-left;
 - `right`: contents are laid out left-to-right;
 - `up`:  contents are laid out bottom-to-top;
 - `down`: contents are laid out top-to-bottom;
 - `grid`: See [Grids](Grids.md)

If no container layout is specified, there are no constraints about where elements sit within the container, and Kite9 will attempt to minimize links and link crossings between elements within the container and outside it.

## `kite9-transform`

This determines how SVG content is placed within the `<g>` element.  Usually, the `<g>` will get a transform so that the content appears in the right place.

This can be:

 - `none`: No transform, just outputs the SVG.
 - `position`: Positions the 0,0 point of the contained SVG at the top-left corner of the containing element.  This is the default for `kite9-type=text` contents.
 - `crop`: Positions the top-leftmost point of the contained SVG at the top-left corner of the containing element.  This is the default for `kite9-type=svg` and most other things.
 - `rescale`:  This is useful for decals, and it sizes the decal the same as the containing element.
 
## Example 1

This example draws an ellipse inside a light blue rounded rectangle.

The following input contains a diagram, which has a `<rect>` element as a container for the `<shape>` element (which contains a circle).  In order to be able to "see" the container, we add the `<decal>` to it, which is able to display an SVG `<rect>` element, the same size as the circle.  [Templating](Templating.md) allows the `width="#{$width}" height="#{$height}"` settings to be resolved after the main circle is sized.

Second, note how we are using "crop" for the ellipse.  Although it is scaled (with the `scale(3,3)` transform), it still completely occupies the `<rect>`.


```xml
  <svg:svg xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svg="http://www.w3.org/2000/svg">
    <diagram xmlns="http://www.kite9.org/schema/adl" id="The Diagram">
      <rect style="kite9-type: container;  ">
        <decal style="kite9-usage: decal; kite9-type: svg; ">
          <svg:rect x="0" y="0" width="#{$width}" height="#{$height}" rx="8" ry="8" style="fill: lightblue; " />
        </decal>
        <shape style="kite9-type: svg; ">
          <svg:g transform="scale(3, 3)">
        	    <svg:ellipse cx="20" cy="20" rx="20" ry="20" stroke="black" stroke-width="1"/>      
          </svg:g>      
        </shape>    
      </rect>
    </diagram>
  </svg:svg>
```

We can output:

```xml
<svg>
  ...
  <g id="The Diagram" class=" kite9-diagram" kite9-elem="diagram">
    <g style="kite9-type: container;" class="kite9-rect" kite9-elem="rect" transform="translate(15.0,15.0)">
      <g style="kite9-usage: decal; kite9-type: svg; " debug="" class=" kite9-decal" kite9-elem="decal">
        <rect x="0" y="0" width="123.0" style="fill: lightblue; " rx="8" class="glyph-back" ry="8" height="123.0"/>
      </g>
      <g style="kite9-type: svg; " class="kite9-shape" kite9-elem="shape" transform="translate(1.5,1.5)">
        <g transform="scale(3, 3)">
          <ellipse rx="20" ry="20" stroke-width="1" cx="20" cy="20" stroke="black"/>      
        </g>    
      </g>   
    </g>
  </g>
</svg>
```

![Resulting Image](images/ellipse.png)

### Notes:

- Notice how the `translates` are set on the `<g>`s.  These are computed by analysing renderings of the elements. 
- See how the `width` and `height` of the `<rect>` have been computed, based on the rendered size of the `<ellipse>`.

## Example 2

Here, we can size a rectangle using text elements.  In order for this to work, Kite9 needs to know exactly how big the rendered text elements will be.

![Resulting Image](images/text.png)

- The container `<rect>` has a `kite9-min-size` set, but because "Hello Something Else" is so long, it makes the container even bigger.
- All of the items of text need to be sized correctly, so that they can be positioned within the shape.
- Again, we use a rounded rectangle background to contain everything.

```xml
<svg:svg>
  ...
  <diagram xmlns="http://www.kite9.org/schema/adl" id="The Diagram">
    <rect style="kite9-type: container; kite9-padding: 10px; kite9-layout: down; kite9-min-size: 150px 150px ">
      <decal style="kite9-usage: decal; kite9-type: svg; ">
        <svg:rect x="0" y="0" width="#{$width}" height="#{$height}" rx="8" ry="8" style="fill: url(#glyph-background); " class="glyph-back"/>
      </decal>
      <text style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 25px;">
        hello something else
      </text>
      <text style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 25px;">
        hello 2
      </text>
      <text style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 15px;kite9-vertical-align: center; kite9-horizontal-align: center; text-align: middle;">
        bette
middler
      </text>
      <text style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 15px; kite9-vertical-align: bottom; kite9-horizontal-align: right; text-align: end;">
        hello
 bottom
      </text>
    </rect>
  </diagram>
</svg:svg>
``` 

This is rendered like this:

```xml
<svg>
  <g id="The Diagram"  class=" kite9-diagram" kite9-elem="diagram">
    <g style="kite9-type: container; kite9-padding: 10px; kite9-layout: down; kite9-min-size: 150px 150px " class=" kite9-rect" kite9-elem="rect" transform="translate(15.0,15.0)">
      <g style="kite9-usage: decal; kite9-type: svg; " debug="" class=" kite9-decal" kite9-elem="decal">
        <rect x="0" y="0" width="247.0" style="fill: url(#glyph-background); " rx="8" class="glyph-back" ry="8" height="160.0"/>
      </g>
      <g style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 25px;" class=" kite9-text" kite9-elem="text" transform="translate(7.8,13.3)">
        <g font-family="'opensans-light-webfont'" font-size="25px">
          <text x="0" xml:space="preserve" y="23.4485" stroke="none">
           <tspan>h</tspan><tspan>ello </tspan><tspan>something </tspan><tspan>else</tspan>
          </text>
        </g>
      </g>
      <g style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 25px;" class=" kite9-text" kite9-elem="text" transform="translate(85.8,47.3)">
        <g font-family="'opensans-light-webfont'" font-size="25px">
          <text x="0" xml:space="preserve" y="23.4485" stroke="none">
            <tspan>h</tspan><tspan>ello </tspan><tspan>2</tspan>
          </text>
        </g>
      </g>
      <g style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 15px; kite9-vertical-align: center; kite9-horizontal-align: center; text-align: middle;" class=" kite9-text" kite9-elem="text" transform="translate(-4877.4,80.0)">
        <g font-family="'opensans-light-webfont'" font-size="15px">
          <text x="4982.2793" xml:space="preserve" y="14.0691" stroke="none">
            <tspan>b</tspan><tspan>ette</tspan></text><text x="4974.0244" xml:space="preserve" y="30.5691" stroke="none">middler
          </text>
        </g>
      </g>
      <g style="kite9-type: text; font-family:  opensans-light-webfont; font-size: 15px; kite9-vertical-align: bottom; kite9-horizontal-align: right; text-align: end;" class=" kite9-text" kite9-elem="text" transform="translate(-9761.5,116.0)">
        <g font-family="'opensans-light-webfont'" font-size="15px">
          <text x="9967.3486" xml:space="preserve" y="14.0691" stroke="none">
            <tspan>h</tspan><tspan>ello</tspan>
          </text>
          <text x="9950.2031" xml:space="preserve" y="30.5691" stroke="none">bottom</text>
        </g>
      </g>
    </g>
  </g>
</svg>
```
