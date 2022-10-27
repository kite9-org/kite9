import { parseTransform } from './api.js';

type Coords = {
	x: number, 
	y: number
}

type BBox = Coords & {
	width: number,
	height: number
}

let _svg : SVGSVGElement;

export function getMainSvg() : SVGSVGElement {
	if (_svg == undefined) {
		_svg = document.querySelector("div.main svg");
	}
	return _svg;
}

export function getHtmlCoords(evt: Event) : Coords {
	if (evt instanceof MouseEvent) {
		const out =  {x: evt.pageX, y: evt.pageY};
		return out;
	} else if (evt instanceof TouchEvent) {
		const t = evt.changedTouches[0];
		const out =  {x: t.pageX, y: t.pageY};
		return out;		
	} else {
		throw Error("Unsupported event type");
	}
}

export function getSVGCoords(evt : Event, draw = false) {
	const out = getHtmlCoords(evt);
	const transform = getMainSvg().style.transform;
	const t = parseTransform(transform);
	out.x = out.x / t.scaleX;
	out.y = out.y / t.scaleY;
	
	if (draw) {
		const el = document.createElementNS("http://www.w3.org/2000/svg", "ellipse")
		el.setAttribute("cx", ""+ out.x);
		el.setAttribute("cy", ""+ out.y);
		el.setAttribute("rx", "4px");
		el.setAttribute("ry", "4px");
		getMainSvg().appendChild(el);
	}
	
	return out;
}

export function getElementPageBBox(e : Element) : BBox {
	if (e instanceof SVGGraphicsElement) {
		const mtrx = e.getCTM();
		const bbox = e.getBBox();
		return {
			x: mtrx.e + bbox.x,
			y: mtrx.f + bbox.y,
			width: bbox.width,
			height: bbox.height
		}
	} else {
		return {
			x: 0,
			y: 0,
			width: 0,
			height: 0
		}
	}
}

export function getElementHTMLBBox(e : Element) : BBox {
	const transform = getMainSvg().style.transform;
	const t = parseTransform(transform);
	const out = getElementPageBBox(e);
	out.x = out.x * t.scaleX;
	out.y = out.y * t.scaleY;
	out.width = out.width * t.scaleX;
	out.height = out.height * t.scaleY;
	return out;
}

export function getElementsPageBBox(elements : Element[]) : BBox {
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
export function is_touch_device4() : boolean {
  return (('ontouchstart' in window) ||
     (navigator.maxTouchPoints > 0));
}

/**
 * More reliable method of getting current target, which works with touch events. 
 */
export function currentTarget(event: Event) : SVGGraphicsElement {
	if ((event as any).touchTarget) {
		return (event as any).touchTarget;
	}
	
	const coords = getHtmlCoords(event);		
	const v = document.elementFromPoint(coords.x - window.pageXOffset, coords.y - window.pageYOffset);
	(event as any).touchTarget = v;
	return v as SVGGraphicsElement;
}

/** 
 * SVG Element builder
 */
export function svg(tag: string, atts: object = {}, contents: Element[] = []) {
	const e = document.createElementNS("http://www.w3.org/2000/svg", tag);
	Object.keys(atts).forEach(key => {
		const val = atts[key];
		if (val) {
			e.setAttribute(key, val);
		}

	});

	if (contents) {
		contents.forEach(c => e.appendChild(c));
	}

	return e;
}


/** 
 * Detect whether we can render on the client side
 */
export function canRenderClientSide() {
	return ((window.CSS as any).registerProperty != null);
}


/**
 * Returns the string 'up', 'down','left','right' 
 * for a given point on the screen related to a target.
 */
export function closestSide(dropTarget: Element, eventCoords : Coords = {x :0, y: 0}) {
	const boxCoords = getElementPageBBox(dropTarget);
	
	const topDist = Math.abs(eventCoords.y - boxCoords.y); 
	const leftDist = Math.abs(eventCoords.x - boxCoords.x); 
	const bottomDist = Math.abs(boxCoords.y +  boxCoords.height - eventCoords.y);
	const rightDist = Math.abs(boxCoords.x + boxCoords.width - eventCoords.x);
	
	const dists = {
		'up': topDist,
		'right': rightDist, 
		'down': bottomDist, 
		'left': leftDist 
	};
	
	const bestSide = ['up', 'right', 'down', 'left']
		.reduce((a, b) => dists[a] < dists[b] ? a : b, 'top');
		
	return bestSide;
}

