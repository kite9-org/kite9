# Kite9 Delivery Plan

Rather than a grand “ta-da”, is there any way I can split this out and do it in bits?

It would be really nice to not have to do it in one big go.  In fact, we should ensure this is the case.   How can we do this in 2-week (ish) “sprints”?

An alternative might be to “rebuild” Kite9 from scratch in a new project.   If this were done, we could start in a different container, and firstly just do building pages.  We could use a new mysql db with Spring boot.

It would be nice to do this so that we have something to see as we go along.
Each release would be to “production”, which would be an amazon EC2 instance, with load-balancer and MySql database at the back end.

## General Sprint Goals

1.  Release instantly.  i.e. full automated tests.  I don’t know yet what this means for Javascript, but I’ll figure it out.
2.  Proper load-balancing etc.
3.  Integration tests running against the server.
4.  BPML
5.  Swimlane Diagrams
6.  Sharing Entities (although, not in this plan)
7.  Fixing up the GUI so we can do quick releases.
8.  Unleashing the full power of SVG.

## The Sprints

### Set up a new container-based server.

- With the same entities in it as Grails. 
- It should be possible to query the model against the original database via REST and it still works.    DONE
- We write a bunch of tests that retrieve the objects as JSON (with bits of embedded xml string, I guess)  DONE
- At this stage, we don’t even need Kite9-core. 
- We need to be able to deploy to the cloud, and run integration tests there. DONE

[Sprint Notes](sprint_001.md)

### Documentation / Check in.  

- Move to github / markup and lose everything that no longer makes sense.  I.e. complete clear-out.  Just keep stuff that makes sense as we go along.
- Some Kite9 website should point to the documentation.
- Would be nice to publish the end-of-sprint stuff on the wiki or somewhere.
- Some vision documents
- Published via github.

[Sprint Notes](sprint_002.md)

### Security

