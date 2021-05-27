# 19th April 2016: Sprint 5: D3 To Load XML

- We should be able to render the XML returned by passing it through a simple d3 component which turns it into SVG.  DONE (24th April 2016)
- Tests should look like "here's some rendering information, handle it". DONE
- Using D3 to display on the screen.   DONE (10th May 2016)
- This should be a simple drop-in replacement to Raphael, and clear out this tech debt.  DONE (17th May 2016)
- (Bonus Goal) make sure animation still looks good, so handle updates. DONE  (17th May 2016)

## Prelude: A new Style of Rendering

In the existing Kite9, I send the sizes and positions of the elements through to the client layer, and then write logic on the client to put all the elements
in their correct positions using Raphael.   Now, aside from the fact we want to remove Raphael, this means duplication:  we have code on both client and server
that knows how to position content within a Glyph.  

One thing that occurred to me in this sprint is tha  we should base rendering entirely of the content of **renderingInformation**.  This way we achieve separation in the gui of 
before and after.  This way, we could take any given tag with an ID, and call render on it.  In fact, I am not going to go 
crazy on rendering client-side for now because of this.  I'm just going to get the basics in place and worry about fine-grained
rendering later, by changing the model. 

So, in this world, `RenderingInformation` would contain an SVG "payload".  We would construct a group for the id'd element, and then 
dump the SVG payload into the group.  Things like offset / size and so on could also be set here.  

In order to do this, I am going to rely on Batik to convert my diagrams into SVG, and handle the outputting.  

## Second Prelude:  Content Types

