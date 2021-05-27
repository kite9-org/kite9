# Date 21st May 2016:  Sprint 7: Server-Side CSS #

- Modify XML Loader so that elements are annotated with CSS Attributes DONE
- Get the CSS loaded up on the server side by Batik.  DONE
- Extend CSS so that we add our new attributes for shapes, etc. DONE
- Remove style information from the java Stylesheet DONE

## Prelude:  Extending Batik

We are already using Batik in two places in Kite9:

 - Handling element styles and overriding
 - Converting to SVG format.
 
It makes sense to double-down and use it's capabilities for CSS parsing and styling of XML documents
generally. 

### `CSSStyleableElement`

This is used by Batik to hold XML that has been styled by a CSS.  So, we are going to need for our 
object model to implement this.  

Likewise, we need to add a `DomExtension` which declares all of the elements in our namespace, and how they
relate to each other.    This is plugged into `ExtensibleDOMImplementation`, which in turn is added to
the `SAXDocumentFactory`.

Realistically, this means that ADL becomes a proper DOM language, and that's going to mean a whole lot of
fixing up code. 

### Returning Styles 

```java
CSSContext ctx = new SVG12BridgeContext(new SomeUserAgent());
CSSEngine engine = ExtensibleDOMImplementation.createCSSEngine(d, ctx)

StyleMap sm = engine.getCascadedStyleMap(someELement, "")
Value v = sm.getValue(SVGCSSEngine.STROKE_WIDTH_INDEX)
```

### Adding CSS Properties to Batik

This is really easy:  just do:

```java
ExtensibleDOMImplementation.registerCustomCSSValueManager(new ValueManager ...

```

And then you have a new CSS property to play with.


### Adding Stylesheets to the document

Create an element that implements `CSSStyleSheetNode`.  It will be able to call:

```java
return getOwnerDocument().getCSSEngine().parseStyleSheet(xx)

### `'style'` Attribute

Implement `SVGStyleable`, with it's `getStyle` method.
```

## Step 1: A New XML Loader

In order to get to the stage where we can style things with Batik, we need to load the XML into a DOM.  So, the great 
thing is about DOM is that you can make it extensible, and use your own objects, using an `ExtensibleDOMImplementation`: 

```java
	public class ADLExtensibleDOMImplementation extends ExtensibleDOMImplementation {

	public ADLExtensibleDOMImplementation() {
		super();
		registerCustomElementFactory(XMLHelper.KITE9_NAMESPACE, "diagram", new ElementFactory() {
			
			public Element create(String prefix, Document doc) {
				Diagram out = new Diagram();
				out.setOwnerDocument(doc);
				return out;
			}
		});
		
		registerCustomElementFactory(XMLHelper.KITE9_NAMESPACE, "glyph", new ElementFactory() {
			
			public Element create(String prefix, Document doc) {
				Glyph g = new Glyph();
				g.setOwnerDocument(doc);
				return g;
			}
		});

		etc.
		
```

### Using DOM in the Object Model

So, this way, I've managed to preserve the existing object model, but use DOM behind the scenes.   This means that
generally speaking, my tests can stay relatively unharmed by all the changes.  The owner document presents a problem, however.

Rather than change all the tests to add this in, I changed `AbstractFunctionalTest` so that it creates a new owner document each time
a test is run:

```java
public class AbstractFunctionalTest extends HelpMethods {

	@Before
	public void initTestDocument() {
		AbstractDiagramElement.TESTING_DOCUMENT =  new ADLDocument();
	}

	...
```

This `TESTING_DOCUMENT` is then used by lots of the constructors that are used by the tests. e.g.:

```java

	public Glyph(String id, String stereotype, String label,  List<CompositionalDiagramElement> text, List<Symbol> symbols) {
		this(id, stereotype, label, text, symbols, false, TESTING_DOCUMENT);
	}

```

A second issue is this:  both the DOM (XML) and the object model are storing state, which is duplication.  To avoid this,
I changed the object model to *just use* the state from the dom.  Where things are attributes, this is easy:

