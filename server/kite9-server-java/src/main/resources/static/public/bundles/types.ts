/**
 * For definining a selection on the diagram (e.g. all links)
 */
export type Selector = () => Element[]

/**
 * For filtering elements in a selection
 */
export type ElementFilter = (e?: Element) => boolean

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