Outputting SVG (or a web page containing SVG) is a *Content-Type* issue, and this is supported by the HTTP spec.  We should make
use of this.  The content types related to diagrams in Kite9 are:

 - **text/adl+xml** : Unrendered ADL (Kite9's internal XML Format).  
 - **text/rendered-adl+xml**:  Same as the above, but including `RenderingInformation` tags to say where each element goes.
 - **text/adl-svg+xml**: As above, but also including snippets of SVG that say exactly what the element looks like (also embedded within `RenderingInformation`
 - **application/pdf**:  PDF type
 - **image/png**: PNG Image format (the usual web format)
 - **image/svg+xml**:  SVG Images
 - **text/html**: Our rendered page, with an SVG image embedded in it (processed using D3/React/etc)
 
To handle these in Spring, we use a [HttpMessageConverter](http://docs.spring.io/spring/docs/3.0.x/javadoc-api/org/springframework/http/converter/HttpMessageConverter.html)
subtype.  

By default, browsers handle a variety of content types themselves.  So, PNG, SVG and HTML would be handled automatically.  Then it would be up to the browser to ask 
for the right content type beyond this.

(ADL = Abstract Diagramming Language, a format for (theoretically) holding any type of diagram)

## Third Prelude: JavaScript

One of the reasons I'm doing this part of the project *right now* is that I want to get the JavaScript libraries sorted out properly.   I am going to use
`maven-frontend-plugin`, which combines several technologies:  Node.js, NPM, Webpack, and Karma.   I'm using these all at work as it is, and I'm going to now port them 
into this project to use.

This will actually be the main bulk of the project, I think.  

## Step 1: Rendering XML

For this we need two classes to encapsulate the XML, and therefore the content types.  To do all of that, I need an `ADLMessageConverter`,
and a class to hold the XML:

```java
public interface ADL {

	MediaType getMediaType();
	
	Diagram getAsDiagram();
	
	String getAsXMLString();

}
```

Implementation for this below.  You can initialize with either the xml or the `Diagram` object, and both getters will work:

```java
public class ADLImpl implements ADL {
		
	private Diagram diagram;
	private String xml;
	private final MediaType mt;
	
	public ADLImpl(String content, MediaType mt) {
		this.xml = content;
		this.mt = mt;
	}
	public ADLImpl(Diagram content, MediaType mt) {
		this.diagram = content;
		this.mt = mt;
	}

	@Override
	public Diagram getAsDiagram() {
		if (diagram == null) {
			diagram = (Diagram) new XMLHelper().fromXML(xml);
		}
		
		return diagram;
	}

	@Override
	public MediaType getMediaType() {
		return mt;
	}
	@Override
	public String getAsXMLString() {
		if (xml == null) {
			xml = new XMLHelper().toXML(diagram);
		}
		
		return xml;
	}	
}
```

### Media Types

And here are the `MediaType`s we are going to convert from/to:

```java
public class MediaTypes {

	public static final String SVG_VALUE = "image/svg+xml";
	public static final String PDF_VALUE = "application/pdf";
	public static final String ADL_XML_VALUE = "text/adl+xml";
	public static final String ARRANGED_ADL_XML_VALUE = "text/arranged-adl+xml";
	public static final String ADL_SVG_VALUE = "text/adl-svg+xml";
	public static final String CLIENT_SIDE_IMAGE_MAP_VALUE = "text/html-image-map";
	
}
```

The rest are available in the `MediaType` class already (e.g. PNG).

### The Converter

Ok, so now we need to implement the `ADLMessageConverter`:

```java
@Component
public class ADLMessageConverter extends AbstractHttpMessageConverter<ADL>{
	/**
	 * This is the list of media types we can support writing.
	 */
	public ADLMessageConverter() {
		super(MediaTypes.ADL_XML, MediaTypes.RENDERED_ADL_XML, MediaType.IMAGE_PNG, MediaTypes.SVG, MediaTypes.PDF, MediaType.TEXT_HTML);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return ADL.class.isAssignableFrom(clazz);
	}
	
	/**
	 * List of things we can read in is much more limited than things we can write back out - just the XML formats, basically.
	 */
	@Override
	protected boolean canRead(MediaType mediaType) {
		return MediaTypes.ADL_XML.includes(mediaType) || MediaTypes.RENDERED_ADL_XML.includes(mediaType);
	}
```

So *reading* the POST body is much more limited, (to an XML format), and looks like this:

```java
	@Override
	protected ADL readInternal(Class<? extends ADL> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		MediaType mt = inputMessage.getHeaders().getContentType();
		Charset charset = mt.getCharSet();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		StreamHelp.streamCopy(inputMessage.getBody(), baos, true);
		String s = baos.toString(charset.name());
		return new ADLImpl(s, mt);
	}
```

Writing is more complex.  If the client asks for unrendered XML, we simply return it.  Otherwise, we need to 
render any unrendered XML, and then select the correct `Format` object, and output it.  `Format` is a bit
of already-built Kite9 code that can render diagrams into the different formats.  

```java
	@Override
	protected void writeInternal(ADL t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		MediaType contentType = outputMessage.getHeaders().getContentType();
		Charset charset = contentType.getCharSet() == null ? Charset.forName("UTF-8") : contentType.getCharSet();
		String stylesheet = StylesheetProvider.DEFAULT;  (1)
		
		if (MediaTypes.ADL_XML.isCompatibleWith(contentType)) {
			outputMessage.getBody().write(t.getAsXMLString().getBytes(charset)); (2)
			return;
		}
			
		try {
			Diagram d = t.getAsDiagram();
			if (!t.isArranged()) {
				// unrendered, so render.
				d = arranger.arrangeDiagram(d, stylesheet); (3)
			}

			Format f = formatSupplier.getFormatFor(contentType); (4)
			Stylesheet ss = StylesheetProvider.getStylesheet(stylesheet); 
			
			f.handleWrite(d, outputMessage.getBody(), ss, true, null, null); (5)
		} catch (Exception e) {
			throw new HttpMessageNotReadableException("Caused by: "+e.getMessage(), e);
		}
		
	}
```

1.  Stylesheet is currently set to default.  Eventually it will be part of the Diagram object/xml.
2.  This returns unrendered XML, if that is what is asked for.
3.  Arranges the diagram.  This adds `RenderingInformation` objects to each element, telling them where they need to be placed.
4.  We choose the `Format` instance based on the requested Content-Type.
5.  Writes to the stream using the chosen `Format` instance.

### The Controller

Is ridiculously simple:  we are receiving POSTed XML in the HTTP Request, and then outputting it again in another format:

```java
@Controller
public class RenderingController {

	@RequestMapping(path="/api/renderer")
	public @ResponseBody ADL echo(@RequestBody ADL input, @RequestHeader HttpHeaders headers) {
		return input;
	}
}
```

Obviously, this will change in the future, and we will do caching, etc.  I also added a method for returning a test card 
`api/renderer/test`, which is just a handy shortcut to having to post content, and a method which randomly modifies this 
test card by changing the links and the text (`api/renderer/random`), which allows me to see how the diagram will animate
between states.

### Testing

I just wrote a simple test which creates a `Diagram` object, and then POSTs it.  The response is examined to check that
actually it's in the right format, and is passably likely to contain what I asked for. For example:

```java
public class RestRenderingIT extends AbstractAuthenticatedIT {
	
	private static final int EXPECTED_HEIGHT = 204;
	public static final int EXPECTED_WIDTH = 264;

	protected byte[] withBytesInFormat(MediaType output) throws URISyntaxException {
		String xml = createDiagramXML();
		HttpHeaders headers = createKite9AuthHeaders(u.getApi(), MediaTypes.ADL_XML, output);
		RequestEntity<String> data = new RequestEntity<String>(xml, headers, HttpMethod.POST, new URI(urlBase+"/api/renderer"));
		byte[] back = getRestTemplate().exchange(data);
		return back;
	}

	@Test
	public void testPNGRender() throws URISyntaxException, IOException {
		byte[] back = withBytesInFormat(MediaType.IMAGE_PNG);
		BufferedImage bi = ImageIO.read(new ByteArrayInputStream(back));
		Assert.assertEquals(EXPECTED_WIDTH, bi.getWidth());
		Assert.assertEquals(EXPECTED_HEIGHT, bi.getHeight());
	}
	
```

## Step 2:  A Web Page

Now that we have plumbed in the original Kite9 Visualization, and got it working, we need to produce a page
containing our diagram, rendered as SVG.  

To do this, I am going to use a React.js component, which will draw on the screen, and then render the D3 contained
within it.


http://localhost:8080/api/renderer/test

### Setting Up Maven-Frontend-Plugin

Ok, so broadly I am going to use the [Maven Frontend Plugin](https://github.com/eirslett/frontend-maven-plugin) to handle javascript packages and testing.   It includes `node`, `karma` and `webpack` integration.

```xml
<plugin>
		        <groupId>com.github.eirslett</groupId>
		        <artifactId>frontend-maven-plugin</artifactId>
		        <version>1.0</version>
	            <configuration>
	            	<installDirectory>target/frontend</installDirectory>
			        <workingDirectory>src/main/frontend</workingDirectory>
			    </configuration>
```
This says:

 - Download `node` into the `target/frontend` directory.  
 - Get the list of modules to download from `src/main/frontend/package.json`.

There are now *lots* of packages downloaded, but broadly they fall into a few categories:

 - Things to make testing work (in the `devDependencies` section).
 - Things to make `webpack` work (listed under the webpack dependency)
 - Things to make `react` work (stuff at the top of the dependency list).
 
### Webpack

[Webpack](http://webpack.github.io) is a brain-bending creation that allows you to package up javascript (and css, fonts, etc) into
*bundles*, which can then be served as a single HTTP request.  

ES6 now includes the `import` directive, so, Webpack looks for these and resolves them to build large javascript modules.  The entry-point
of our application will be `app.jsx`, and this will import various other resources like this:

```
import React from 'react';
import { render } from 'react-dom'
import ADLSpace from './adl/components/ADLSpace.jsx'
```

Webpack has a config file `webpack.config.js`, which contains details about the *inputs* and *outputs*, and the transforms that need to apply
in the middle:

```js
module.exports = {
	...
    
    entry: {
    	bundle:  './app.jsx'
    },
    output: {
        path: __dirname + "/../../../target/classes/static/dist",
        filename: "bundle.js"
    },

    module: {
    	loaders: [
    	   ...
    	   { test: /\.less$/, loader: "style!css!less" },
    	   { test: /\.css$/, loader: "style!css" },
    	   { test: /\.(png|woff|woff2|eot|ttf|svg)$/, loader: "url-loader?limit=1000000" } 
    	]    	
    }

}
```

So the loaders are chained together.  The less loader for example chains, `less-loader`, `css-loader` and `style-loader` in that order, 
and bundles the output into the same bundle.js file as everything else goes in.

Note that webpack is building into the `target/classes` directory, which allows us to have a refreshing loader when we run:

```sh
mvn frontend:npm 
```

### Setting up React

`.jsx` is an extension for React files which allows them to include snippets of HTML which will be unpacked into React template code.

So our app.jsx calls this:

```js
render(
		<App />,
		document.getElementById('react')
)
```

Which renders the <App /> react module into the `react` element on the page.   At the moment, <App /> just renders to `Hello World`.

### HTMLFormat

Finally, we are ready to construct the `HTMLFormat` code, which will output our `ADLImpl` as a webpage, which effectively means just outputting this:

```html
<!DOCTYPE html>
	<html>
	<head lang="en">
	    <meta charset="UTF-8"/>
	    <title>Kite9</title>
	</head>
	<body>
		{content}
	
	    <div id="react"></div>

	    <script src="/dist/bundle.js"></script>

	</body>
</html>
```

The built webpack code, containing the `<App />` react template is loaded in from the `/dist/bundle.js` file, and rendered on the screen,
inside the `react` element.

This can be easily run in a browser, and is the default format, since browsers ask for the `text/html` content-type when they request the page.  
Again, I am skipping the choice of view/templating code on the server side as it's just not needed yet.

### ADLSpace React Component

Instead of just rendering 'Hello World' on the screen, we really want the `App` component to render an `ADLSpace` component, which 
will lay out the diagram using D3.  

So, to start with, I want a react component that actually renders some SVG on the screen.  I'm going to import the old Kite9
code for now, and get this displaying to give a base from which to improve things.

This is the entire component for rendering some SVG, excluding the actual `update` logic:

```js
import React from 'react'
import ReactDOM from 'react-dom'
import jQuery from 'jquery'
import d3 from 'd3'

var round = 1;

export default class ADLSpace extends React.Component {
	
	render() {
		return (<svg id={this.props.id} width={this.props.width} height={this.props.height} xmlns="http://www.w3.org/2000/svg" />)
	}
	
	componentDidMount() {
		this.update(this.props.content);
	}		
	
	componentDidUpdate() {
		this.update(this.props.content);
	}
		
	update(xml) {	
		...
	}
}
	
```

Whenever react renders the ADLSpace component, it will call either `componentDidMount` (on the first render) or 
`componentDidUpdate` (on subsequent ones).   

In the initial version of the `update` method, I simply called off to the old Kite9 render methods and used Raphael.

It mainly seems to work:

![First laid out page](images/005_1.png)

 - Fonts are wrong (webpack wants to add them to the bundle)
 - Background colours are also not present for some reason.
 
I think I can live with this:  it's a reasonable starting point to begin with removing the Raphael dependency.

## Step 3: Removing Raphael, Tidying Up

So there's still a bit of a mess here:

1.  We've got three large javascript files which control all the loading/updating rendering. 
2.  Two of them are referencing Raphael.
3.  They're all doing lots of XML manipulation, which in the future I think will just *go*.

I need to decide whether to pause this, and do the `renderingInformation` change, or try and pull out the Raphael.  Obviously, doing the R.I. change will *massively*
reduce the amount of XML we are importing, so maybe this is a good idea.

Also, we need to do the grouping change.  But, I definitely want to do that after renderingInformation.  So...
 
### Spike Solution: Rendering SVG Within Kite9 Visualisation

Some of this turned out to be easy:   I used Batik's `SVGGraphics2D` class to create a new SVGRenderer class in Kite9.  What doesn't work:

 - By default, Kite9 renders all the fonts into shapes before adding them to the graphics, so you end up with a much larger
SVG file than you expect (all the paths to describe each character).  
 - Any kind of fill, including background fills
 - Shadows
 - Literally *everything* is encoded on a per-element basis: stylesheets are completely out-of-the-question.
 
 But on the plus side, it's pretty exact.  By not using text, I wonder if this improves speed?  Hard to say.

So, I could just spend the rest of this sprint sorting this out, and that would be great.  Is this worth doing? I think, yes:
if we can plug this into the rendering information, it's going to simplify things massively, and that's a huge win, 
and it should knock out the Raphael problem at the same time.  (Animation is likely to be made harder though I think).

### Fixing Background Fills + Shadows

 - Batik comes with it's own `LinearGradientPaint`.  Maybe I should use this instead of the AWT one?
 
Trying [this handler](https://gist.github.com/msteiger/4509119) from the internet.  This works really well and solves the issue of Gradient fills not being
supported.  I had to make a couple of changes:

1.  `LinearGradientPaint` is the object used in Java to represent the gradient fill.  However, I wrap one of these so that I can use the same paint for any size of
glyph.  This means it's not a `LinearGradientPaint` when it gets the handler.  So, I unwrap it first.
2.  As a result of (1), I have to modify the handler class to use percentages for gradient start/end points, which is pretty simple.
3.  Naively, each element with a gradient paint gets their gradient converted separately, e.g.

```xml
<linearGradient xmlns="http://www.w3.org/2000/svg" x1="50.0%" x2="50.0%" y1="0.0%" style="color-interpolation:sRGB;" y2="100.0%" id="gradient1" spreadMethod="pad"><stop style="stop-color:rgb(242,242,242);" offset="0%"/><stop style="stop-color:rgb(204,204,204);" offset="100%"/></linearGradient>
<linearGradient xmlns="http://www.w3.org/2000/svg" x1="50.0%" x2="50.0%" y1="0.0%" style="color-interpolation:sRGB;" y2="100.0%" id="gradient1" spreadMethod="pad"><stop style="stop-color:rgb(242,242,242);" offset="0%"/><stop style="stop-color:rgb(204,204,204);" offset="100%"/></linearGradient>
<linearGradient xmlns="http://www.w3.org/2000/svg" x1="50.0%" x2="50.0%" y1="0.0%" style="color-interpolation:sRGB;" y2="100.0%" id="gradient1" spreadMethod="pad"><stop style="stop-color:rgb(242,242,242);" offset="0%"/><stop style="stop-color:rgb(204,204,204);" offset="100%"/></linearGradient>
...
```

This is pointless duplication, so I introduced a cache.  But there is something called `GradientPaintValueManager` which I wrote to convert between Raphael's idea 
of gradients, and the one in Batik/Java.  So, this will need to be removed at some point soon.

What's the right way to do this?  I guess, if we are using CSS generally, then we will need some CSS for fill gradients, and Raphael's approach is both working and OK.
So, maybe we're barking up the wrong tree by removing this.  For now, I've added the fill property as the key for the cache, so that we can cache the gradients.  This fixes the problem, but 
on the whole this bit is messy now.

### Fixing the TestCard

 - Stuff seems to be offset incorrectly is all. This turned out to be because we had zero-size connection 
 bodies in the diagram, which, if you try to draw them cause the masks to go out of whack.  Easily fixed.
 
Now we have a bunch of visualisation code to check in, including new tests:  it's time to bite the bullet, get a github private repo and check this stuff in there.
Also, I need to check in the SVG, so that we can use it as a baseline when running the tests.


### Adding the SVG To The Rendering Information

Ok, this is a tricky bit. Ideally, the rendering information will be contained within the existing XML.  But, we have different *layers* to take care of: the shadow, the foreground 
and the 'flannel' (the layer with the interactivity).  

For a typical Glyph, the SVG looks like this (comments added by me):

```xml
		<!-- SHADOW --> 
		<g
			style="fill:rgb(179,179,179); text-rendering:optimizeLegibility; color-interpolation:linearRGB; color-rendering:optimizeQuality; stroke:rgb(179,179,179); image-rendering:optimizeQuality;">
			<rect x="14.5" y="14.5" width="95" style="stroke:none;" rx="6"
				ry="6" height="47" />
		</g> 

		
		<g style="fill:url(#gradient1); text-rendering:optimizeLegibility; color-interpolation:linearRGB; color-rendering:optimizeQuality; stroke:url(#gradient1); image-rendering:optimizeQuality;">
			

			<!-- BACKGROUND -->
			<rect x="12.5" y="12.5" width="95" style="stroke:none;" rx="6" ry="6" height="47" />

			<!-- BORDER -->
			<rect x="12.5" y="12.5" width="95" style="fill:none; stroke:black;" rx="6" ry="6" height="47" /> 
			<!-- STEREO -->
			<path
				d="M41.5469 29.0938 L41.5469 27.4062 Q42.4219 27.7969 43.0234 27.9531 Q43.625 28.1094 44.125 28.1094 Q44.7188 28.1094 45.0391 27.8828 Q45.3594 27.6562 45.3594 27.2031 Q45.3594 26.9531 45.2188 26.7578 Q45.0781 26.5625 44.8047 26.3828 Q44.5312 26.2031 43.6875 25.7969 Q42.9062 25.4375 42.5156 25.0938 Q42.125 24.75 41.8906 24.2969 Q41.6562 23.8438 41.6562 23.25 Q41.6562 22.1094 42.4219 21.4609 Q43.1875 20.8125 44.5469 20.8125 Q45.2188 20.8125 45.8281 20.9688 Q46.4375 21.125 47.0938 21.4062 L46.5156 22.8281 Q45.8281 22.5469 45.375 22.4375 Q44.9219 22.3281 44.5 22.3281 Q43.9844 22.3281 43.7031 22.5625 Q43.4219 22.7969 43.4219 23.1875 Q43.4219 23.4219 43.5391 23.6016 Q43.6562 23.7812 43.8984 23.9531 Q44.1406 24.125 45.0469 24.5469 Q46.25 25.125 46.6953 25.7031 Q47.1406 26.2812 47.1406 27.125 Q47.1406 28.2812 46.3047 28.9531 Q45.4688 29.625 43.9844 29.625 Q42.6094 29.625 41.5469 29.0938 ZM47.8906 24.2969 L47.8906 23.5312 L48.875 22.9375 L49.3906 21.5469 L50.5312 21.5469 L50.5312 22.9531 L52.3594 22.9531 L52.3594 24.2969 L50.5312 24.2969 L50.5312 27.4531 Q50.5312 27.8281 50.7422 28.0078 Q50.9531 28.1875 51.3125 28.1875 Q51.7812 28.1875 52.4375 27.9844 L52.4375 29.3125 Q51.7656 29.625 50.7969 29.625 Q49.7188 29.625 49.2266 29.0781 Q48.7344 28.5312 48.7344 27.4531 L48.7344 24.2969 L47.8906 24.2969 ZM53.3652 26.2656 Q53.3652 24.625 54.1855 23.7266 Q55.0059 22.8281 56.459 22.8281 Q57.8496 22.8281 58.623 23.6172 Q59.3965 24.4062 59.3965 25.7969 L59.3965 26.6719 L55.1621 26.6719 Q55.1934 27.4375 55.6152 27.8594 Q56.0371 28.2812 56.8027 28.2812 Q57.3965 28.2812 57.9199 28.1641 Q58.4434 28.0469 59.0215 27.7656 L59.0215 29.1562 Q58.5527 29.3906 58.0215 29.5078 Q57.4902 29.625 56.7246 29.625 Q55.1465 29.625 54.2559 28.75 Q53.3652 27.875 53.3652 26.2656 ZM55.209 25.4844 L57.7246 25.4844 Q57.709 24.8125 57.373 24.4531 Q57.0371 24.0938 56.4746 24.0938 Q55.8965 24.0938 55.5762 24.4531 Q55.2559 24.8125 55.209 25.4844 ZM60.8457 29.5 L60.8457 22.9531 L62.2051 22.9531 L62.4707 24.0469 L62.5488 24.0469 Q62.8613 23.5 63.377 23.1641 Q63.8926 22.8281 64.502 22.8281 Q64.8613 22.8281 65.1113 22.875 L64.9707 24.5469 Q64.752 24.5 64.4395 24.5 Q63.5957 24.5 63.1113 24.9375 Q62.627 25.375 62.627 26.1719 L62.627 29.5 L60.8457 29.5 ZM65.9043 26.2656 Q65.9043 24.625 66.7246 23.7266 Q67.5449 22.8281 68.998 22.8281 Q70.3887 22.8281 71.1621 23.6172 Q71.9355 24.4062 71.9355 25.7969 L71.9355 26.6719 L67.7012 26.6719 Q67.7324 27.4375 68.1543 27.8594 Q68.5762 28.2812 69.3418 28.2812 Q69.9355 28.2812 70.459 28.1641 Q70.9824 28.0469 71.5605 27.7656 L71.5605 29.1562 Q71.0918 29.3906 70.5605 29.5078 Q70.0293 29.625 69.2637 29.625 Q67.6855 29.625 66.7949 28.75 Q65.9043 27.875 65.9043 26.2656 ZM67.748 25.4844 L70.2637 25.4844 Q70.248 24.8125 69.9121 24.4531 Q69.5762 24.0938 69.0137 24.0938 Q68.4355 24.0938 68.1152 24.4531 Q67.7949 24.8125 67.748 25.4844 ZM72.9941 26.2188 Q72.9941 24.6094 73.8301 23.7188 Q74.666 22.8281 76.1816 22.8281 Q77.1191 22.8281 77.8457 23.2344 Q78.5723 23.6406 78.9551 24.4141 Q79.3379 25.1875 79.3379 26.2188 Q79.3379 27.8125 78.4941 28.7188 Q77.6504 29.625 76.1504 29.625 Q75.1973 29.625 74.4785 29.2109 Q73.7598 28.7969 73.377 28.0234 Q72.9941 27.25 72.9941 26.2188 ZM74.8066 26.2188 Q74.8066 27.1875 75.127 27.6875 Q75.4473 28.1875 76.166 28.1875 Q76.8848 28.1875 77.1973 27.6875 Q77.5098 27.1875 77.5098 26.2188 Q77.5098 25.2344 77.1973 24.75 Q76.8848 24.2656 76.1504 24.2656 Q75.4473 24.2656 75.127 24.75 Q74.8066 25.2344 74.8066 26.2188 Z"
				style="fill:rgb(112,112,112); stroke:none;" /> 
		</g>

		<!-- LABEL --> 
		<g
			style="text-rendering:optimizeLegibility; font-size:15; font-family:&apos;Open Sans&apos;; color-interpolation:linearRGB; color-rendering:optimizeQuality; image-rendering:optimizeQuality;">
			<path style="stroke:none;"
				d="M21.5156 49.5 L21.5156 38.7969 L24.0625 38.7969 Q26.0625 38.7969 27.0156 39.5312 Q27.9688 40.2656 27.9688 41.75 Q27.9688 42.8125 27.3984 43.5547 Q26.8281 44.2969 25.6719 44.625 L28.5781 49.5 L27.6875 49.5 L24.9219 44.8281 L22.2656 44.8281 L22.2656 49.5 L21.5156 49.5 ZM22.2656 44.1875 L24.3125 44.1875 Q25.6562 44.1875 26.4062 43.5859 Q27.1562 42.9844 27.1562 41.7969 Q27.1562 40.5781 26.4219 40.0234 Q25.6875 39.4688 24.0312 39.4688 L22.2656 39.4688 L22.2656 44.1875 ZM29.7886 45.5156 Q29.7886 43.5625 30.7339 42.4766 Q31.6792 41.3906 33.3198 41.3906 Q34.9604 41.3906 35.8979 42.4922 Q36.8354 43.5938 36.8354 45.5156 Q36.8354 47.4531 35.8901 48.5469 Q34.9448 49.6406 33.2729 49.6406 Q32.2261 49.6406 31.4292 49.1406 Q30.6323 48.6406 30.2104 47.6953 Q29.7886 46.75 29.7886 45.5156 ZM30.5542 45.5156 Q30.5542 47.1562 31.2729 48.0703 Q31.9917 48.9844 33.312 48.9844 Q34.6323 48.9844 35.3511 48.0703 Q36.0698 47.1562 36.0698 45.5156 Q36.0698 43.8594 35.3433 42.9531 Q34.6167 42.0469 33.2964 42.0469 Q31.9761 42.0469 31.2651 42.9531 Q30.5542 43.8594 30.5542 45.5156 ZM39.0308 49.5 L39.0308 38.1094 L39.7651 38.1094 L39.7651 40.9688 Q39.7651 41.6094 39.7339 42.1562 L39.7026 42.7812 L39.7651 42.7812 Q40.2183 42.0625 40.8589 41.7266 Q41.4995 41.3906 42.3901 41.3906 Q44.062 41.3906 44.9136 42.4375 Q45.7651 43.4844 45.7651 45.5156 Q45.7651 47.5 44.8745 48.5703 Q43.9839 49.6406 42.3745 49.6406 Q41.5151 49.6406 40.8354 49.2891 Q40.1558 48.9375 39.7651 48.2969 L39.7026 48.2969 L39.4839 49.5 L39.0308 49.5 ZM39.7651 45.625 Q39.7651 47.4375 40.3979 48.2188 Q41.0308 49 42.3745 49 Q43.6714 49 44.3276 48.0859 Q44.9839 47.1719 44.9839 45.5 Q44.9839 42.0469 42.3901 42.0469 Q40.9839 42.0469 40.3745 42.8516 Q39.7651 43.6562 39.7651 45.5156 L39.7651 45.625 ZM47.5996 38.7969 L48.5371 38.7969 L48.2871 42.6562 L47.8496 42.6562 L47.5996 38.7969 ZM50.1187 49.1562 L50.1187 48.375 Q51.3218 48.9688 52.6655 48.9688 Q53.8374 48.9688 54.4468 48.5781 Q55.0562 48.1875 55.0562 47.5312 Q55.0562 46.9375 54.5718 46.5234 Q54.0874 46.1094 52.978 45.7188 Q51.7749 45.2812 51.2905 44.9766 Q50.8062 44.6719 50.564 44.2734 Q50.3218 43.875 50.3218 43.3125 Q50.3218 42.4219 51.0718 41.9062 Q51.8218 41.3906 53.1655 41.3906 Q54.4624 41.3906 55.6187 41.875 L55.353 42.5312 Q54.1812 42.0469 53.1655 42.0469 Q52.1968 42.0469 51.6265 42.3672 Q51.0562 42.6875 51.0562 43.2656 Q51.0562 43.875 51.5015 44.25 Q51.9468 44.625 53.228 45.0938 Q54.3062 45.4844 54.7983 45.7891 Q55.2905 46.0938 55.5327 46.4922 Q55.7749 46.8906 55.7749 47.4219 Q55.7749 48.5 54.9624 49.0703 Q54.1499 49.6406 52.6655 49.6406 Q51.0562 49.6406 50.1187 49.1562 ZM61.3306 44.1562 Q61.3306 42.5312 62.0024 41.2656 Q62.6743 40 63.9243 39.3125 Q65.1743 38.625 66.7993 38.625 Q68.5493 38.625 69.9399 39.2656 L69.6431 39.9375 Q68.2524 39.2969 66.7524 39.2969 Q64.6274 39.2969 63.3931 40.6016 Q62.1587 41.9062 62.1587 44.125 Q62.1587 46.5469 63.3306 47.7656 Q64.5024 48.9844 66.7993 48.9844 Q68.2681 48.9844 69.2993 48.5625 L69.2993 44.8125 L66.1274 44.8125 L66.1274 44.1094 L70.0649 44.1094 L70.0649 48.9844 Q68.5181 49.6406 66.5806 49.6406 Q64.0337 49.6406 62.6821 48.2109 Q61.3306 46.7812 61.3306 44.1562 ZM72.5684 49.5 L72.5684 38.1094 L73.3027 38.1094 L73.3027 49.5 L72.5684 49.5 ZM74.6313 41.5312 L75.3813 41.5312 L77.0688 46 Q77.8345 48.0625 78.0376 48.7812 L78.0845 48.7812 Q78.3813 47.8438 79.0845 45.9688 L80.772 41.5312 L81.522 41.5312 L77.9438 50.7969 Q77.522 51.9219 77.2251 52.3125 Q76.9282 52.7031 76.5376 52.9062 Q76.147 53.1094 75.5845 53.1094 Q75.1782 53.1094 74.6626 52.9531 L74.6626 52.3125 Q75.0845 52.4375 75.5688 52.4375 Q75.9438 52.4375 76.2329 52.2578 Q76.522 52.0781 76.7485 51.7109 Q76.9751 51.3438 77.2876 50.5469 L77.6782 49.5 L74.6313 41.5312 ZM82.8442 53.1094 L82.8442 41.5312 L83.4692 41.5312 L83.6099 42.6719 L83.6411 42.6719 Q84.4692 41.3906 86.2661 41.3906 Q87.8755 41.3906 88.7271 42.4453 Q89.5786 43.5 89.5786 45.5156 Q89.5786 47.4844 88.688 48.5625 Q87.7974 49.6406 86.2505 49.6406 Q84.4224 49.6406 83.5786 48.2656 L83.5161 48.2656 L83.5474 48.8906 Q83.5786 49.4219 83.5786 50.0781 L83.5786 53.1094 L82.8442 53.1094 ZM83.5786 45.5312 Q83.5786 47.4062 84.2036 48.2031 Q84.8286 49 86.2349 49 Q87.4692 49 88.1333 48.0938 Q88.7974 47.1875 88.7974 45.5469 Q88.7974 42.0469 86.2661 42.0469 Q84.8755 42.0469 84.2271 42.8125 Q83.5786 43.5781 83.5786 45.2969 L83.5786 45.5312 ZM91.7725 49.5 L91.7725 38.1094 L92.5068 38.1094 L92.5068 41.7344 L92.46 42.75 L92.5225 42.75 Q92.96 42.0312 93.6396 41.7109 Q94.3193 41.3906 95.335 41.3906 Q98.0537 41.3906 98.0537 44.2969 L98.0537 49.5 L97.335 49.5 L97.335 44.3438 Q97.335 43.1406 96.8271 42.5938 Q96.3193 42.0469 95.2568 42.0469 Q93.835 42.0469 93.1709 42.7656 Q92.5068 43.4844 92.5068 45.1094 L92.5068 49.5 L91.7725 49.5 Z" />
		</g> 
```

 - It's interesting that the label is within a separate group to the stereo and the rest of it:  I wonder why this is?

If it weren't, this would make life a bit simpler I think.  My plan is to add some methods to the Graphics2D object so that we can tell it where to put each bit of XML as it goes along.

### Rendering Per Element

For each on-screen element, there can be several layers.  Stuff is added to the graphics context from the bottom layer to the top.  We have the notion of this already
in `GraphicsSourceRenderer`, but I've now made this specific:

```java
public interface GraphicsSourceRenderer<X> extends Renderer<X> {

	/**
	 * Returns a graphics context that the displayer can use.
	 */
	public GraphicsLayer getGraphicsLayer(GraphicsLayerName layer, float transparency, Dimension2D size);
	
	public void setDisplayer(RequiresGraphicsSourceRendererCompleteDisplayer cd);
	
	/**
	 * Returns the size of the image for a given diagram size.
	 */
	public Dimension2D getImageSize(Dimension2D diagramSize);
}
```

So you pass in a `GraphicsLayerName` and get a `GraphicsLayer` object back. 

```java
public enum GraphicsLayerName {

	BACKGROUND, SHADOW, MAIN, FLANNEL, WATERMARK, COPYRIGHT, DEBUG
}
```

`GraphicsLayer` is an interface which matches the used methods of `Graphics2D`, but contains two extra ones:

```java
public interface GraphicsLayer {
	
	/**
	 * Use this to indicate we have started processing a diagram element.
	 */
	public void startElement(DiagramElement de);
	
	/**
	 * Use this to indicate end of processing.
	 */
	public void endElement();
	
	...
	
	// draw(Shape), fill(Shape) etc.
}
```

This means we can use a `BasicGraphicsLayer` (which is a simple adapter for an actual `Graphics2D` object, or we can
use a new `SVGGraphicsLayer`, which manipulates the DOM tree to add in the extra groups we need:

```java

public class SVGGraphicsLayer extends BasicGraphicsLayer {
	
	...
	
	@Override
	public void startElement(DiagramElement de) {
		Element group = document.createElement("g");
		group.setAttribute("layer", name.name());
		if (de instanceof IdentifiableDiagramElement) {
			group.setAttribute("element-id", ((IdentifiableDiagramElement) de).getID());
		}
		
		((SVGGraphics2D)g2).setTopLevelGroup(group);
		super.startElement(de);
	}

	@Override
	public void endElement(DiagramElement de) {
		Element topGroup = getTopLevelGroup();
		if (worthKeeping(topGroup)) {
			originalTopGroup.appendChild(topGroup);
		}
		
		super.endElement(de);
		((SVGGraphics2D)g2).setTopLevelGroup(topGroup);
	}

```

Finally, we have an `SVGRenderer`, which implements `GraphicsSourceRenderer` and passes back the `SVGGraphicsLayer` object to the ADL Renderers to use.

Now, the XML coming out of the `SVGRenderer` looks like this:

```xml
<svg ...
  <g><defs id="defs1"><linearGradient x1="50.0%" x2="50.0%" y1="0.0%" style="color-interpolation:sRGB;" y2="100.0%" id="gradient1" spreadMethod="pad"><stop style="stop-color:rgb(242,242,242);" offset="0%"/><stop style="stop-color:rgb(204,204,204);" offset="100%"/></linearGradient></defs>

    ...
	<g element-id="RG" layer="SHADOW">
	   <g style="fill:rgb(179,179,179);... /></g>
	</g>
```

So, we have correctly separated out the different *layers* and *diagram elements* in our svg output.  

Then, when it comes to streaming the XML, we can redirect the different parts into the different `<renderingInformation>` objects:


```java
public abstract class AbstractRenderingInformation implements RenderingInformation {
	
	...
	@XStreamAsAttribute
	protected boolean rendered = true;
	protected Object displayData;

	public Object getDisplayData() {
		return displayData;
	}

	public void setDisplayData(Object displayData) {
		this.displayData = displayData;
	}
	
```

And `displayData` would simply be an object containing some XML, so the parts from the previous block of XML would just be held in different map elements.

```java
/**
 * Contains rendering information for drawing components in SVG format.
 * 
 * @author robmoffat
 */
public class XMLFragments {

	@XStreamImplicit
	private final List<Element> parts = new ArrayList<Element>();

	public List<Element> getParts() {
		return parts;
	}
}
```

### Populating the XMLFragments with SVG

We need to be able to output essentially the original ADL document, but containing SVG fragments.  And, because we have divided up the XML into different layers and groups, 
it's just a case of transferring those layers/groups into our ADL document `RenderingInformation` objects.

To do this, I have a renderer called `ADLAndSVGRenderer`:

```java
public class ADLAndSVGRenderer extends SVGRenderer {

	...
	
	@Override
	protected SVGGraphicsLayer createGraphicsLayer(GraphicsLayerName name) {
		return new SVGGraphicsLayer(g2, name, document, topGroup, externalizeFonts()) {

			@Override
			public void endElement(DiagramElement de) {
				Element thisGroup= getTopLevelGroup();
				if ((worthKeeping(thisGroup)) && (de instanceof PositionableDiagramElement)) {
					RenderingInformation ri = ((PositionableDiagramElement)de).getRenderingInformation();   (1)
					SVGCSSStyler.style(thisGroup);	
					ensureDisplayData(thisGroup, ri); (2)
				}
					
				...
				
				super.endElement(de);   
			}
		};
	}
	
	private void ensureDisplayData(Element xml, RenderingInformation ri) {
		Object displayData = ri.getDisplayData();
		
		if (displayData == null) {
			displayData = new XMLFragments();
			ri.setDisplayData(displayData);
		}
		
		if (displayData instanceof XMLFragments) {
			((XMLFragments) displayData).getParts().add(xml);
		} else {
			throw new Kite9ProcessingException("Mixed rendering: "+displayData.getClass());
		}
	}

	@Override
	protected boolean externalizeFonts() {
		return true;
	}
	
}
```

1.  Find the `RenderingInformation` for the element in question
2.  Add the XML fragment to the `renderingInformation`

The XML now looks something like this:

```xml
<arrow id="a1">  (1)
   <renderingInformation xsi:type="rectangle" rendered="true">  (2)
    <displayData xsi:type="org.kite9.framework.serialization.XMLFragments">  (3)
     <g element-id="a1" layer="SHADOW">   (4)
      <g style="fill:rgb(179,179,179); text-rendering:optimizeLegibility; color-rendering:optimizeQuality; image-rendering:optimizeQuality; stroke:rgb(179,179,179); color-interpolation:linearRGB; stroke-width:2;">
       <rect height="24" rx="4" ry="4" style="stroke:none;" width="108" x="26" y="38"></rect>
      </g>
     </g>
	...
```

1.  The ADL definition for the `<arrow>` element.
2.  `RenderingInformation` for the arrow.
3.  `DisplayData` (embedded `XMLFragment`s)
4.  A fragment - the group containing the shadow of the arrow.

### Minimizing/Converting Defs

One problem with the XML fragments approach is that we end up repeatedly defining things like the `<defs>` (gradient fills and so on).
It would be best if we stored all of these within the XML fragments of *just* the diagram, and referenced them from there.

Also, there are lots of 'default' values that get applied to the base `<svg>` tag, which go missing by just rendering the sub-fragments
within our ADL structure.  So, it would be nice to keep these too.

I managed to get all of this to store within the `XMLFragments` of the diagram like so:

```xml
<diagram xmlns="http://www.kite9.org/schema/adl" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="auto:41">
 <renderingInformation xsi:type="diagram-ri" rendered="true">
  <displayData xsi:type="org.kite9.framework.serialization.XMLFragments" xmlns="http://www.w3.org/2000/svg">
   <defs id="defs1">
    <linearGradient color-interpolation="sRGB" id="gradient1" spreadMethod="pad" x1="50.0%" x2="50.0%" y1="0.0%" y2="100.0%">
     <stop offset="0%" stop-color="rgb(242,242,242)"></stop>
     <stop offset="100%" stop-color="rgb(204,204,204)"></stop>
    </linearGradient>
   </defs>
   <svg style="fill-opacity:1; color-rendering:auto; color-interpolation:auto; text-rendering:auto; stroke:black; stroke-linecap:square; stroke-miterlimit:10; shape-rendering:auto; stroke-opacity:1; fill:black; stroke-dasharray:none; font-weight:normal; stroke-width:1; font-family:'Dialog'; font-style:normal; stroke-linejoin:miter; font-size:12; stroke-dashoffset:0; image-rendering:auto;"></svg>
  </displayData>
  <position x="0.0" y="0.0"></position>
  <size x="432.0" y="96.0"></size>
 </renderingInformation>
 ```
 
This is done inside `ADLAndSVGRenderer`, which extends the usual `SVGRenderer`, because it enscapsulates the SVG within the original ADL document.  

```java
public class ADLAndSVGRenderer extends SVGRenderer {

	...

	@Override
	protected String output(Diagram something) throws SVGGraphics2DIOException, IOException {  (1)
		ensureDisplayData(getDiagramDefs(), diagramRendering);
		ensureDisplayData(getDiagramDefaultStyles(), diagramRendering);
		g2.setTopLevelGroup(topGroup);
		return new XMLHelper().toXML(something);
	}
 
 	...
 	
```	

1.  This is the regular `output()` method for a `Renderer`, extended to add the new details in.



### Rendering it on-screen using D3 / Animating

This is really the nub of the problem:   we have a React component on the screen with some SVG.  We have some new XML coming in, and we need to transition between the 
two.  To do this, we need to consider each `XMLFragment` part in turn.  Each fragment refers to a particular layer of a particular diagram element.  So, it could be the shadow of 
a glyph, or the border of a context.  We have the 'before' and 'after' SVG, and we need to animate between the two.   

Additionally, we need to consider what happens if there is a new element, or an element gets removed.   Luckily, D3 has a great idea for this: the `data()` command, 
which is capable of keeping track of what entities are on the screen, vs what's new and what's removed:

```js

export default class ADLSpace extends React.Component {

	... (see above)

	update(xml) {
		var react = this;
		var groups = [];
		var dom = ReactDOM.findDOMNode(this);
		var d3dom = d3.select(dom)
		var d3xml = d3.select(xml)				(1)
	
		...
	
	
		// create the layers
		this.props.layers.forEach(function(layer, i) {
			var groupLayer = d3dom.select("g[group-layer='"+layer+"']")   (2)
			if (groupLayer.size() == 0) {
				d3dom.append("g").attr("group-layer", layer);
				groupLayer = d3dom.select("g[group-layer='"+layer+"']")
			}
			
			var elements = d3xml.selectAll('*[id] > renderingInformation > displayData > g[layer="'+layer+'"]')[0];   (3)
			var d3groups = groupLayer.selectAll("g[key]")	(4)
			var elementsData = d3groups.data(elements, function(data) {   	(5)
				var key = data.attributes['element-id'].value
				return key
			})

			elementsData.enter().append("g")			(6)
				.attr("id", function(data) {
					return react.props.id+"-"+layer+"-"+data.attributes['element-id'].value.replace(":","-")
				}).attr("key", function(data) {
					return data.attributes['element-id'].value
				});
			
			elementsData.each(function(data) {			(7)
				mergeElements(this.parentElement, data, this)
			});
						
			elementsData.exit().remove();			(8)
		});
	
	
	
	}
```

1. Now we have d3 handles to the new xml and the existing svg dom.
2. Creating svg groups for each layer (ensures the correct ordering of elements in the diagram)
3. Select the ADL elements to display in the new state
4. Select existing elements to match them with
5. This key explains how to match elements between new state and old - i.e. where keys match on new and old versions, it's the same element.
6. `enter()` deals with new entities coming onto the diagram.  We create a new group for them, with ID and Key attributes.
7. This handles the transition of elements between their old (or just initialized) state, and their new state.
8. This handles removing elements that are not in the new XML.

Merging elements works in the following way:

- Go through each element in the 'from' state.  
- For each element, find a (hopefully) corresponding element in the copy-'to' state.
- Transition all the attributes.
- Recurse into that element and do the same thing. ( unless it's text - in this case, just change the text contents)
- Remove any elements in the 'to' state that didn't get transitioned.

It looks like this:

```js
function mergeElements(domWithin, domFrom, domTo) {
	if (hashElement(domFrom) != hashElement(domTo)) {    (1)
		if (domFrom.tagName == 'text') {    (2)
			// transition text content (if any)
			d3.select(domTo.children).remove();
			d3.select(domTo).text(domFrom.textContent)
			mergeAttributes(domFrom, domTo)  (3)
		} else {
			// transition the elements
			mergeAttributes(domFrom, domTo)  (3)
			
			var processed = [];	(4)
			
			for (var i = 0; i < domFrom.children.length; i++) { (5)
				var e = domFrom.children.item(i)
				var matchTo = findMatchingNode(processed, domTo.children, e)  
				
				if (matchTo == undefined) {
					matchTo = d3.select(domTo).append(e.tagName)[0][0]
				}
				
				processed.push(matchTo)
				mergeElements(domTo, e, matchTo)   (6)
			}
			
			
			// remove any surplus elements left in domTo
			for (var i = 0; i < domTo.children.length; i++) {  (7)
				var e = domTo.children.item(i)
				
				if (processed.indexOf(e) == -1) {
					d3.select(e).remove();
					i = i - 1;  //because we removed one
					console.log("removed " +e.textContent)
				} 
				
			}			
		}
	} 
}
```

1.  A quick check to see if any change is needed - using the `hash` functionality from the original Kite9 client.
2.  Checking if the element is a `text` node - if so, we just transition the text.
3.  Merge Attributes - this goes through each attribute in turn and does a D3 transition between them.
4.  Keeps track of the elements in the 'to' state that we want to keep.
5.  Go through each from state element
6.  Recursive call to merge the contents of the from element and it's matched element.
7.  Removing elements that are not needed anymore

### Merging Attributes

This works in a similar way to merging elements, except that we are going to use D3's transition here to ensure a smooth change between the elements.  This 
makes it look like we are animating smoothly from one state to another.

```js
function mergeAttributes(domFrom, domTo) {
	var trans = d3.select(domTo).transition();  (1)
	
	// transition attributes in domFrom
	var attrs = domFrom.attributes
	for (var i = 0; i < attrs.length; i++) {
		var attr = attrs.item(i)
		trans.attr(attr.name, attr.value)   (2)
	}
	
	// remove any surplus elements left in domTo
	attrs = domTo.attributes    
	for (var i = 0; i < attrs.length; i++) {   (3)
		var attr = attrs.item(i)
		if (attr.name != 'key') {
			if (domFrom.attributes.getNamedItem(attr.name) == undefined) {
				domTo.attributes.removeNamedItem(attr.name)
			}
		}
	}
}
```

1. Create the transition
2. This transitions one of the 'to' elements to it's 'from' value.
3. Remove any attributes that aren't present in the 'from' state now.

### Big Win

We've now replaced 1500 lines of Javascript with something faster but pretty much equivalent in 200 lines.  There are a few issues:

 - The key is rendering all over the place.  (I'm going to leave this for now)
 - For some reason, the group structure being sent by Batik changes from one render to the next.  This means the animation isn't quite as smooth as it otherwise could be.
 
Now we are up to here:

![Nearly complete](images/005_2.png)

 
It's working "enough" that I can forget about this now and focus on some of the main things to get right.

### Fonts

There is a *huge* performance improvement to be made from rendering fonts on the client side rather 
than the server.   If they're rendered on the server, the client is sent a huge `<path>` containing the outline of every letter.

This is OK for when we are rendering a standalone SVG file, but it's a waste if we can store font's on the client and reuse them.

```java
/**
 * Outputs Font information in a CSS stylesheet format.  
 * 
 * @author robmoffat
 * 
 */
@Controller
public class FontController {
	
	public static final String BASIC = "basic";
	
	@RequestMapping("/api/renderer/fonts/{name}.ttf")   (1)
	public void font(@PathVariable("name") String name, OutputStream os) throws IOException {
		InputStream is = AbstractStylesheet.getFontStream(name);
		StreamUtils.copy(is, os);
	}
	
	@RequestMapping("/api/renderer/stylesheet.css")  (2)
	public void guiStylesheetCss(@RequestParam(value = "name", required = false) String name,
			final HttpServletResponse sr) throws IOException, InstantiationException, IllegalAccessException {
		Stylesheet ss = StylesheetProvider.getStylesheet(name);

		Map<String, ? extends Font> textStyles = ss.getFontFamilies();
		sr.setContentType("text/css");
		Writer w = sr.getWriter();

		for (Map.Entry<String, ? extends Font> e : textStyles.entrySet()) {
			Font f = e.getValue();
			String family = SVGFont.familyToSVG(f);
			String weight = SVGFont.weightToSVG(f);
			String style = SVGFont.styleToSVG(f);
			w.write("@font-face { \n");
			w.write("\tfont-family: " + family +";\n");
			w.write("\tfont-weight: "+weight+";\n");
			w.write("\tfont-style: "+style+";\n");

			if (f instanceof LocalFont) {
				w.write("\tsrc: url('/api/renderer" + ((LocalFont) f).getFontFileName() + "') format('truetype');\n");
			}

			w.write("}\n\n");
		}
		w.flush();
		w.close();
	}

}
```

1.  This is for sending back the font data.
2.  This is for rendering a simple stylesheet to define these fonts.  It looks like this:

```css

@font-face {
    font-family: 'opensans light webfont';
    font-weight: normal;
    font-style: normal;
    src: url('/api/renderer/fonts/opensans-light-webfont.ttf') format('truetype');
}

@font-face {
    font-family: 'opensans bold webfont';
    font-weight: normal;
    font-style: normal;
    src: url('/api/renderer/fonts/opensans-bold-webfont.ttf') format('truetype');
}

```

One thing to note here is that I'm overriding the meaning of 'Font Family'.   For Open Sans, for some reason, the weight 
was not set correctly in the fonts, and so both Bold and Light were given the same font-weight.  This broke rendering.

I control between outputting path info and SVG `<text>` elements using this code in the `SVGGraphicsLayer`:

```java

	@Override
	public void outputText(Font font, double y, double x, String line) {
		if (!externalFonts) {
			GlyphVector gv = font.createGlyphVector(FONT_RENDER_CONTEXT, line.toCharArray());
			outline = gv.getOutline((float) (x), (float) y);
			fill(outline);
		} else {
			g2.setFont(font);
			g2.drawString(line, (float) x, (float) y); 
		}
	}
	
```	

### Simplifying the SVG

One last problem is that Batik outputs SVG differently at different times.  Sometimes, all of the graphics elements are in 
a single `<g>` group.  Other times, there are multiple elements.  

I'm leaving this for now, as the whole rendering subsystem will get overhauled with Sprint 7.  

Although, I think animations will be improved if we have it.

## Wrap-Up

Although this sprint has taken 2 weeks longer than expected, it's won in another way - I've been able to remove all of the client-side
stylesheet logic from the app (3 sprints' worth) so, it's a bigger gain than a loss.




