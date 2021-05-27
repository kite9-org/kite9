# 19th March 2016: Sprint 2:  Markdown and  GitHub Pages #

For this sprint, I want to get the documentation in order for Kite9, so that each time I *do* some 
sprint documentation, it's got somewhere to live.

 - Sprint Articles on Github Pages   DONE
 - Some Kite9 website should point to the documentation.   DONE
 - Some vision documents   DONE
 - Decomissioning info.kite9.com, and moving all the help articles off there.  DONE 
 - Marketing (SaaS) front-matter (at least a place-holder).  This was suggested by Andrew, adding it in to this sprint.  DONE
 

## Step 1:  Placeholders 

I already have the text from the last sprint, so it's not a million miles away in terms of content.  All that's required is to push this to github and try to configure pages.  So far, i have this structure:

```
./help
./sprints
./sprints/images
./sprints/images/001_1.png
./sprints/sprint_001.md
./sprints/sprint_002.md
./vision
./vision/layout.md
./vision/problems.md
./vision/README.md
./vision/unique_features.md
```

A lot of this isn't fleshed out yet.  

## Help Documentation

So, I am missing some 'help' documentation.  I still have this from the old Kite9.  It's on a Wordpress blog served at info.kite9.com, and I initially wrote it using Evernote.  (There is a great Wordpress/Evernote sync plugin for Wordpress that I am using).  

After looking for an Evernote exporter, and finding nothing, I am thinking the answer might be exporting it from Wordpress.

I tried the "Export To Jekyll" plugin first, but actually this didn't really work all that well:  images didn't get exported, and, while the format of the export was good, a lot of the tags didn't get converted to markdown.  So, meh.

How about exporting from Evernote as HTML, and then converting the HTML to Markdown?  This was a good way to go.  Evernote has an export option, and then I was able to use pandoc as follows:

```
ls *.html | awk '{print "pandoc --from=html --to=markdown_strict \"" $0 "\" > \"" $0 ".md\""}' | sh
```

## GitHub Pages

Once I commit my changes, everything is visible on Github.  However, it's not as nice as GitHub pages would look, so the next job is to set that up.  Github Pages essentially allows you to store your static HTML inside a Git repository, and use this as the source for serving up webpages.  In essence, this means that you 
have *built HTML* checked into your project, on the gh-pages branch.  

Yes, this sounds really weird:  it seems to make no sense to check in anything but the *source* into Git, but nevertheless, this is the model they're going for here.  It *would* make sense if we were hand-editing HTML, but really, who has time for that in this day and age?

## Jekyll

The answer appears to be Jekyll.  From there, I can add Markdown files to my project, and then I think the github pages get built automatically without me having to do anything.  

By putting an instance of Jekyll in my `gh-pages` branch, github will *compile my markdown files* and show them as HTML at my Github pages site.   This is pretty cool, but seems like overkill.  

Also, it was *really hard* to figure out theming:  it's best to download something like [lanyan](http://lanyon.getpoole.com) and use this as a base for your `gh-pages` branch, adding your content on top.

## Vision Documentation

A lot of the material about Kite9 is looking kind of old, and actually, I don't really want to keep it.  What I need is to clearly explain:

1.  The Shortcomings of existing tools   DONE
2.  Why Kite9 addresses these shortcomings.  DONE
3.  Why this is credible. 
4.  What these tools empower, in terms of further functionality.
5.  Why this has a strong business case.
6.  The architecture of the new Kite9, and what that enables.

Ok, so I've added all of this.  The trick with Jekyll is *always remember the front-matter* and then it will generate something for you.  

## FAQ Index Page In Jekyll

It's possible to use the Liquid templates in Jekyll to index for you.  This is faq/index.html:

```
---
layout: page
title: FAQs
---

<ul>
  {% for page in site.pages %}
        {% if page.layout == 'faq' %}
          <li><a href="{{site.baseurl}}/{{ page.url }}">{{ page.title }}</a></li>
        {% endif %}   
  {% endfor %}  
</ul>
```

It simply scans through the list of pages and produces a set of links.  

## Nice Images

I added a bit of CSS to improve the look of images:

```css
.page img {
	width: 30rem;
	margin: auto;
	box-shadow: 3px 3px 14px #aaa;
}
```

This gives the images a drop-shadow, and just separates them from the body-text sufficiently to make them *definitely images*:  otherwise it's sometimes hard to tell what's an image and what isn't.

## Deploying to info.kite9.com

I added a CNAME file into the gh-pages root, containing

```
info.kite9.org
```

And now, github expects to host the repo when it's webserver sees this URL coming in.  I just need to change my DNS entries now so that info.kite9.com points to github (this is a CNAME).

To make this work properly, I needed to change my `_config.yml` file so that it expected the baseurl to be `/`.

This is basically saying which directory your files will be served from: on github, it was `/k9`. 

```
url:                 http://info.kite9.com
baseurl:             ''
```

## Adding Tracking

Seems to be a bit simpler now, I just added this to head.html:

```
<script>
	(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
	(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
	m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
	})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');
	
	ga('create', 'xxx', 'auto');
	ga('send', 'pageview');
  </script>
```

Which I pulled straight off the google website. 
 
## Videos

I just created a single `videos.md` page and then used youtube -> share -> embed to get the `iframe` tags 
to place in the page.  Works for now, I guess.

## Example Images

Problem with what I have done now, is that the home page of Kite9.com is broken, because it links to a lot of the content on info.kite9.com.  So, I need to fix this too.

