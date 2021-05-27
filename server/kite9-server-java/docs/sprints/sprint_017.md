# 23rd January 2018: Sprint 17: Rest-Based Rendering

- some new entities:  rendered data entity.  diagram xml entity.   (should we have a single entity for hashed content?  Might be a good idea)
- currently, this is groovy code.  Refactor so this is a first-class Java, Spring service.
- Use REST, use the user token to validate requests.
- Write some tests for this.
- Store results in the content table.
- hard-code the stylesheets for now.
- We need an entity in the system to hold details about registered CSS stylesheets.  We will use the public URLs of these in the XML, but the actual values will be cached in the DB to speed things up.
- Should be an option to say “don’t update” or “update every…”, and the cache, when returning, will check and behave accordingly.
- So, handle this caching.


# 1.  Looking Back

So, I am reviewing the k9-server project that I started in March 2016.  There are a few assumptions now that seem wrong.

- First, the approach to authentication seems off:  obviously, I was trying to create a stateless architecture for logins, 
but nowadays everyone is using JWT, so really I should upgrade spring to use that (also, I have experience of using that from
work so it shouldn't be too hard.   Nevertheless, in the meantime, it'll probably do.
- Second, I've got all these `Format` classes, which map to different ways you can bring back the diagram.   SVG is now our underlying
format, and everything else should be converted from it.  This is going to require more Batik-work.  I think the alternate formats
are a good idea, but it can wait.
- All the ADL classes are gone now:  we're dealing with pure XML all the way through.  This has blown up a lot of the old code.
- We convert `svg+xml` as the input to pure SVG as the output, now, so a lot of the `MediaTypes` seem off.
- We have some front-end code using React and D3.  Like the content types, this is from [Sprint 5](sprint_005.md).  This is used to transition
between one SVG diagram and another.   I think inevitably we're going to have to change that, but right now I'm not sure how:
  - Can we have a simple transitions package that allows animation without D3 or React?  This might be good.
  - Alternatively, is there a react-like package that allows transition from one XML doc to another? 
- I think we have lots of odd code lying around that does Batik-y SVG manipulation.  We need to remove this.
- FontController is a bit anachronistic now:  everything else just uses the regular stylesheet, and `ResourceRenderer`, so we need to upgrade to that,

# 2.  Fixing the Formats

First thing is to get the `RestRenderingIT` working again, and that means fixing up the image formats.  Notably HTML, PDF and PNG are broken.
I've commented these out for now.  I don't really need them yet.

# 3.  Foreign URLs / Mail

This is a problem.  When we are loading the tests in eclipse, we are ok to create URLs which link to filesystem elements, and they
get loaded up fine by the renderer.  However,  when we run in Docker, everything goes crazy.

So, first up:  do we really need Docker?  The way I am taking things at work is to build everything into the Spring application that it needs
(the opposite of Microservices).  Given the overhead of Docker, I feel we probably should do the same thing here, and make the *executable jar*
the unit of deployment.

There's just too much I don't know about Docker.

**Aspirin** might allow me to get around this limitation.  Then, I can simplify the project and run everything in the one executable.
This is probably a couple of hours work and will save me a lot of time.  You live and learn.

This actually will solve the URL problem entirely, too.  Although ideally we should look at ways to cache URLs so that we don't 
have to load fonts every single time we render something.

## `ParsedURL`

So, the main way resources are loaded is with `ParsedURL`, and the protocol handlers.  I can replace this on startup to implement
some kind of resource caching, as I need it.

## Do We Need This?

Possibly.  When we are running tests, it would be really handy to be able to use URLs that are part of a repository.  But, what's the 
workflow here?

- Someone writes a stylesheet
- They attach the stylesheet to their Kite9 diagram, and begin editing.
- They want to change the stylesheet, so maybe they upload a new version.
- We can use a CDN in front of Kite9 to cache this big stuff (eventually, so long as we don't allow changes)

What if they *don't* use the repository?  Is there any issue with loading files from elsewhere?  I am worried about an attack vector
I guess, especially since we have to load fonts.  Work happens when you load a Kite9 diagram.

In the first instance, probably, there won't be much going on, and it might be possible to have an in-memory cache of some of the fonts 
and whathaveyou.  What about images?  Templates?  These all would need to be stored too.

# 2.  Do We Need This *Now*?

The only reason I can think of why we might need this now is so that we can load stuff up for testing.  If we pull in these files 
and stick them in the repository, maybe that helps for testing.   But we could write some really minimal tests that don't use any 
URLs and just have nested stylesheet information in them.

So, all of this, I am going to pause.








