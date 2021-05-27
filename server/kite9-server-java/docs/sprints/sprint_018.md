# Editor 17th February 2018

 - I'd like a web page that loads the latest version of a document, and allows me to select things on it.

## Overview

So, as described above, we want to be able to create an application which we can use.  Since *everything* is controlled
using CSS, but we now need to add in Javascript behaviours.

Also, we need to consider the difference between *updates* and loading the whole page.  Here are some use-cases:

1.  Person hits a URL, and it loads one of our SVG documents (latest revision of).  The content-type is set to HTML, so it
loads embedded in an HTML frame.  It looks nice, and everyone is happy.  Somehow, we specify *javascript behaviours* on the page, and 
these get loaded in and applied to the dom tree.  Elements flash as you hover, etc.

- Library:  basicCanvas(), addScreenFurniture(), select(), classMatch()

2.  Somehow, we embed a form in the page for login.  Person enters details and they get logged in.  This is a POST action.  But, 
the resultant returned object is SVG.   The HTML page needs to know how to re-render (process animations, etc).  So, this means
that there should be a javascript library brought in to handle that.

 - Library:   fetch(), animateUpdate(somesvg)
 
3.  The next page that comes back is a list of their projects (again, rendered using Kite9).   They can scroll through 
the list, and click the one they want, or add a new one.  So, we're going to need some simple translation between the project
objects and the Kite9 representation of those.  (So, transform the Projects into some ADL, and return it).

4.  Same for Documents.

5.  Person loads the doc.  Again, the screen animates.  However, more javascript is needed now:  let's say they're an *editor*.  

- Library:  edit(), popup(), hover(), select()

So, let's start at the beginning, and figure this out.

## Problem 1:  Javascript

I can easily return an HTML page with embedded SVG canvas.  That's no problem.  It'd be easy to just add the Javascript to 
the top of the page, but that's a non-starter because as you move through the pages, you're going to end up needing different
bits of Javascript.  And, we need to allow people to embed their javascript from anywhere.

I'm favouring one of two solutions:  embed into the CSS or, have a special tag that we use for this.  There is already support
for the `<script>` tag, so the principle of least surprise suggests using this.

Let's try this.  (it works nicely in Safari, at least).

*If we set this in the CSS, could it get loaded in the HTML page?*  The way I see it, we should import jQuery, and any svg extensions
to that that we deem necessary.   All of this can be referenced in the CSS.   How about testing?  Ideally, we are going to use jest 
and do some server-side testing.   

So this would be part of the Kite9 processing - unpack any javascript includes and add them as `<script>` tags.   This would be
dependent on a set of `roles` that the user is in:  e.g. editor, selector, user etc.   However, this isn't perfect: the problem is that
then we are doing custom-rendering for each user.   Better to load up all the Javascript on the client side, and let the client side work
out what's relevant.

### Mocking

If we're going down the jQuery route (again) then I think it makes sense to *wrap* it in our own functionality.  This way, we get to 
mock it for testing, and we can test in Jest.  

Jobs:

- Message Converter needs to also be able to wrap the page into HTML.  Can we test that even if the SVG is embedded in some HTML, 
that the `<script>` tags are honoured?
- Message Converter also needs to be able to handle `Document`.  If someone loads a document, it should be able to output the 
latest revision in whatever format is requested.
- We need for the CSS to unpack the javascripts into the SVG.
- Select functionality
- src/generated/resources/static vs bin/static

## Problem 2: Object Model

So, we're fairly committed to using REST, HATEOAS etc.  But this bring it's own problems.  Let's think about content negotiation again.
The most common use cases are:

1.  Someone submits some ADL+SVG, and wants to get back a PNG.  This means internally, we need to load the ADL+SVG into an object, render it, then
output it in the correct format.
2.  Someone wants to load revision 40 as SVG.   Will this be the ADL+SVG input representation, or the SVG output representation, or another?  It's 
becoming clear that we are going to need to render SVG at least into different formats *at some stage*.
3.  Someone posts a new revision.  This needs to supercede the old revision, and we need to render it and store the rendered result.  *If the user asks for rendered*.

So, it's all about the format they request:  if it's HAL+JSON, give them that.  If it's ADL+SVG, return the input ADL+SVG.  If it's anything else, render it and return,
*and make sure you store the result being returned*.  So, what concepts do we need?

- A `Revision` is a point-in-time picture of some (input) ADL+SVG, and it's rendered output SVG.
- The `ADL` class therefore represents that input, but in a way that can be transformed towards the output.
- We can use `String` for the output SVG... 
- We need some kind of superclass to `Revision`, which can represent a transformation: This is `Formattable`.  

So, `Revision` extends `Formattable`, and whereas an implementation of `Formattable` *could* generate the SVG every time, `Revision` can hold onto the rendered version.

In order to pull back a rendered version of a revision, you hit the `content` rel:

```
{
  "inputXml" : "some new xml",
  "diagramHash" : "abc123",
  "dateCreated" : "2018-03-09T17:59:16.403+0000",
  "outputXml" : "renderedXML",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/api/revisions/1"
    },
    "revision" : {
      "href" : "http://localhost:8080/api/revisions/1"
    },
    "content" : {
      "href" : "http://localhost:8080/api/revisions/1/content"
    },
    "previousRevision" : {
      "href" : "http://localhost:8080/api/revisions/1/previousRevision"
    },
    "author" : {
      "href" : "http://localhost:8080/api/revisions/1/author"
    },
    "nextRevision" : {
      "href" : "http://localhost:8080/api/revisions/1/nextRevision"
    },
    "document" : {
      "href" : "http://localhost:8080/api/revisions/1/document"
    }
  }
}
```

... and specify one of the `MediaTypes` we support and it'll return either rendered or unrendered content.

### Testing

We are using the internal H2 database that ships with Spring/Java for testing atm.  You can log onto it here:

```
http://localhost:8080/console/login.do
password: abc
```

The `DomainObjectResourceLifecycleTest` configures this to be browseable as the test is running.