```java

public abstract class AbstractIdentifiableDiagramElement extends AbstractDiagramElement implements IdentifiableDiagramElement, Serializable, StyledDiagramElement {


	public final String getID() {
		return getAttribute("id");
	}
	
	public void setID(String id) {
		setAttribute("id", id);
	}

```

Where they are embedded elements, it's harder.  So, I constructed some helper methods:

```java
public abstract class AbstractDiagramElement extends AbstractElement implements XMLDiagramElement, CompositionalDiagramElement {

	...

	@SuppressWarnings("unchecked")
	public <E extends Element> E getProperty(String name, Class<E> expected) {
		E found = null;
		for (int i = 0; i < getChildNodes().getLength(); i++) {
			Node n = getChildNodes().item(i);
			if ((expected.isInstance(n)) && (((Element)n).getTagName().equals(name))) {
				if (found == null) {
					found = (E) n;
				} else {
					throw new Kite9ProcessingException("Not a unique node name: "+name);
				}
			}
		}
	
		return found;
	}
	
	public <E extends Element> E replaceProperty(String propertyName, E e, Class<E> propertyClass) {
		E existing = getProperty(propertyName, propertyClass);
		if (e == null) {
			if (existing != null) {
				this.removeChild(existing);
			}
		 	return null;
		}

		if (!propertyClass.isInstance(e)) {
			throw new Kite9ProcessingException("Was expecting an element of "+propertyClass.getName()+" but it's: "+e);
		}

		if (e instanceof XMLDiagramElement) {
			((XMLDiagramElement)e).setTagName(propertyName);
			((XMLDiagramElement)e).setOwnerDocument((ADLDocument) this.ownerDocument); 
		}
		
		if (!e.getNodeName().equals(propertyName)) {
			throw new Kite9ProcessingException("Incorrect name.  Expected "+propertyName+" but was "+e.getNodeName());
		}
		
		if (existing != null) {
			this.removeChild(existing);
		}
		
		this.appendChild(e);
		
		return e;
	}
	
```

I was able to replace all of the state get* and set* methods in the other classes using these two.

For lists of items (`Symbol`s, `TextLine`s, etc.) I used a new class, `ContainerProperty`, which implemented
`Iterable`, but extended `AbstractDiagramElement`, so it was a full XML element type.  This meant that persistence
worked correctly.
 
### DOM and Links

Links are a bit more tricky to handle with the DOM.  The references (to/from) are handled with an ID, which has to refer to 
something else in the document.  

This requires a lookup, which is not ideal.  `Edge` objects continue to work with direct java object references, but `Link`s 
extend `AbstractConnection`, which uses the lookup:

```java

public abstract class AbstractConnection extends AbstractIdentifiableDiagramElement implements Connection {

	Element fromEl = getProperty("from", Element.class);
		String reference = fromEl.getAttribute("reference");
		Connected from = (Connected) ownerDocument.getChildElementById(ownerDocument, reference);
		return from;
	}

	public void setFrom(Connected v) {
		Element from = ownerDocument.createElement("from");
		from.setAttribute("reference", v.getID());
		replaceProperty("from", from, Element.class);
		from = v;
	}

	...
```

### Builders

The Java API originally had builder classes, but I removed these for now (along with their tests).  This is just extra 
code to carry round.  I will re-instantiate these as needed.  In any case, the builders were undergoing *a lot* of 
changes in a separate branch, so I don't really care for updating all this now

### Writing XML Out Again

We have two round-trip XML tests: `Test1SerializeDiagram` and `Test4StyledDiagram` (the latter doesn't *actually* seem to do much with styles,
it just has some classes attached to a few nodes.  We need to improve later in the sprint).

Because of the problems with DOM Canonicalization, I found it was necessary to use `xmlunit` to do the comparison (attribute ordering)
and so there is a class called XMLCompare in the same directory as the tests now.

### RenderingInformation

Unfortunately, because of the way the DOM works, we have to construct the object based entirely on the tag.  So, the polymorphism we used to have
around `RenderingInformation` is gone, and I have a single concrete class, `BasicRenderingInformation`.  

