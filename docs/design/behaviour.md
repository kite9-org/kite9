# Behaviour

Behaviour of both the Kite9 Editor and Github browser is provided via Javascript.

## 1.  Separation Of Concerns

behaviours / classes / commands / templates / bundle

- **external**: 3rd party open-source libraries (getting rid of these)
- **bundle**: helper functions.
- **classes**:  provide bits of functionality, like the palette, transition, metadata, shortcut, linker etc.
- **behaviours**:  add event listeners
- **templates**: composes everything in to functions, e.g admin, editor

Note that these are in dependency order: earlier categories cannot access later categories.

On The java side:

- **commands**: provide a way to call back to change the diagram

## 2. Attributes

Attributes are added to SVG elements in order to give them certain behaviours within the Kite9 editor.

### id 

_Implemented by **actionable**, **selectable** and **hoverable**.

Allows hover/select to work.  Without an id, you can’t hover/select.  Ids must be unique.  Id’s can have a part-suffix.  e.g id@part.  The parts may not exist in the ADL, but in the expanded, templated version of the diagram.  

### k9-child

_Implemented by **containers/child**_

You can add a child element. Value of this attribute is the palette#id of the new element.

### k9-label

_Implemented by **labels**_

You can label it.  Value of this attribute is the template for the label.

### k9-highlight

_Implemented by **highlight**_.

Can be any / all of
* **fill**: this is a permanently filled shape
* **pulse**: this is filled when you mouse-over the shape
* **stroke**: permanent change to the stroke effect.
* **outline**: stroke effect change on mouse-over
* **grab**: fills the shape so it can always be grabbed
* **bar**: makes the stroke wider so it can be grabbed 

(all of this happens only if indicators are shown)

### k9-ui

Dictates the things you can choose from the context menu / gestures within the client.

#### Linking

* **align**: Can be aligned with other elements. _Implemented by **links/align**_
* **autoconnect**:  Participates in auto-connect.  _Implemented by **links/autoconnect**_
* **connect** : can be connected to other elements.  _Implemented by **links/link**_

#### Deleting

  - **delete** : allows the element to be deleted (single level)   _Implemented by **selectable/delete**_
  - **cascade** : deletes child elements
  - **orphan** : deleted when the parent is deleted
  
#### Moving

* **drag** : can be dragged around. _Implemented by **dragable**._

#### Editing

* **edit**: we can edit the text  _Implemented by **editable/text**_
  -  **text** content that should be in the editor
* **(none)**: Provides an XML editor for editing the xml of the element  _Implemented by **editable/xml**._  All elements have this (for) now.
* **image**: You can set the image url.  _Implemented by **editable/image**._

#### Attributes

* **layout**: you can set the layout.    _Implemented by **containers/layout**._
  
#### Navigation

* **focus**:  We transition to the URI contained in the ID (used for navigation) _Implemented by **navigable/focus**._
* **open**: means that we try to open the id as a URL.  Different from focus as this opens a whole new page (means reloading JS, good for switching context)   _Implemented by **navigable/open**._

#### Selection

* **grid**: Means that we can do select-row and select-column grid operations.

#### Labels

- **label** you can label with the default label
- **place** you can set the position of a label with one of the values from LabelPlacement.  NB:  this should be combined with port placement eventually - it's wrong as-is.

#### Styling

- **layout**:  allows you to set up `--kite9-layout`
- **fill**: allows you to set, `fill`, `fill-opacity`
- **stroke**: allows you to set, `stroke`, `stroke-opacity`, `stroke-width`, and later `stroke-dasharray` etc.
- **size**: allows you to set minimum size, margins, padding.
- **align**: allows you to set the horizontal and vertical align for an element.
- **font**: allows you to change `font-family`, `font-weight`, `font-size`, `font-style`.
- **port**: Allows you to change `--kite9-port-side` and `--kite9-port-position`.

#### Other

* **vote**: Allows the user to vote up a connected element.

### Containment

_Implemented by **containment** class._

_Used by **selectable/replace**, **containers/insert** and **containers/contain**._

Define a token to define the type of the object.  Then use **k9-palette** to mark up an element with this type.

- **k9-contains**: Indicates the types of things that this object can contain.
- **k9-palette**:  Defines the types of thing this object is.  So far ADL defines: **link**, **end**, **cell**, **connected**, **grid**, **label** and **port**. Define others. 
- **k9-containers**: Defines the types of things this object can be contained in.

When declaring palettes, you need to tell Kite9 which types each palette contains:

```xsl
 <xsl:template name="containers-diagram-palettes">
     <adl:palette contains="connected" url="/public/templates/containers/palette.adl" />
  </xsl:template>
```
Later, we should add:

 - **k9-link**:  Says what types of link can connect to this object.


### k9-shape

Indicates the shape used by the background.  "shapes" behaviour defines some of these, but flow-chart and uml also define some.

### k9-texture

 - **background**: 
 - **foreground**: 
 - **none**: makes it transparent, but you can still click it.
 
Can be solid, transparent, or something else defined by your stylesheet.  e.g. designer specifies "gradient".

### k9-rounding

Rounding corners of rounded rectangle.

### k9-animate

 - **link**:  this indicates that it's a link, so when you drag, change the shape of the link, rather than just moving it.