On the other hand, this makes me think that perhaps what we *really* need to do is combine these sites together so that actually they're not separate after all.  But, the problem with this is, right now I don't know how to reconcile k9 redux with the original, *without breaking the GUI* (see sprint plan #4).

So.. I guess the answer for now is really just to fix the links in the website, and point them to the right things.  DONE.   This was a bit of a pain - don't change the URLs again.

## Design of A SaaS Front Page

Broadly, I need to do the following:

1. Find a template.
2. Populate the template.

### Some initial design

- Mention it's in Beta.

https://blog.kissmetrics.com/8-saas-elements/   :  What Makes a Good SaaS Site.  

#### Part 1, Above The Fold.    (header)

 - *A one-sentence phrase that captures what you do*:  Diagrams laid out sensibly, crisply and automatically the way you want them.
 - *Call To Action* :  Sign up (mention it's free), or *request a demo*.
 
#### Part 2, the Video (callout)

 - Full-width link to the video.  Might need to improve this a bit.

#### Part 3, Screenshots  (portfolio)

 - *Screenshots*: Ok, we can do this.  Might need to make them a bit cooler.
 
#### Part 4: The bubbles.  (services)

*Feature Breakdown*:   Ok, this sounds like a lot of words.   Can we use the FAQ here?  If I describe a features as existing, add an image, then link the FAQ article... 

#### Part 5: More Info / Vision (about)

- I should mention my roadmap / vision pages

### Finding The Template

I already bought and paid for the *Mineral* theme, but never used it.  This is a WordPress theme, but I am thinking it shouldn't be too hard to repurpose (let's try.)   Problem is, this is all in PHP, and Jekyll doesn't use that.  I need something simpler.

*Or, I could just do this by hand.*.   The problem with Mineral is the sheer amount of crap that ends up being imported, which would be a testing and maintenance nightmare.  

Luckily, someone has designed a *really simple* SaaS Jekyll theme: https://volny.github.io/stylish-portfolio-jekyll/

Nice work, and it has an Apache license.  

I'll start with this.

### Populating It

#### Marketing Messages

Some marketing messages, to add:

 - You want everyone to have *a common viewpoint*.
 - Easily keep things up to date as the system changes (documentation goes stale)
 - Generate diagrams from your code (so the map reflects the territory).
 - Save time by letting Kite9 do the layout for you
 - Use common sets of symbols, so that everyone understands what the diagram means
 
#### Pricing Page

So, I have a free version, (maybe up to 5 users), a priced version (for up to 20 users, coming soon) and a self-hosted version (£ call)

Managed to download a simple bootstrap template here:  http://www.bootply.com/80183

To get around the need to set up lots of complex payments workflow, for now I have just configured some mailto:  addresses for the buttons, like so:

```
<a class="btn btn-lg btn-block btn-danger" href="mailto:support@kite9.com?subject=Tell me about Hosted Kite9&body=Hi, Please email me back with details of the hosted plan for Kite9.  thanks.">CLICK TO ENQUIRE</a>
```

#### Kite9 Logo

I managed to find an old Adobe Illustrator version of the logo, and convert it to SVG using an [online tool](cloudconvert.com).  It was a pretty simple job then to attach it to the top-right of the page.   However, because it's transparent, it doesn't really work on a white background, so I made it black and grey.   Since most backgrounds in Kite9 are white, it looks acceptable, and matches up with the hamburger nicely.

### HTTPS

Where are we on this?   I guess for the documentation, this will be handled by github pages. ( no action needed)

### Unifying The Themes

Lanyon looks very different to the bootstrap I am using on my main Kite9 site.  It would be nice if these didn't look so jarringly different.  To achieve this, I am going to need to:

- Override the font used 
- Import the header bar
- Change the colour scheme.

What should the header look like?  Ideally, as simple as possible.   I like the idea of the hamburger as giving you more information.  I think we should keep this, and actually use it in the main tool as well.   Ok, so I think, going forwards, we'll have just the Kite9 logo on the right (rendered from SVG), and on the left, the hamburger.

On the other hand, bootstrap looks really nice for the main screens: should we stick with this?
In the GUI, when we get that far, we will add  Undo, Redo, and Resize as icons, but these won't be buttons (as they are now), they will just be outlines.  

This should look a lot cooler.  The hamburger should "transform" into the spinner, when pages are loading.  


### The "book a demo" button.

Just another mailto:  address.  I want to make sure I don't get spammed too heavily, especially at my main kite9 address.  So, I set up an email alias on the rackspace account.  

I had to go to ```my.rackspace.com/```, and then, once logged in there, products -> Cloud Office -> Open Cloud Office Portal.

Then, I could set up the support alias, and configure it to point to my main address.


### Replacing the two background images with something more relevant.

Downloaded a couple of images from dreamstime.  Resized using Preview on the iMac.  


### Common Fonts

My SaaS front-page uses these CSS:

```
font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;
font-size: 27px;
font-weight: normal;
```

My Lanyon pages use:

```
font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;
font-size: 18px;
font-weight: normal;
```

... so I guess this is fine?  They don't look similar, and I think the problem is that I have two 'head' page-includes, where what I need is to factor out the common stuff.  

Eventually, I got it down to this difference:

```
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/bootstrap.min.css" >
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/stylish-portfolio.css">
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/lanyon-drawer.css">
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/kite9.css">
```

... On the SaaS pages, and

```
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/bootstrap.min.css" >
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/poole.css">
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/syntax.css">
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/lanyon.css">
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/lanyon-drawer.css">
  <link rel="stylesheet" href="{{ site.baseurl }}/public/css/kite9.css">
```

... On the other pages, which is really close to being the same.


### Deploying to Github Pages

ALthough everything worked fine running Jekyll locally, after deploying to github, a lot of the pages' templates were broken.  I never really got to the bottom of this:  error messages weren't logged anywhere.   

After much messing about and testing ideas, I came away with *nothing*, but the act of messing about seemed to fix all the pages.  So just trying things multiple times worked, strangely.


 