This contains the `displayData` stuff from [Sprint 5](sprint_005.md), as well as the elements needed to render routes. It works for now, but it's 
not pretty.

### Fixing Tests

There were lots of broken tests as a result of this massive change.   Most I was able to get working again.  A few I couldn't, but these were mainly 
due to styling anyway, so they'd just get broken when we do the next part of this change.  

### Fixing Background

When we set the size of the background, it seems like the component doesn't change shape to it.  This needs to be sorted out.  Again, I'd rather push
on and break some more things before I fix this.   

## Step 2: Using CSS Instead Of Java Stylesheets

Ok, so this is the next big change:  we want to use Batik to load the stylesheet information up for a node, from the attached stylesheets.  Broadly,
I want to have each *type of component* (e.g. glyph) having a CSS class, and have the CSS classes listed in a stylesheet.   Then, we can use the CSS stylesheet
attached to the XML document to render the right fonts etc.  

We should be able to do this in the first instance with *existing CSS classes*, and the existing Java stylesheet information (somehow).

### Writing A Stylesheet

My first attempt at this is `designer2012.css`, which has elements in it like this:

```css

.glyph {
	...
	
	stroke-width: 2px;
	stroke: black;
	
	...
}
```

In order to get glyphs to have the '.glyph' style, we have `AbstractStyledDiagramElement`:

```java

public abstract class AbstractStyledDiagramElement extends AbstractDiagramElement implements StyledDiagramElement {
	
	...
	private StyleMap sm;

	public StyleMap getComputedStyleMap(String pseudoElement) {
		return sm;
	}

	public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
		this.sm = sm;
	}

	public String getCSSClass() {
		return getAttribute("class")+" "+getTagName();
	}

	public String getShapeName() {
		return getAttribute("shape");
	}
	
	public void setShapeName(String s) {
		setAttribute("shape", s);
	}

	...
	
```

So the css Classes also have the tag name added to them.

### Loading It Up

To load up the stylesheet, we need to first add an implementation of `CSSStylesheetNode`:

```java
public class StylesheetReference extends AbstractDiagramElement implements CSSStyleSheetNode {

	...
	
    /**
     * The DOM CSS style-sheet.
     */
    protected transient StyleSheet styleSheet;
	
	/**
     * Returns the associated style-sheet.
     * TODO: Also need to handle in-line style sheets.
     */
    public StyleSheet getCSSStyleSheet() {
        if (styleSheet == null) {
            ADLDocument doc = (ADLDocument)getOwnerDocument();
            CSSEngine e = doc.getCSSEngine();
            String bu = getHref();
            ParsedURL burl = new ParsedURL(getBaseURI(), bu);
            String media = getMedia();
            styleSheet = e.parseStyleSheet(burl, media);
        }
        return styleSheet;
    }
	
	public String getHref() {
		return getAttribute("href");
	}
	
	public void setHref(String href) {
		setAttribute("href", href);
	}
	
	public String getMedia() {
		return getAttribute(SVGConstants.SVG_MEDIA_ATTRIBUTE);
	}
	
	public void setMedia(String media) {
		setAttribute(SVGConstants.SVG_MEDIA_ATTRIBUTE, media);
	}
}

```

This class *knows enough* to get the `CSSEngine`, and use it to parse the stylesheet.  To get the `CSSEngine`, we are calling the `ADLDocument` class, 
and this simply calls through to the `ADLExtensibleDOMImplementation`, which returns a Batik `SVG12CSSEngine`.   ADLDocument has a wrinkle:
 it needs to extend SVGOMDocument, as this is what the `SVG12CSSEngine` is now expecting to use.
 
