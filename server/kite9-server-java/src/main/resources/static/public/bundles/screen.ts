import { parseTransform } from './api.js';
import { Area, Direction, Point } from './types.js';

let _svg: SVGSVGElement;

export function getMainSvg(): SVGSVGElement {
	if (_svg == undefined) {
		_svg = document.querySelector("div.main svg");
	}
	return _svg;
}

export function getHtmlCoords(evt: Event): Point {
	if (evt instanceof MouseEvent) {
		const out = { x: evt.pageX, y: evt.pageY };
		return out;
	} else if (evt instanceof TouchEvent) {
		const t = evt.changedTouches[0];
		const out = { x: t.pageX, y: t.pageY };
		return out;
	} else {
		throw Error("Unsupported event type");
	}
}

export function getSVGCoords(evt: Event, draw = false): Point {
	const out = getHtmlCoords(evt);
	const transform = getMainSvg().style.transform;
	const t = parseTransform(transform);
	out.x = out.x / t.scaleX;
	out.y = out.y / t.scaleY;

	if (draw) {
		const el = document.createElementNS("http://www.w3.org/2000/svg", "ellipse")
		el.setAttribute("cx", "" + out.x);
		el.setAttribute("cy", "" + out.y);
		el.setAttribute("rx", "4px");
		el.setAttribute("ry", "4px");
		getMainSvg().appendChild(el);
	}

	return out;
}

export function getElementPageBBox(e: Element): Area {
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

export function getElementHTMLBBox(e: Element): Area {
	const transform = getMainSvg().style.transform;
	const t = parseTransform(transform);
	const out = getElementPageBBox(e);
	out.x = out.x * t.scaleX - window.pageXOffset;
	out.y = out.y * t.scaleY - window.pageYOffset;
	out.width = out.width * t.scaleX;
	out.height = out.height * t.scaleY;
	return out;
}

export function maxArea(elements: Area[]): Area {
	if (elements.length == 0) {
		throw new Error("No elements provided")
	} else if (elements.length == 1) {
		return elements[0];
	} else {
		type Bound = {x: number, y: number, x2: number, y2: number }
	
		const bounds: Bound[] = elements
			.map(bb => {
				return {
					x: bb.x,
					y: bb.y,
					x2: bb.x + bb.width,
					y2: bb.y + bb.height
				}
			});

		const bound: Bound = bounds.reduce((a, b) => {
			return {
				x: Math.min(a.x, b.x),
				y: Math.min(a.y, b.y),
				x2: Math.max(a.x2, b.x2),
				y2: Math.max(a.y2, b.y2)
			}
		});

		return  {
				x: bound.x,
				y: bound.y,
				width: bound.x2 - bound.x,
				height: bound.y2 - bound.y
		}
	}
}

/**
 * This is from https://stackoverflow.com/questions/4817029/whats-the-best-way-to-detect-a-touch-screen-device-using-javascript#4819886
 */
export function is_touch_device4(): boolean {
	return (('ontouchstart' in window) ||
		(navigator.maxTouchPoints > 0));
}

/**
 * More reliable method of getting current target, which works with touch events. 
 */
export function currentTarget(event: Event): SVGGraphicsElement {
	if ((event as any).touchTarget) {
		return (event as any).touchTarget;
	}

	const coords = getHtmlCoords(event);
	const v = document.elementFromPoint(coords.x - window.pageXOffset, coords.y - window.pageYOffset);
	(event as any).touchTarget = v;
	return v as SVGGraphicsElement;
}

/*
 * More reliable method of getting all current targets, which works with touch events. 
 */
export function currentTargets(event: Event): SVGGraphicsElement[] {
	if ((event as any).touchTargets) {
		return (event as any).touchTargets;
	}

	const coords = getHtmlCoords(event);
	const v = document.elementsFromPoint(coords.x - window.pageXOffset, coords.y - window.pageYOffset);
	(event as any).touchTargets = v;
	return v as SVGGraphicsElement[];
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
export function canRenderClientSide(): boolean {
	return ((window.CSS as any).registerProperty != null);
}


/**
 * Returns the string 'up', 'down','left','right' 
 * for a given point on the screen related to a target.
 */
export function closestSide(dropTarget: Element, eventCoords: Point = { x: 0, y: 0 }): Direction {
	const boxCoords = getElementPageBBox(dropTarget);

	const topDist = Math.abs(eventCoords.y - boxCoords.y);
	const leftDist = Math.abs(eventCoords.x - boxCoords.x);
	const bottomDist = Math.abs(boxCoords.y + boxCoords.height - eventCoords.y);
	const rightDist = Math.abs(boxCoords.x + boxCoords.width - eventCoords.x);

	type Pair = { d: Direction, a: number }
	
	const dists: Pair[] = [
		{ d: 'up',  a: topDist},
		{ d:'right', a: rightDist},
		{ d:'down', a: bottomDist},
		{ d: 'left', a: leftDist}
	];
	
	const DEFAULT: Pair = {d: "up", a: 10000 }  

	const bestSide = 
		dists.reduce((a: Pair, b: Pair) => (a.a < b.a ? a : b), DEFAULT);

	return bestSide.d;
}

