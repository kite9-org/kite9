/**
 * For definining a selection on the diagram (e.g. all links)
 */
export type Selector = () => Element[]

/**
 * For filtering elements in a selection
 */
export type ElementFilter = (e?: Element) => boolean

/**
 * For filtering a pair of elements.  e.g. for a drag target and a drop target.
 */
export type ElementBiFilter = (e1?: Element, e2?: Element) => boolean

/**
 * For definining a selection from a palette (e.g. all links)
 */
export type PaletteSelector = (e: Element) => Element[]

/**
 * Various usages of a point
 */
export type Point = {x : number, y: number}

/**
 * Point with width, height
 */
export type Area = Point & {
	width: number,
	height: number
}

export type Range = [number, number]

/**
 * Given an id, returns the element
 */
export type Finder = (u: string) => Element

/**
 * Used everywhere, e.g. css values.  Weird there's no proper enum 
 * support for this..
 */
export type Direction  = "up" | "down" | "left" | "right"
export const directions : Direction[] =  ["up" , "down", "left" , "right" ] 

export function rotateClockwise(d: Direction | undefined) : Direction {
	switch(d) {
		case 'up':
			return 'right';
		case 'down':
			return 'left';
		case 'left':
			return 'up';
		case 'right':
			return 'down';
		case undefined:
			return undefined;
	}
}

export function rotateAntiClockwise(d: Direction | undefined) : Direction {
	switch(d) {
		case 'up':
			return 'left';
		case 'down':
			return 'right';
		case 'left':
			return 'down';
		case 'right':
			return 'up';
		case undefined:
			return undefined;
	}
}

export function sharedArea(a1: Area, a2: Area) : Area {
	const x1= Math.max(a1.x, a2.x);
	const y1= Math.max(a1.y, a2.y);
	const x2= Math.min(a1.x+a1.width, a2.x+a2.width);
	const y2= Math.min(a1.y+a1.height, a2.y+a2.height);
	
	return {
		x: x1,
		y: y1,
		width: Math.max(0, x2-x1),
		height: Math.max(0, y2-y1)
	} as Area
	
}

export function intersects(r1: Range, r2: Range) {
	const startIn = (r1[0] >= r2[0]) && (r1[0] < r2[1]);
	const endIn = (r1[1] > r2[0]) && (r1[1] <= r2[1]);
	return startIn || endIn;
}
