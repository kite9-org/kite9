# 31st March 2016: Sprint 3: Grails 3 *

## Goals

- Upgrade the original Kite9 to Grails 3.
- We can keep our existing Kite9 infrastructure working by moving to Grails 3, which is based on Spring Boot.
- This probably means moving to gradle builds too, and therefore a gradle fabric8 plugin.
- I think ideally we should stick to the spring/jpa-based persistence because it's more future proof, so we need to move the rest of those entites and add tests for them.
- This means that Kite9 should be completely ported to AWS.
- Add a table for the diagram contents, unrendered and rendered
- Need to check email works

## Problem

The problem with even doing this sprint is that it's a backward step:  we're not going to be able to get this UI working correctly anyway.  At what stage will we have a converted Kite9?

## Background

This sprint came about because of talks with Andrew Lockley.  He said we needed to get users in front of the system and the best way I can think to do this is to port the platform onto AWS fully. 

This means we're carrying a bit of baggage:  we have Grails baked into our application still, which will be mainly used for rendering views.  (Ideally, that's all it will be used for:  I think we should convert all the rest of the controllers to Java / Spring Boot.. in [Sprint 5](sprint_005.md) )

## Downloading Grails 3

From the look of it, the simplest thing might be to try and 'port' all the code from my existing server project into a new installation of grails.   This is going to involve moving some files around and upgrading the configuration.  
However, from the off, it looks like Grails 3 is going to be a big problem:

 - I'm struggling now with my code not compiling, because my domain objects are gradle, and compiled *after* java code, which sadly depends on it.
 - The security plugins seems completely broken here.  

What to do?  I guess the basic problem is neither of these things - I want to move them to straight java anyway.  The bigger problem is the GSP pages.    Can I get these working at all?

I am thinking the answer is no:  there's just too much crazy going on in them.  A better option might be to port them across and convert them to something else at the same time, but that's likely 
to take a *loong* time. 

## Other options.  

- We could simply rewrite the GORM code as JPA, and then drop this in as a replacement for 

** Pausing This Sprint:  Going To Work on Sprint 4+5 first.  **

** Cancelling this entirely - we're not going to do this (13/5/2016) **