```java
**
 * NOTE:  It would be better not to extend SVGOMDocument, and extend AbstractStyleableDocument,
 * but CSSUtilities does lots of casting to SVGOMDocument, and we want to use that in the
 * kite9-visualisation project.
 * 
 * @author robmoffat
 *
 */
public class ADLDocument extends SVGOMDocument {

	...

	public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return ((ADLExtensibleDOMImplementation)implementation).createElementNS(this, namespaceURI, qualifiedName);
	}

	public Element createElement(String name) throws DOMException {
		return ((ADLExtensibleDOMImplementation)implementation).createElementNS(this, XMLHelper.KITE9_NAMESPACE, name);
	}
	
	 /**
     * Returns true if the given Attr node represents an 'id'
     * for this document.
     */
    public boolean isId(Attr node) {
        if (node.getNamespaceURI() != null) return false;
        return XMLConstants.XML_ID_ATTRIBUTE.equals(node.getNodeName());
    }

    /**
     * The default view.
     */
    protected transient AbstractView defaultView;

    /**
     * The CSS engine.
     */
    protected transient CSSEngine cssEngine;


    /**
     * Sets the CSS engine.
     */
    public void setCSSEngine(CSSEngine ctx) {
        cssEngine = ctx;
    }

    /**
     * Returns the CSS engine.
     */
    public CSSEngine getCSSEngine() {
    	if (cssEngine == null) {
    		ADLExtensibleDOMImplementation impl = (ADLExtensibleDOMImplementation) getImplementation();
    		cssEngine = impl.createCSSEngine(this);
    	}
    	
        return cssEngine;
    }

    // DocumentStyle /////////////////////////////////////////////////////////

    public StyleSheetList getStyleSheets() {
        throw new RuntimeException(" !!! Not implemented");
    }

   ...

}
```

Finally, we need to give the  `Diagram` class a reference to it's stylesheet:

```java

public class Diagram extends AbstractConnectedContainer {

	...

	public StylesheetReference getStylesheetReference() {
		return getProperty("stylesheet", StylesheetReference.class);
	}
	
	public void setStylesheetReference(StylesheetReference ref) {
		replaceProperty("stylesheet", ref, StylesheetReference.class);
	}
	
}
```

In each test, we need to make sure that the stylesheet is set before rendering, otherwise everything would go wrong.

### Refactoring the Displayers

The displayers all make references to the `Stylesheet` object, which no longer exists at this level - style is all a subservient property of
the ADL diagram elements themselves.

This means you're almost *forced* down the route of refactoring the `Displayers` to work one-to-one with the diagram elements, but I am going to
resist this as long as possible, because the diagram elements themselves will be changing so much soon.

In the meantime, I deleted all of the references to the Stylesheet object, but then had to add some of this information back into a `StaticStyles` object:

```java
/**
 * Temporary class
 * @author robmoffat
 *
 */
public class StaticStyle {
	
	public static double getSymbolWidth() {
		return 20; 
		//return ss.getSymbolSize() + ss.getInterSymbolPadding();
	}

	public static Color getWatermarkColour() {
		return new Color(0f, 0f, 0f, .2f);
	}

	public static float getLinkHopSize() {
		return 12;
	}

	public static Paint getBackground() {
		return Color.WHITE;
	}

	public static Stroke getDebugLinkStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Font getDebugTextFont() {
		// TODO Auto-generated method stub
		return null;
	}

	public static double getInterSymbolPadding() {
		return 2;
	}

	public static int getKeyInternalSpacing() {
		return 15;
	}
}
```

But really, this is pretty minimal.

The displayers themselves already made heavy use of some style objects: `SVGAttributedStyle`, `BoxStyle`, `FixedShape`, `TextStyle` and `TerminatorShape`.  

So, instead of getting these classes from the stylesheet, they are now constructed, using the element from the DOM.  e.g.:

```java
public class ShapeStyle extends SVGAttributedStyle {
	
	public ShapeStyle(StyledDiagramElement stylableElement) {
		super(stylableElement);
	}

	...
	
	public Stroke getStroke() {
		Element e = getStyleElement();
		return PaintServer.convertStroke(e);
	}
		
	
	public Paint getStrokeColour() {
		return convertPaint(false);
	}

	public boolean isFilled() {
		Value v = getCSSStyleProperty(CSSConstants.CSS_FILL_PROPERTY);
		return v != null;
	}

	public Paint getBackground(Shape s) {
		return convertPaint(true);
	}
	
	...
```

