# Technical Debt

I'm putting issues in here that are too big to reasonably fix without a few weeks' work.  
Otherwise, I'll be stuck on R18n forever.

# Issues with Grouping

Having fixed this issue with gridding, I am led onto a different issue.  Grouping isn't quite in the right order to make 51_7 work: left-side groups get completed before 
right-side ones, and this means the expected buddy merge between top-right and bottom-right doesn't happen before left gets merged completely.

Why is this?

- First, the ordering of `MergeOption`s is suspect:  all perpendicular merges should happen after all in-axis merges.  This is just sensible, but doesn't seem to be 
enforced.
- Second, we need to make sure that we re-appraise merge options when we change the axis from UNDIRECTED TO X_FIRST_MERGE or whatever it is.  Currently, we don't 
really process this properly.
- Third, we need to make sure that if a merge changes priority, we update it and put it back in the queue.   Not sure this happens yet.

See test 51_7.

Priorities should be all about merging things together that are as close as possible in the plane.

However, we can't always say how close they will be:

- IN the saem container, they could be a container width apart c.
- nearest neighbours, will be 1 apart
- in axis, 0 apart
- non-nearest neighbours, will be l apart, where l is the length of a link.  
- in different containers, > c apart? (depends on container size)
- aligned, a glyph apart (will depend on number of links leaving glyph)

... and so on

So, somehow, with imprecise knowledge, we need to prioritise on this basis.

# R18n / getLength()

1.   getLength has a hack in it where we use 2 subsidiary distances to figure out length.  This shouldn't be done:
if we are using `minimize` distance, then we should add a constraint so that the length is fixed.  i.e. remove all the crap in VertexTurn.recalculateLength()
and just set minimum distances beforehand.

2.  Where we have u-shaped turns, it's insane to even try to predict which one is shorter.  It doesn't even matter.  Instead,
a `VertexTurn` should just be allowed to accumulate `Slideable`s in it's length, and then when we come to the next G-shaped turn
we set a constraint against all of them.   
 - (39_6 for example) 
 - 9_8:  problem with compaction (not fixing)


These sound quite simple but I've been bitten before so I don't want to get into this now.  These are both highly related and should be done together.

# 38_2: Positioning

Ok, what's going on here? 

# Label Positioning

Why can you only place labels on the bottom edge of a container? 

 - Mainly, because 2 labels on the same edge would totally interfere with each other...
