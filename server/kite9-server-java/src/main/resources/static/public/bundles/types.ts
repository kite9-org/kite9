/**
 * For definining a selection on the diagram (e.g. all links)
 */
export type Selector = () => Element[]

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
export const directions =  ["up" , "down", "left" , "right" ] 
export type Direction  = "up" | "down" | "left" | "right"