So, because they have access to the element itself, they know both the `style` and the `class` of the element.  Methods like 
`getStroke()` and `getBackground()` already exist on the style objects, it was just a case of using the in-built Batik functions
to compute the styles from the element rather than the old way, which was to create a temporary element.

## Step 3: New CSS Properties

The first problem was that there are plenty of CSS properties we need in the stylesheet, which aren't in the expected list of CSS elements for SVG.

### Margins & Padding

These are supported by Batik as RectValues, so we need to add a value manager which returns those.  *Except there already is one*, which is nice.
I was also able to use much of this code again for handling padding in the same way.

### Shadows

I introduced the box-shadow css from HTML quite easily:

```css
	box-shadow-x-offset: 4px;
	box-shadow-y-offset: 4px;
	box-shadow-color: teal;
	box-shadow-opacity: .3; 
	
	/* or */
	box-shadow: 4px 4px 0px #333333; 
```

This is obviously now defined on a per-element basis, but still it works well.  

### Fills

Fill is more tricky:  The problem is that in SVG, a fill is defined with a pattern, which is actual SVG code:

- We could somehow embed the fill in the stylesheet.  This would work, ...ish.  However, it would be messy.  
- We could expect the fill to be defined somewhere else (in another file).  Although, this might not work very well on all browsers.  Somehow, it has to be 
rendered correctly back into the SVG document.
- I think somehow importing the texture into the stylesheet sounds best.   That way, we can add it to the SVG document as we use it.  
- All of this makes me think that we should just be constructing an SVG document to output...  This would be massively consistent.  
- Needs thought first.  
- Some thought later.  This is definitely the way to go.   It's going to mean changing the way displayers work *fairly* radically, but it's not the end of the world.
- Going to add a new sprint for this

### Shapes

Shapes are an interesting problem.  Originally, I wanted to define shapes with classes.  Actually, I think I still do.
But, for now, I am going to define them with names and leave them in a fixed class, `ShapeHelper`, which will serve up the shapes.

It looks something like this:

```java

public abstract class ShapeHelper {
	
	...
	
	public static java.awt.Shape createSymbolShape(SymbolShape shape, double innerSize, double x, double y) {

		...
	}

	public static Shape getTerminatorShape(String shape) {
		float ahs = getLinkEndSize();
		float half = ahs / 2;

		switch (shape) {
		case "ARROW":
		case "ARROW OPEN":
			return new Polygon2D(
					new float[] { 0, 0 - half, 0, half }, 
					new float[] { ahs, ahs, 0, ahs  }, 4);
		case "CIRCLE":
			float radius = half;
			return new Ellipse2D.Float(- radius, - radius, radius * 2, radius * 2);
		case "GAP":
		case "NONE":
			return null;
			
		case "DIAMOND":
		case "DIAMOND OPEN":
			return  new Polygon2D(
					new float[] { 0, 0 - half, 0, half }, 
					new float[] { ahs*2, ahs, 0, ahs  }, 4);
		case "BARBED ARROW":
			// taily arrow
			GeneralPath s = new GeneralPath();
			s.moveTo(0, ahs);
			s.lineTo(0, 0);
			s.lineTo(-half, ahs);
			s.moveTo(0, 0);
			s.lineTo(half, ahs);
			s.moveTo(0, 0);
			return s;
		default:
			return null;
		}
	}
	
	...
	
	
}
```

Again, this is not a perfect implementation, but a placeholder to avoid having to get involved in more work on refactoring
the displayers.

## Summary

Ok, so this sprint has taken a long time.  It's 2nd August now, which means it took 9 weeks rather than 2.  There were about 3 weeks
of holidays in that, and a new job, so perhaps this is not such a huge underestimate.  

Right now, about 80% of the tests are passing, but it's pointless to get to 100% because everything I'm going to do next is going
to break more things.  It's time to move on.




