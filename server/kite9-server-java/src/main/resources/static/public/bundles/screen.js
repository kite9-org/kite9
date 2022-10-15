import { parseTransform } from './api.js';

var _svg;

export function getMainSvg() {
	if (_svg == undefined) {
		_svg = document.querySelector("div.main svg");
	}
	return _svg;
}

export function getHtmlCoords(evt) {
	var out =  {x: evt.pageX, y: evt.pageY};
	
	return out;
}

export function getSVGCoords(evt, draw) {
	var out = getHtmlCoords(evt);
	var transform = getMainSvg().style.transform;
	var t = parseTransform(transform);
	out.x = out.x / t.scaleX;
	out.y = out.y / t.scaleY;
	
	if (draw) {
		var el = document.createElementNS("http://www.w3.org/2000/svg", "ellipse")
		el.setAttribute("cx", out.x);
		el.setAttribute("cy", out.y);
		el.setAttribute("rx", "4px");
		el.setAttribute("ry", "4px");
		getMainSvg().appendChild(el);
	}
	
	return out;
}

export function getElementPageBBox(e) {
	const mtrx = e.getCTM();
	const bbox = e.getBBox();
	return {
		x: mtrx.e + bbox.x,
		y: mtrx.f + bbox.y,
		width: bbox.width,
		height: bbox.height
	}
}

export function getElementHTMLBBox(e) {
	var transform = getMainSvg().style.transform;
	var t = parseTransform(transform);
	var out = getElementPageBBox(e);
	out.x = out.x * t.scaleX;
	out.y = out.y * t.scaleY;
	out.width = out.width * t.scaleX;
	out.height = out.height * t.scaleY;
	return out;
}

export function getElementsPageBBox(elements) {
	return elements
		.map(e => getElementPageBBox(e))
		.reduce((a, b) => { return {
			x: Math.min(a.x, b.x),
			y: Math.min(a.y, b.y),
			width: Math.max(a.x + a.width, b.x + b.width) - Math.min(a.x, b.x),
			height: Math.max(a.y + a.height, b.y + b.height) - Math.min(a.y, b.y)
		}});
}

/**
 * This is from https://stackoverflow.com/questions/4817029/whats-the-best-way-to-detect-a-touch-screen-device-using-javascript#4819886
 */
export function is_touch_device4() {
    
    var prefixes = ' -webkit- -moz- -o- -ms- '.split(' ');
    
    var mq = function (query) {
        return window.matchMedia(query).matches;
    }

    if (('ontouchstart' in window) || window.DocumentTouch && document instanceof DocumentTouch) {
        return true;
    }

    // include the 'heartz' as a way to have a non matching MQ to help terminate the join
    // https://git.io/vznFH
    var query = ['(', prefixes.join('touch-enabled),('), 'heartz', ')'].join('');
    return mq(query);
}


/**
 * More reliable method of getting current target, which works with touch events. 
 */
export function currentTarget(event) {
	if (event.touchTarget) {
		return event.touchTarget;
	}
	
	const coords = getHtmlCoords(event);		
	var v = document.elementFromPoint(coords.x - window.pageXOffset, coords.y - window.pageYOffset);
	event.touchTarget = v;
	return event.touchTarget;
}

/** 
 * SVG Element builder
 */
export function svg(tag, atts, contents) {
	function objectEach(m, action) {
		for (var key in m) {
		    // skip loop if the property is from prototype
		    if (m.hasOwnProperty(key)) {
		    	const val = m[key];
		    	if (val) {
			        action(key, val);
		    	}
		    }
		}	
	}
	
	var e = document.createElementNS("http://www.w3.org/2000/svg", tag);
	objectEach(atts, (k, v) => e.setAttribute(k, v));
	
	if (contents) {
		contents.forEach(c => e.appendChild(c));
	}
	
	return e;
}


/** 
 * Detect whether we can render on the client side
 */
export function canRenderClientSide() {
	return (window.CSS.registerProperty != null);
}

