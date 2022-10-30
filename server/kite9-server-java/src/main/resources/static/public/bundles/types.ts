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
 * Given an id, returns the element
 */
export type Finder = (u: string) => Element
