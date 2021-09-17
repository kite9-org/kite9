# 2nd February 2018: Sprint 15: Command Pattern

- Insert element (id, id-less xml bits)
- Modify element (id, what it looks like after, before)
- Remove element (id)
- Set attribute
- All basically stuff that allows you to manipulate XML (this allows palettes to work).
- After posting the command, it should return the new JSON (for react), or an error message should pop up. redux can handle that.
- Commands should attempt to apply to the active diagram.
- Load

# 1.  Creating Commands

There's not a great deal to write about here, apart from maybe the flow:

- Command comes in.  It'll be JSON.  Let's take a modify command.  This will have a docId, elementId, pre-change XML, post-change XML.
- We load the document, and the revision associated with it.
- We load up the XML from the document.  This will use the ADLExtensibleDocumentFactory.  Code for this exists already, in the `XMLAbstractTranscoder`.  How about extending the Kite9SVGTranscoder to work with documents?
- Having loaded up the XML, we find our element in the document.  Using `findById`.
- Then, we serialize this as a string, and compare it to the pre-change XML.  If it matches, we're go.
- Parse the post-change XML (again using the ADL parser) and pop this in.  Somehow.
- Perform the save operation on the documents.
- With the already loaded DOM, we can now perform a *render* of the data, producing an output XML `Document`.
- Notify any document listeners of the fact that there is new XML.

## REST For Commands

Ok, I'm struggling now because I chose to use HATEOAS for the repositories (alright, chose is the wrong word).  Now, I'm thinking I need
to somehow use the same thing for the command pattern.   How would this work?

- You can't *retrieve* commands in the conventional sense.  But, maybe you can retrieve and post `Revision`s?   This seems like an idea worth pursuing.   
- I spent a long time on this with no joy:  https://stackoverflow.com/questions/48760910/posting-with-spring-hateoas-spring-data-rest
 
How do I want this to look?   A `Command` and a `Revision` are *intertwined*.  I could POST a revision with a command inside... but I would get back 
the `Command` or the `Revision`, whereas what I really want is a controller that I post a command to, and it returns ADL.  The great thing is that 
now, we hae a single SVG representation for an ADL diagram, whereas we used to hold the stylesheet separately. 

## Types Of Commands

- Create / Modify Node (After, Inside, Existing State (how much state?), New State)
- Delete / Move Node (Node Id, deep?, After, Inside)

Ideally, I want to be able to delete a *list* of nodes with the same command.
Can I move a bunch at the same time?  I guess so... if I move in order.

If I am just modifying the *attributes* of a node, then it's not going to be a *deep* modify.

Should commands be individually reversible?   If so, this allows each user to undo their changes, so long as they don't interact with any
others.   This seems too difficult and out-of-scope: we should just do the usual thing of rolling back to a previous revision.

## Processing Burden

Old Way:

- ** Client Side:** : Parse XML.  DOM stored (one original, other in Raphael), serialized with each command.  Sent to server as a block of XML.  
- ** Server Side:** : Parse the DOM (entirely).  Render.  Return new DOM.

New Way:

- ** Client Side: ** : Parse XML.  DOM stored (one original).  Send fragment of XML with each command.
- ** Server Side: ** : Load the full XML from disk.  Parse.  Parse the fragment.  Make changes to DOM.  Render.  Change back to Return new DOM.

We *gain* on network transport costs but there is an overhead in loading from disk/cache on the server side.

## Serializing and Parsing

THere is a way in Java for Serializing now:  `DomImplementationLS` class.  This is pretty useful.  It seems to output in UTF-16 
which is a major pain in eclipse though.  

## Comparing

It's necessary to compare the XML in the "expected" state with the "before" state.   To do this, we're going to use
`DOMDifferenceEngine` again, which we've used before.   However, it's going to need some customization, as it needs to 
*stop* comparing when it comes to IDs.   i.e. the doc could look like this:

```xml
<a id="sdfklj">
  <b id="sdfsd" attr="ggg">
    <c id="eee" />
  </b>
</b>  
```

We need to compare with this and match (if it's a change to `a`):

```xml
<a id="sdfklj">
  <b id="sdfsd"></b>
</b>  
```

We can do this efficiently with `xmlunit` as usual, although this time I am using `DiffBuilder` which has more control.

## Namespaces

In order to parse the xml fragments coming into the commands (before, after) they need to be properly namespaced SVG diagrams.  I think we need to say: they *are* 
proper diagrams, but only containing a single element after the `<svg>` tag.  We *could* allow them to have more complex structure, and iterate through child
elements, but this is perhaps excessive.   Also, this means that we can be flexible about namespace declarations.


## Serializing Correctly

On the client side, we need to serialize and leave elements with IDs as *stubs*, that is, placeholders.   How then are we
going to load in this data?

We're going to need a special kind of SAX parser that can move in referenced elements as-needed.  You could have something like:

```xml

<a id="top">
  <thing> 
    <a id="elem1" />
  </thing>
  <shape>
    <b id="elem2" />
  </shape>
</a>
  
```

## Resolving URLs

Whenever we construct an ADL document, we provide it's URI, so that we can figure out what relative URLs will look like.  But, how should this work on the server?   I guess, 
each document will have a URL, and then the ADL for that will be at the same URL.   So generally, we're not going to use relative URLs.  Also, this approach totally screws 
up when the document is transferred to the client.

So, the answer is, don't use relative URLs in the documents?  A f**k it.





