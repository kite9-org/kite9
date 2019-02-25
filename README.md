[![Build Status](https://travis-ci.com/robmoffat/kite9-visualization.png)](https://travis-ci.com/robmoffat/kite9-visualization)


Kite9 is a SVG Visualization Library for converting XML into SVG, useful for creating diagrams and user interfaces.

[Basic Kite9 Process](docs/images/process.png)

For examples, head over to the [Risk-First](riskfirst.org) website to see some example Kite9 diagrams.

## Basic Features

- **New CSS Directives** Such as `horizontal-align`, `min-width`, `padding` and `margin`.  These are used in HTML CSS, but are now available for use in SVG.
- **Containment** It's possible to express XML elements as containing other elements, and have the positioning and sizing adjust according to this.
- **Templating**.  Use the `kite9-template` CSS directive to specify an SVG template to use to render a piece of XML.
- 

## Batik

Kite9 is written in Java, and heavily based on [Apache Batik SVG Toolkit](https://xmlgraphics.apache.org/batik/), which supplies parsers for SVG and CSS, as well as conversion from SVG primitives to Java Graphics2D primitives for sizing and layout.

## General Principles

This is a transform process.  The input to the transform in any XML document.  However, in order for any transformation to take place, elements need to be styled using CSS to indicate to Kite9 how to display them.  

For this reason, Kite9 defines plenty of it's own CSS directives, prefixed with `kite9`.  A full list is here.

