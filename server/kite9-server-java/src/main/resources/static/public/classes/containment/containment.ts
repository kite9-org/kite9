export const WILDCARD = "*";

export type WcElement = '*' | Element | null

export type ContainmentCallback = (elements: Element[], parents?: WcElement[], children?: WcElement[]) => Element[]

/** 
 * Handles drag and drop rules, as well as surround/contain and insert.  Replace is handled elsewhere.
 */
export class Containment {

	callbacks : ContainmentCallback[] = []
	
	add(cb: ContainmentCallback) {
		this.callbacks.push(cb);
	}
	
	
	/**
	 * This is the main function.  elements is the set of elements that will be reduced and returned.  
	 * Generally, these are SVG (kite9) elements, but you can also supply "*" ( a string-star), which acts as a wildcard.
	 * Parents and children are optional, and will "limit" the elements returned in the reduction.
	 */
	allowed(elements : Element[], parents? :WcElement[], children: WcElement[] = []) : Element[] {
		// run each callback.  return the union of allowed elements from all callbacks.
		
		return this.callbacks
			.map(cb => cb(elements, parents, children))
			.reduce((a, b) => [...new Set([...a, ...b])]);
	}

	/**
	 * Helper function
	 */
	canContainAny(element: Element[] | Element, parent: Element[] | Element) : boolean {
		return this.allowed(
			Array.isArray(element) ? element : [element], 
			Array.isArray(parent) ? parent : [parent]).length == 1;
	}
	
	/**
	 * Helper function
	 */
	canContainAll(element: Element[] | Element, parent: Element[] | Element) : boolean {
		const needed = Array.isArray(element) ? element.length : 1;
		return this.allowed(
			Array.isArray(element) ? element : [element], 
			Array.isArray(parent) ? parent : [parent]
			).length == needed;
	}

}