- User entity, creatable, queryable via rest.
- Sign Up Screen:  *Name*, *Email Address*, *Password*.  This would send an email out to *confirm* the email address.  
- A special URL would confirm the email, being a hash of some secret salt and their details.
- Edit screen:  user is allowed to go onto their page and change the email, but that invalidates it again (meaning we don't send to it).
- Log-in Screen (steal these from the existing grails app for now)
- Limiting the projects you can look up, based on who you are.
- Need to check email works

[Sprint Notes](sprint_004.md)

### D3 To Load XML

- We should be able to render the XML returned by passing it through a simple d3 component which turns it into SVG.
- Every item from the object model will be a group, which will potentially have some svg elements associated with it.
- Using D3 to display on the screen.
- This should be a simple drop-in replacement to Raphael, and clear out this tech debt.
- Tests should look like "here's some rendering information, handle it".

[Sprint Notes](sprint_005.md)

### Setting Up Travis + AWS

- building of all projects (including Visualization)
- Continuous build of master, (releasing to Amazon automatically? )
- Sort out DNS
- mail gateway
- automated service testing (running docker tests)

[Sprint Notes](sprint_006.md)

### Server-side CSS

- Modify XML Loader so that elements are annotated with CSS Attributes
- Get the CSS loaded up on the server side by Batik.
- Extend CSS so that we add our new attributes for shapes, etc.
- Remove style information from the java Stylesheet

[Sprint Notes](sprint_007.md)

### Object Model Part 1 - Containers

- everything should be parts and containers.  Links should be reformatted.   Ideally, we are backwards-compatible with what came before.  So, you can load up the original diagram xml and it comes back in the new format.  (this means objects like Glyph still work...)
- Containers
- Parts (with type = glyph-simple, glyph-with-stereo etc.)

[Sprint Notes](sprint_008.md)

### Glyphs Using Grids

- Grid layout for Glyphs (i.e. layout=grid ) 
- Removing @Ignores
- Containers - allowed leaving edges. (brought in as needed to fix errors)

[Sprint Notes](sprint_013.md)

### SVG As The Output Format

- In order that we can properly take advantage of fills, we need to start using SVG as the output format.
- "Common" section of the diagram, containing things to reference.
- this means converting our displayers to use SVG rather than Graphics2D.  
- Better to get this out of the way early.
- Update test results to check SVG, where necessary.  Don't use this for all tests, just some.
- Split tests into planarization, orth, compaction, display so we can add SVG to the display ones only.

[Sprint Notes](sprint_011.md)

### Centering

- Centering of content within a container
- Centering / alignment of text
- Connection Labels and overlapping 

[Sprint Notes](sprint_014.md)

### Looks

- Fonts
- Add shadows in
- Complete work on the designer stylesheet so we can display everything in high fidelity.
- Control Layer
- Container Corners

[Sprint Notes](sprint_016.md)

### Visualisation Engine refactoring

- some new entities:  rendered data entity.  diagram xml entity.   (should we have a single entity for hashed content?  Might be a good idea)
- currently, this is groovy code.  Refactor so this is a first-class Java, Spring service.
- Use REST, use the user token to validate requests.
- If we’ve been refactoring carefully, this should also still work.
- Write some tests for this.
- Store results in the content table.
- hard-code the stylesheets for now.
- We need an entity in the system to hold details about registered CSS stylesheets.  We will use the public URLs of these in the XML, but the actual values will be cached in the DB to speed things up.
- Should be an option to say “don’t update” or “update every…”, and the cache, when returning, will check and behave accordingly.
- So, handle this caching.
- //We need an "Entity" element in the database, which we'll also use later for indexing the XML.// Don't do this yet

[Sprint Notes](sprint_017.md)

### Command Pattern

- Insert element (id, id-less xml bits)
- Modify element (id, what it looks like after, before)
- Remove element (id)
- Set attribute
- All basically stuff that allows you to manipulate XML (this allows palettes to work).
- After posting the command, it should return the new JSON (for react), or an error message should pop up. redux can handle that.
- Commands should attempt to apply to the active diagram.

[Sprint Notes](sprint_015.md)

### Editor

- We’re going to need a lot of the original app now.  So, we need to structure this a bit better, and use webpack / npm stuff.
- Add in a javascript testing framework.
- Ideally, we should be able to load a document and have behaviours on the document to allow you to select elements.
- Some stylesheet should define the GUI behaviours.
- We need some tests for this in the Javascript.  Clicking on containers, clicking on parts, clicking on links.
- Somehow, select should extend the react layout to include the shapes needed to make select work… (not sure we will need these anymore though?)
[Sprint Notes](sprint_018.md)

### Context Menu

- Define the context menu plugin.   This should come up when people select stuff.  Once you select stuff, the menu should pop up.  What does it contain?   Nothing yet.  Just needs to come up in the right place.
- Add a couple of placeholder options.
- Options can appear and be greyed out.
- Write a test.

### Modal Plugin

- We want a javascript function that opens up a modal dialog.
- It should take an array of fields, and callbacks for validation, and possibly callbacks for options, if it’s going to be populated with some.

### Menu Plugins

- Add things like “edit text”, “delete”, “surround with container".   Edit text will need to use the modal dialog.  Plug these into the context menu.

### The Palette, part 1.

- First, a palette needs to pop up, with appropriate elements in it.  To do this, we need to look at palettes defined by the stylesheets, and also look at the context of where we are putting the element.   I think this is a call to the server for some JSON which can be rendered containing the palettes in question.
- Write a test that this comes up in the modal plugin.

### The Palette, part 2

- When you click an element, the palette should close, and you are dragging around the element that you want to place.
- What would be the command for this?  It could be a fairly complex piece of XML.   So, we need to create a command that adds XML.
- Extend the test to do this.

### The Palette, part 3

- We need to grey out elements that are not allowed.
- So, this is some kind of plugin to the react component again.  (We are going to need some general way of adding callbacks to react).
- We also need a way to say which elements a container can accept.  (or alternatively, which elements can go in container x).   Which way round makes more sense?  Either a container can accept anything, or it can accept only certain kinds of element.

### Top-Level Menu

- Again, this should be pluggable.
- We want to add the zoom controls, as well as undo/redo.
- Undo and redo are actually going to post commands to the server now.
- Write some javascript tests for the existence of these and that you can press them and the correct actions occur.

### Keys And Labels

- Currently, keys are a big mess.
- It should be possible to place labels anywhere (and everywhere) inside a container
- It should be possible to set the alignment for the label sides.


### Link

- So, the basic thing is, you click on an element, and you can select draw link.  And, then it draws a link to another element.

### Link to New

- Draws a link, but puts a new element down again (the last one from the palette).  So again, we need to create some kind of event that draws to the mouse pointer, recognises the container we’re under, etc.  And then adds an element to the container.   This is a combination of the palette-drop plus link functionality.
- Must add a test.  Redux must be able to have the state of what is going on, what is being dragged, etc.


### Undo / Redo Commands

- These are going to simply take you back and forth through the diagram history and choose the “active” diagram.  Revisions will still be in numbered / timestamped order, but when you undo, you undo for everyone.  But nothing is lost.  NAILED IT.
- You should be able to head back to any previous version in the diagram history and say, “this is the active version”.  We should have some marker record somewhere to do this.  i.e in Document.
- All other commands end up creating a new revision, and setting the active document to that.


### BPML:  Write a stylesheet for this in CSS

- Include most of the basic entities, render a diagram using it.


### Object Model Part 2 - Links/Ports/Terminators/Labels

- Ports
- Aligns (allowed arriving edges)
- Links as “straight” rather than LEFT, RIGHT etc.
- STRAIGHT directive.
- Ability to join links to other links/edges rather than just vertices (deprecate side vertex stuff, ContainerCornerTransform stuff)
- Allow gridded containers to have links leaving them.

[Sprint Notes](sprint_010.md)


### Fix aligns.

- We want to be able to create these from the gui too by selecting a bunch of align-able elements.
- You need to fix the compaction process to respect these too.
- Write a test that makes sure this works.
- Generally, the code we already have for the javascript is good.
- An align should have layout - it should just be a long line on the diagram in the end (if show layouts mode is on)

### Layouts

- Layouts need some kind of background image so that we can see how they are set up.  You can do this ridiculously easily with patterns, where you can define a pattern in SVG, and use it as a fill for another SVG element.  awesome.
- Use CSS to turn on layout information at the global level.
- Write a test to test what the screen looks like with layout info on or off.  Problem is, this is more global state, and we wanted to avoid global state (especially invisible global state)

### Hierarchical Layout

- Allow this as an option.  There is the secondary option of left, mid and right-aligned hierarchies.

### Reverse Link

- Swaps the ends of the link around… the Ends point at the opposite elements.
- Test

### Max-Size Algorithm

### Collapse Container

- Containers should have a smallest-size option.


### Grouping Fix



[Sprint Notes](sprint_012.md)

## Known Remaining Issues

- Line-lengths: we should be able to set these.
- 14_3 could be better
- Test 51_7 is broken because we do gridding in a funny order.  Investigate this and provide a fix.
- See [Sprint 13](sprint_013.md) for how we should do gridding, and allow spanning squares.

[Notes](technical_debt.md)

