# ??? : Sprint 10 - Overhaul Object Model - Ports

- Ports (overlapping elements on a glyph)
- Links as “straight” rather than LEFT, RIGHT etc.
- STRAIGHT directive

# Ports

These are interesting.  They are effectively "extra" pieces attached to the edge of a `Glyph` (or whatever).  While
ports are effectively zero-size, the idea of a Decal is that it does have size, which is considered as part of rendering.  

We already have something a bit like ports:  when we create containers, and attach links to them, we create ports then.  So, really, when we
decide how to represent an object on the planarization we need to consider if the component have edges leaving from a particular port?  If so, then really the component should be represented by it's corners on the diagram,
and the ports should be represented too, in the same way (we already have code for this, we just need to generalize the approach, rather than making it 
specific to Containers).

When this happens, we can add the ports in the Planarization phase, much like we do for an existing container.


## Port Approaches

- **Port position is significant**, therefore overlap before you reorder the edges.   (An edge meets a particular point).  This is true of 
directed edges, which should meet the "middle" of each `Contained` (so, we define ports for the middle).
- **Port order is significant**:   maybe order indicates something?  In this case, we should create ports ahead-of-schedule in the right order.
- **Port side only is significant**:  therefore, instead of having one link per port, we create lots of links, and then split them out.  I believe
we already have code for this too.
- **Side is insignificant**: therefore, just have a central position.

So, we're going to need to factor this out so that it's not specific to Glyphs/Containers/Arrows.  This is also going to impact the behaviour
of the GUI. 

I think we're definitely going to need some experimentation around this, but it's likely we're not a long way from this with the way we set up container 
ports as it is.  

## Defining Ports

The available ports are a property of the thing being connected to.  There are a couple of approaches which could work:  

A glyph has a number of them, defined positionally much like any other components (using `occupies` ?).  Then, you would need
to represent them in the xml, with an ID, for them to be usable.  This seems like the most *apropos* solution, because then the Glyph can 
show extra detail (by way of it's port).  This does open you up to the possibility of ports getting accidentally deleted.  (Certainly, they can be selected)

We should use the XML/CSS approach to define ports in the usual way (and, I guess, allow them to be added in the usual way).  Even if a port is unused, you're probably
going to want to represent it on the diagram so that you can drag things onto it.

## Port Positioning

Another idea is that to draw the port, we might need to define where it is, and then draw from that point.  Within the context of layout, ports and decals
should therefore be defined by their *center*, and drawn from that point.   Defining the center might be tricky, you could end up with something like:

x: (xs-x2)/2
y: xe;

`xs`, `xe`, `ys`, `ye` should *always* exist, even when we don't have a grid.

This allows us to define some standard port-positions, and use them.  So, we should be able to include a child XML element, which represents a port, add it to 
the planarization, and then link to it. 

A better approach might be:

side: top/bottom/left/right;
pos: 50%/10px/-10px;

This means it would be impossible to define a port within the body of the container.  However, by doing this we are veering away from the approach used for Decals.
I'm not sure this is necessarily a bad thing.

## Ports In the GUI

Let's say you create a glyph, and then decide you want to fix the position of a link arriving at it.  The obvious option is to click the link end and 
select "create port here".  Then, we have a port for that link.  If there are other links arriving on the same side, they *may* be interrupted:  what we should
do is ensure that there are "above" and "below" container ports that they can connect to.  

Sometimes, other links will already be on ports.  I don't see this as being a problem: we ensure "specific" ports are interleaved with "container" ports for the
other elements to reach.

Having created a port, you should be free to move it where you like.  i.e. Set the side it exists on, and the position on that side.

Ports will exist within the XML of the document.  

## Point Ports

Sometimes, you'll want to have a particular point in an object (a handle, say) which has all the links.  In this case, you 
might want all the links to meet at a specific point, on a specific port.  

You can either say:  
 - a port should only have one link
 - ports can grow as links are added
 - ports should "aggregate" their links on top of each other.
 
Not sure which of these is right, but maybe that's a question for another day. 
 

