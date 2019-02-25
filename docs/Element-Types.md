

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

 - `none`,
 - `rescale`:  This is useful for decals, and it sizes the decal the same as the containing element.
 - `position`, 
 - `crop`;
 
## Putting It All Together...

The following input contains a diagram, which has a `<rect>` element as a container within it.  This further has two items of `<text>`.  

```xml
  <diagram id="The Diagram">
    <rect style="kite9-type: container; kite9-padding: 10px; kite9-layout: down; kite9-min-height: 120px ">
      <decal style='kite9-usage: decal; kite9-type: svg; '>
        <svg:rect x='0' y='0' width='#{$width}' height='#{$height}' rx='8' ry='8' style='fill: url(#glyph-background); ' class="glyph-back" />
      </decal>
      <text style="kite9-type: text; font-size: 25px;">
        hello something else
      </text>
      <text style="kite9-type: text; font-size: 15px; kite9-vertical-align: bottom;">
        hello b
      </text>
    </rect>
  </diagram>
```

We can output:


 
